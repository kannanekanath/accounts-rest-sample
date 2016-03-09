package com.kannane.webservice.accounts;

/**
 * Useful for listening to updates in In memory databases. Used throughout in concurrent testing
 */
enum CrudEventType {CREATED, UPDATED, DELETED, LOADED};

public interface InMemoryEventListener<T> {
    void onEvent(CrudEventType eventType, T object);
}
