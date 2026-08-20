package com.ordertracking.verification.exception;

public class InvalidDocumentStatusTransitionException
        extends RuntimeException {

    public InvalidDocumentStatusTransitionException(String message) {
        super(message);
    }
}