package uk.gov.dwp.uc.account;

public interface AccountService {
    boolean validateAccount(Long accountId);
    long  checkBalance(Long accountId);

}
