package com.retailer.rewards.exception;

/**
 * Exception thrown when a requested Customer cannot be found.
 */
public class CustomerNotFoundException extends ResourceNotFoundException {

    public CustomerNotFoundException(Long customerId) {
        super("Customer with ID " + customerId + " was not found.");
    }

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
