package com.ordertracking.verification.service;

import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentStatus;

public interface DocumentStatusTransitionService {

    void validateTransition(DocumentStatus currentStatus, DocumentStatus newStatus);

    void transition(VerificationDocument document, DocumentStatus targetStatus);
}