package uk.gov.dwp.uc.account;

public interface AccountService {
    boolean validateAccount(long accountId);
    int  checkBalance(long accountId);

}
