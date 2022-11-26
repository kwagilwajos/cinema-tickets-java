package uk.gov.dwp.uc.account;

public class AccountServiceImpl implements AccountService {
    @Override
    public boolean validateAccount(long accountId) {
        return accountId > 0;
        /*
        todo: implementation for validating account,
              now checking just if accountId is > 0
         */
    }

    @Override
    public int checkBalance(long accountId) {
        /*
        todo: Implementation for balance checking logic
              putting maximum possible booking as a dummy value
         */
        return 400;
    }
}
