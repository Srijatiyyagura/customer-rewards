package com.retailer.rewards.exception;

/**
 * Exception thrown when an invalid date range is specified (e.g. startDate after endDate).
 */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
