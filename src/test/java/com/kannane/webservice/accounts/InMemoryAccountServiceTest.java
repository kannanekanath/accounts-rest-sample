package com.kannane.webservice.accounts;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;
import java.util.concurrent.*;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Designed to abuse the concurrency aspects
 */
public class InMemoryAccountServiceTest {

    public static final int STANDARD_DELAY = 500;
    private Executor customExecutor = command -> {
        new Thread(command).start();
    };

    private InMemoryAccountService service;
    private TestListener listener;

    @Before
    public void before() {
        service = new InMemoryAccountService();
        listener = new TestListener();
        service.addListener(listener);
    }
    @Test
    public void testInsertAndDeleteSimultaneously() throws Exception {
        /**
         * Make creation wait so we can test deletion
         */
        Account accountToDelete = createAccountAsync().get();
        listener.blockCreation();
        CompletableFuture<Account> creation = createAccountAsync();
        assertFalse("Creation thread will not finish until we send the signal", creation.isDone());
        CompletableFuture<Account> deletion = deleteAccountAsync(accountToDelete.getId(), STANDARD_DELAY);

        sleep(STANDARD_DELAY);
        assertFalse("Deletion will not finish even after 500 ms since we are blocking the signal", deletion.isDone());

        listener.unblockCreation();
        creation.get(1, TimeUnit.SECONDS);
        deletion.get(1, TimeUnit.SECONDS);
        assertTrue("Creation thread is now complete because we have sent the signal", creation.isDone());
        assertTrue("Deletion thread is now complete because we have sent the signal", deletion.isDone());
    }

    @Test
    public void testTransferSimultaneous() throws Exception {
        Account a1 = createAccountAsync().get(), a2 = createAccountAsync().get(),
            a3 = createAccountAsync().get(), a4 = createAccountAsync().get(), a5 = createAccountAsync().get();
        listener.blockUpdate(a1.getId());
        double balance = a1.getBalance();

        CompletableFuture<Void> transfer1 = transferAccountsAsync(a1, a2, 1d, 0);
        sleep(STANDARD_DELAY);
        assertFalse("Transfer wont be complete as it is blocked", transfer1.isDone());

        CompletableFuture<Void> transfer2 = transferAccountsAsync(a1, a3, 1d, STANDARD_DELAY);
        sleep(2 * STANDARD_DELAY);
        assertFalse("Second transfer wont complete since we cannot lock the account", transfer2.isDone());

        CompletableFuture<Void> transfer3 = transferAccountsAsync(a4, a5, 1d, STANDARD_DELAY);
        transfer3.get(1, TimeUnit.SECONDS);
        assertTrue("A transfer that does not involve locked accounts goes okay", transfer3.isDone());

        listener.unblockUpdate(a1.getId());
        transfer1.get(1, TimeUnit.SECONDS);
        transfer2.get(1, TimeUnit.SECONDS);
        assertEquals("Both transfers for account a1 is through", balance - 2, a1.getBalance().doubleValue(), 0.00001);
    }

    private CompletableFuture<Account> createAccountAsync() {
        return supplyAsync(() -> service.createAccount(new Account(0L, "randomuser", 20d)), customExecutor);
    }

    private CompletableFuture<Account> deleteAccountAsync(Long id, long delay) {
        sleep(delay);
        return supplyAsync(() -> service.deleteAccount(id), customExecutor);
    }

    private CompletableFuture<Optional<Account>> findAccountAsync(Long id, long delay) {
        sleep(delay);
        return supplyAsync(() -> service.findAccount(id), customExecutor);
    }

    private CompletableFuture<Void> transferAccountsAsync(Account from, Account to,
                                                          Double amount, long delay) {
        sleep(delay);
        return runAsync(() -> service.transferMoney(from.getId(), to.getId(), amount), customExecutor);
    }

    private void sleep(long delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
