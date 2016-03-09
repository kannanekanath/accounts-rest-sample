package com.kannane.webservice.accounts;

import com.kannane.webservice.ServiceException;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singleton;

public class InMemoryAccountService implements AccountService {

    private final Map<Long, Account> accountsMap = new HashMap<>();
    private final Map<Long, ReadWriteLock> locksMap = new HashMap<>();
    private final ReadWriteLock globalLock = new ReentrantReadWriteLock();

    @Override
    public Optional<Account> findAccount(Long id) {
        return doInLockedContext(globalLock.readLock(), () -> {
            if (accountsMap.containsKey(id)) {
                return doInLockedContext(locksMap.get(id).readLock(), () -> Optional.of(getAccountInternal(id)));
            }
            return Optional.<Account>empty();
        });
    }

    @Override
    public void transferMoney(Long fromAccountId, Long toAccountId, Double amount) {
        if (Objects.equals(fromAccountId, toAccountId)) {
            throw new ServiceException("The from and to account should not be the same [" + fromAccountId + "]");
        }
        doInLockedContext(globalLock.readLock(), () -> {
            if (!accountsMap.containsKey(fromAccountId)) {
                throw new ServiceException("Account Id [" + fromAccountId + "] not found");
            }
            if (!accountsMap.containsKey(toAccountId)) {
                throw new ServiceException("Account Id [" + toAccountId + "] not found");
            }
            /**
             * We need to write lock both accounts however to avoid deadlock we will acquire resources
             * in increasing order
             *
             * https://www.securecoding.cert.org/confluence/display/java/LCK07-J.+Avoid+deadlock+by+requesting+and+releasing+locks+in+the+same+order
             */
            Collection<Lock> accountLocks = Stream.of(fromAccountId, toAccountId)
                    .sorted()
                    .map(locksMap::get)
                    .map(ReadWriteLock::writeLock)
                    .collect(Collectors.toList());
            return doInLockedContext(accountLocks,
                    () -> transferMoneyInternal(accountsMap.get(fromAccountId), accountsMap.get(toAccountId), amount));
        });
    }

    @Override
    public Account createAccount(Account account) {
        return doInLockedContext(globalLock.writeLock(), () -> createAccountInternal(account));
    }

    @Override
    public Account deleteAccount(Long id) {
        return doInLockedContext(globalLock.writeLock(), () -> {
            if (!accountsMap.containsKey(id)) {
                throw new ServiceException("Account with id [" + id + "] not found");
            }
            return doInLockedContext(locksMap.get(id).writeLock(),
                    () -> deleteAccountInternal(accountsMap.get(id)));
        });
    }

    private Account getAccountInternal(Long id) {
        Account account = accountsMap.get(id);
        notifyObservers(CrudEventType.LOADED, account);
        return account;
    }

    private Void transferMoneyInternal(Account fromAccount, Account toAccount, Double amount) {
        if (toAccount.getBalance() + amount <= toAccount.getBalance()) {
            throw new ServiceException("Rounding error with balance for [" + toAccount  + "]. Balance went too high");
        }
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);
        notifyObservers(CrudEventType.UPDATED, fromAccount);
        notifyObservers(CrudEventType.UPDATED, toAccount);
        return null;
    }

    private Account deleteAccountInternal(Account account) {
        Account deletedAccount = accountsMap.remove(account.getId());
        locksMap.remove(account.getId());
        notifyObservers(CrudEventType.DELETED, deletedAccount);
        return deletedAccount;
    }

    private Account createAccountInternal(Account account) {
        if (account.getId() != null && account.getId() != 0) {
            throw new ServiceException("Account already has an ID and cannot be created");
        }
        Account domainAccount = new Account((long) (accountsMap.size() + 1),
                account.getName(), account.getBalance());
        accountsMap.put(domainAccount.getId(), domainAccount);
        locksMap.put(domainAccount.getId(), new ReentrantReadWriteLock());
        notifyObservers(CrudEventType.CREATED, domainAccount);
        return domainAccount;
    }

    private void notifyObservers(CrudEventType eventType, Account account) {
        listeners.forEach(l -> l.onEvent(eventType, account));
    }

    void addListener(InMemoryEventListener<Account> l) {
        listeners.add(l);
    }

    private Collection<InMemoryEventListener<Account>> listeners = new ArrayList<>();


    private <T> T doInLockedContext(Lock readWriteLock, Supplier<T> function) {
        return doInLockedContext(singleton(readWriteLock), function);
    }

    /**
     * This is a reusable utility method when you want to run a given function in the context of locks.
     *
     * ex: to read an account, read-lock the global lock, write-lock the account lock and read data.
     *
     * @param locks the read locks to use
      @param function the function to execute in the context
     * @param <T> the generic
     * @return the returned value from function
     */
    private <T> T doInLockedContext(Collection<Lock> locks, Supplier<T> function) {
        locks.stream().forEach(Lock::lock);
        try {
            return function.get();
        } finally {
            locks.stream().forEach(Lock::unlock);
        }
    }


}
