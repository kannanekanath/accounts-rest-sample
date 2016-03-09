package com.kannane.webservice.accounts;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Purely for test purposes to simulate concurrency by blocking threads/unblocking in lifecycles
 */
public class TestListener implements InMemoryEventListener<Account> {
    private static final Object TEST = new Object();
    private static final CompletableFuture<Object> COMPLETED_FUTURE = completedFuture(TEST);
    private CompletableFuture<Object> creationBlock = COMPLETED_FUTURE, deletionBlock = COMPLETED_FUTURE;

    private Map<Long, CompletableFuture<Object>> loadBlock = new HashMap<>(), updateBlock = new HashMap<>();

    @Override
    public void onEvent(CrudEventType eventType, Account object) {
        try {
            switch (eventType) {
                case CREATED:
                    creationBlock.get();
                    break;
                case DELETED:
                    deletionBlock.get();
                    break;
                case UPDATED:
                    updateBlock.getOrDefault(object.getId(), COMPLETED_FUTURE).get();
                    break;
                case LOADED:
                    loadBlock.getOrDefault(object.getId(), COMPLETED_FUTURE).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void blockCreation() {
        creationBlock = new CompletableFuture<>();
    }

    public void blockDeletion() {
        deletionBlock = new CompletableFuture<>();
    }

    public void blockLoad(Long id) {
        loadBlock.put(id, new CompletableFuture<>());
    }

    public void blockUpdate(Long id) {
        updateBlock.put(id, new CompletableFuture<>());
    }

    public void unblockCreation() {
        creationBlock.complete(TEST);
    }

    public void unblockDeletion() {
        deletionBlock.complete(TEST);
    }

    public void unblockLoad(Long id) {
        loadBlock.get(id).complete(TEST);
    }

    public void unblockUpdate(Long id) {
        updateBlock.get(id).complete(TEST);
    }
}
