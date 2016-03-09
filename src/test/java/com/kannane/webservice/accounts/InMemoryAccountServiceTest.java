package com.kannane.webservice.accounts;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.CompletableFuture.runAsync;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Designed to abuse the concurrency aspects
 */
public class InMemoryAccountServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInsertAndDeleteSimultaneously() throws Exception {
        /**
         * Make creation wait so we can test deletion
         */
        CompletableFuture<Object> signal = new CompletableFuture<>();
        InMemoryAccountService service = new InMemoryAccountService();
        service.addListener(((eventType, object) -> {
            if(eventType.equals(CrudEventType.CREATED)) {
                try {
                    signal.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }));
        CompletableFuture<Void> creation = runAsync(() -> service.createAccount(new Account(0L, "account1", 45d)));
        assertFalse("Creation thread will not finish until we send the signal", creation.isDone());
        CompletableFuture<Void> deletion = runAsync(() -> service.deleteAccount(1L));

        thrown.expect(TimeoutException.class);
        deletion.get(1, TimeUnit.SECONDS);
        assertFalse("Deletion thread will not finish even after 500 ms since we are blocking the signal",
                deletion.isDone());

        signal.complete("");
        creation.get();
        assertTrue("Creation thread is now complete because we have sent the signal", creation.isDone());
        assertTrue("Deletion thread is now complete because we have sent the signal", deletion.isDone());
    }

    public void testTransferSimultaneous() throws Exception {
        InMemoryAccountService service = new InMemoryAccountService();
    }
}
