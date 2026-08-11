package com.ordertracking.verification.exception;

public class DuplicateVerificationDocumentException
        extends RuntimeException {

    public DuplicateVerificationDocumentException(String message) {
        super(message);
    }
}