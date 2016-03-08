package com.kannane.webservice.accounts;

import java.util.Optional;

public interface AccountService {

    Optional<Account> findAccountById(final String id);

    void transferMoney(final String fromAccountId, final String toAccountId, final Double amount);
}
