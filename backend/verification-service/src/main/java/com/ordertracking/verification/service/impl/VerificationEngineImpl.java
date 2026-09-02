package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.dto.VerificationResult;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.service.VerificationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VerificationEngineImpl
        implements VerificationEngine {

    @Override
    public VerificationResult verify(
            VerificationDocument document) {

        log.info(
                "Starting document verification. documentId={}, documentType={}",
                document.getDocumentId(),
                document.getDocumentType()
        );

        /*
         * Actual document verification will be implemented
         * in the next stage.
         *
         * This class currently establishes the verification
         * engine boundary.
         */
        return new VerificationResult(
                DocumentStatus.MANUAL_REVIEW,
                "Automated verification is not yet implemented."
        );
    }
}