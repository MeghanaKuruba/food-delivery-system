package com.ordertracking.verification.service;

import com.ordertracking.verification.dto.VerificationDocumentResponse;

public interface VerificationDocumentVerificationService {

    VerificationDocumentResponse verifyDocument(String documentId);

    void verifyDocuments(Long applicationId);
}