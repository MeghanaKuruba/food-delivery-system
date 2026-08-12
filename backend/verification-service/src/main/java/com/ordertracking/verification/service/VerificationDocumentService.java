package com.ordertracking.verification.service;

import com.ordertracking.verification.dto.SubmitVerificationDocumentRequest;
import com.ordertracking.verification.dto.UpdateVerificationDocumentRequest;
import com.ordertracking.verification.dto.VerificationDocumentResponse;

import java.util.List;

public interface VerificationDocumentService {

    VerificationDocumentResponse submitDocument(Long applicationId, SubmitVerificationDocumentRequest request);

    VerificationDocumentResponse getDocument(String documentId);

    List<VerificationDocumentResponse> getDocuments(Long applicationId);

    VerificationDocumentResponse updateDocument(String documentId, UpdateVerificationDocumentRequest request);
}