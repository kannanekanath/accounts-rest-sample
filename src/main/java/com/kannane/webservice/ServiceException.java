package com.kannane.webservice;

/**
 * A marker interface for all service exceptions
 */
public class ServiceException extends RuntimeException {
    private final int httpErrorCode;

    public ServiceException(String message) {
        this(message, 500);
    }

    public ServiceException(String message, int httpErrorCode) {
        super(message);
        this.httpErrorCode = httpErrorCode;
    }

    public int getHttpErrorCode() {
        return httpErrorCode;
    }
}
