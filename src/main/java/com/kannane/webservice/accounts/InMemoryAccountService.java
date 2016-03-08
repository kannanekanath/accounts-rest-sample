package com.kannane.webservice.accounts;

import com.kannane.webservice.ServiceException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryAccountService implements AccountService {

    private final Map<String, Account> accounts;

    public InMemoryAccountService() {
        /** Ideally this could go inside another repository but trying to keep everything simple here*/
        this(sampleAccounts());
    }

    private static Map<String, Account> sampleAccounts() {
        return Stream.of(
                new Account(1L, "user1", 45.0),
                new Account(2L, "user2", 79.5),
                new Account(3L, "user3", 125.4))
                .collect(Collectors.toMap(a -> a.getId().toString(), Function.<Account>identity()));
    }

    public InMemoryAccountService(final Map<String, Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public Optional<Account> findAccountById(final String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public void transferMoney(final String fromAccountId, final String toAccountId, final Double amount) {
        Optional<Account> fromAccount = findAccountById(fromAccountId);
        fromAccount.orElseThrow(() -> new ServiceException("The fromAccount [" + fromAccountId + "] does not exist"));
        Optional<Account> toAccount = findAccountById(toAccountId);
        toAccount.orElseThrow(() -> new ServiceException("The toAccount [" + toAccountId + "] does not exist"));
        if (Objects.equals(fromAccountId, toAccountId)) {
            throw new ServiceException("The from and to account should not be the same [" + fromAccountId + "]");
        }
        /**
         * There is no transaction semantics in memory, so lets assume we wont fail
         * But in reality we need to rollback if one aspect has failed
         */
        fromAccount.get().withdraw(amount);
        toAccount.get().deposit(amount);
    }
}
