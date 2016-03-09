package com.kannane.webservice.accounts;

import java.util.Optional;

public interface AccountService {

    Optional<Account> findAccount(final Long id);

    void transferMoney(final Long fromAccountId, final Long toAccountId, final Double amount);

    Account createAccount(Account account);

    Account deleteAccount(Long id);
}
