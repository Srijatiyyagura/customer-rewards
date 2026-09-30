package com.retailer.rewards.exception;

/**
 * Base exception for resources not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
