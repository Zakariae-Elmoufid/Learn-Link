package org.example.learnlink.common.exception;

/**
 * Exception thrown when a business validation fails
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
