package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.dto.VerificationResult;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.service.VerificationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VerificationEngineImpl implements VerificationEngine {

    @Override
    public VerificationResult verify(VerificationDocument document) {

        log.info(
                "Starting document verification. documentId={}, documentType={}",
                document.getDocumentId(),
                document.getDocumentType()
        );

        /*
         * Real verification will be added later.
         *
         * Planned flow:
         * 1. Validate uploaded document.
         * 2. Extract data using OCR.
         * 3. Load document-specific data.
         * 4. Compare against reference data.
         * 5. Apply document-specific verification rules.
         */

        log.info(
                "Document requires manual review. documentId={}, documentType={}",
                document.getDocumentId(),
                document.getDocumentType()
        );

        return new VerificationResult(
                DocumentStatus.MANUAL_REVIEW,
                "Automated verification is not yet implemented."
        );
    }
}