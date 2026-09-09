package com.ordertracking.verification.service;

import com.ordertracking.verification.enums.DocumentType;

public interface VerificationDocumentRule {

    void validate(DocumentType documentType);
}