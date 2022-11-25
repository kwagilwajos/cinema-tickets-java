package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.account.AccountService;
import uk.gov.dwp.uc.account.AccountServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.*;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceImpl implements TicketService {

    // Declarations
    private static final AccountService accountService = new AccountServiceImpl();
    private static final TicketPaymentService ticketPaymentService = new TicketPaymentServiceImpl();
    private static final SeatReservationService seatReservationService = new SeatReservationServiceImpl();

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        var ticketBooking = bookingVerifier(ticketTypeRequests);

        var amountToBePaid = calculateBookingAmount(ticketBooking);

        makePayment(accountId,amountToBePaid);

        makeReservation(accountId, ticketBooking);
    }

    /**
     * @param ticketTypeRequests : Domain Pojo storing Ticket type and number of tickets
     * @return Map<Type, Integer> ; Contains Ticket Type and respective number of seats booked
     */
    private static Map<Type, Integer> bookingVerifier(TicketTypeRequest... ticketTypeRequests) {
        // Check number of tickets
        var numberOfTickets = Arrays.stream(ticketTypeRequests)
                .filter(x -> x.getTicketType() != INFANT)
                .map(TicketTypeRequest::getNoOfTickets)
                .mapToInt(x -> x)
                .sum();

        if (numberOfTickets > 20)
            throw new InvalidPurchaseException("Number of tickets exceeds the limit");

        // Infant/child to adult ratio
        var ticketBooking = Arrays.stream(ticketTypeRequests)
                .filter(x -> x.getTicketType() != INFANT)
                .collect(Collectors.
                        groupingBy(TicketTypeRequest::getTicketType,
                                Collectors.mapping(TicketTypeRequest::getNoOfTickets, Collectors.summingInt(x -> x))));

        if (ticketBooking.containsValue(0))
            throw new InvalidPurchaseException("Number of tickets can't be 0");

        if (!ticketBooking.containsKey(ADULT))
            throw new InvalidPurchaseException("Child/Infant ticket must be purchased with adult ticket");

        return ticketBooking;
    }

    /**
     * @param ticketBooking : A map containing Ticket Type and respective number of seats booked
     * @return Int variable: contains the Amount to be paid for all the tickets
     */
    private static int calculateBookingAmount(Map<Type, Integer> ticketBooking) {
        final int ADULT_FARE = 20;
        final int CHILD_FARE = 10;
        int bookingAmount = 0;

        //calculate total price
        for (Map.Entry<Type, Integer> entry : ticketBooking.entrySet()) {
            if (entry.getKey() == ADULT) bookingAmount += ADULT_FARE * entry.getValue();
            if (entry.getKey() == CHILD) bookingAmount += CHILD_FARE * entry.getValue();
        }

        return bookingAmount;
    }


    /**
     * @param accountId : Long Type : Account ID for the account o be debited
     * @param amount : Int Type: Amount to be debited
     */
    private static void makePayment(Long accountId, int amount) {
        if (!accountService.validateAccount(accountId)) throw new InvalidPurchaseException("Invalid account");
        if(accountService.checkBalance(accountId) < amount) throw  new InvalidPurchaseException("Insufficient funds from the account");

       try {
           ticketPaymentService.makePayment(accountId,amount);
       }catch (Exception e){
           throw new InvalidPurchaseException("Something went wrong during making payment: "+ e.getMessage());
       }
    }

    /**
     * @param AccountId : identifier for account to which the reservation is to be made
     * @param ticketBooking : A map containing Ticket Type and respective number of seats to be reserved.
     */
    private static void makeReservation(Long AccountId, Map<Type, Integer> ticketBooking) {
        var numberOfSeats = ticketBooking
                .values()
                .stream()
                .mapToInt(x -> x).sum();

        try {
            seatReservationService.reserveSeat(AccountId,numberOfSeats);
        }catch (Exception e){
            throw new InvalidPurchaseException("Something went wrong during seats reservation: "+ e.getMessage());
        }
    }

}
