package uk.gov.dwp.uc.pairtest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.account.AccountServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceImplTest {

    private TicketService ticketService;

    @Mock
    private TicketPaymentServiceImpl ticketPaymentService;
    @Mock
    private SeatReservationServiceImpl seatReservationService;
    @Mock
    private AccountServiceImpl accountService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ticketService = new TicketServiceImpl(accountService, ticketPaymentService, seatReservationService);

        doNothing().when(ticketPaymentService).makePayment(isA(Long.TYPE), isA(Integer.TYPE));
        doNothing().when(seatReservationService).reserveSeat(isA(Long.TYPE), isA(Integer.TYPE));
        when(accountService.validateAccount(isA(Long.TYPE))).thenReturn(true);
        when(accountService.checkBalance(isA(Long.TYPE))).thenReturn(400);
    }

    @Test
    public void purchaseTicketWithValidIdAndValidNumberOfSeats() {
        long accountId = 345678;
        TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(ADULT, 5);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(CHILD, 5);
        TicketTypeRequest ticketTypeRequest3 = new TicketTypeRequest(INFANT, 5);

        ticketService.purchaseTickets(accountId, ticketTypeRequest1, ticketTypeRequest2, ticketTypeRequest3);

        //Checking if the final method in the service is called
        verify(seatReservationService, times(1)).reserveSeat(accountId, 10);
    }

    @Test
    public void purchaseTicketWithValidIdAndInvalidNumberOfSeats() {
        long accountId = 345678;
        TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(ADULT, 15);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(CHILD, 15);
        TicketTypeRequest ticketTypeRequest3 = new TicketTypeRequest(INFANT, 5);

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId,
                ticketTypeRequest1, ticketTypeRequest2, ticketTypeRequest3));

        String expectedMessage = "Number of tickets exceeds the limit";
        String actualMessage = exception.getMessage();

        assertEquals(actualMessage, expectedMessage);
    }

    @Test
    public void purchaseTicketWithInValidIdAndInvalidNumberOfSeats() {
        long accountId = 0;
        TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(ADULT, 5);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(CHILD, 5);
        TicketTypeRequest ticketTypeRequest3 = new TicketTypeRequest(INFANT, 5);

        ticketService = new TicketServiceImpl(accountService, ticketPaymentService, seatReservationService);
        when(accountService.validateAccount(0)).thenReturn(false);

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId,
                ticketTypeRequest1, ticketTypeRequest2, ticketTypeRequest3));

        String expectedMessage = "Invalid account";
        String actualMessage = exception.getMessage();

        assertEquals(actualMessage, expectedMessage);
    }

    @Test
    public void purchaseTicketWithValidIdAndWithoutAdultTicket() {
        long accountId = 345678;
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(CHILD, 5);
        TicketTypeRequest ticketTypeRequest3 = new TicketTypeRequest(INFANT, 5);

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId,
                ticketTypeRequest2, ticketTypeRequest3));

        String expectedMessage = "Child/Infant ticket must be purchased with adult ticket";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

}