package uk.gov.dwp.uc.account;

public class AccountServiceImpl implements AccountService {
    @Override
    public boolean validateAccount(Long accountId) {
        if (accountId > 0) return true;
        /*
        todo: implementation for validating account,
              now returning true as a dummy value
         */
        return true;
    }

    @Override
    public long checkBalance(Long accountId) {
        /*
        todo: Implementation for balance checking logic
              putting maximum possible booking as a dummy value
         */
        return 400;
    }
}
