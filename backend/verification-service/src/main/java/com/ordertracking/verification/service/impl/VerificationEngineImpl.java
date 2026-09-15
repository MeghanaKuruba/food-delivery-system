package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.dto.OcrResult;
import com.ordertracking.verification.dto.VerificationResult;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.service.OcrService;
import com.ordertracking.verification.service.VerificationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationEngineImpl
        implements VerificationEngine {

    private final OcrService ocrService;

    @Override
    public VerificationResult verify(VerificationDocument document) {

        log.info(
                "Starting document verification. documentId={}, documentType={}",
                document.getDocumentId(),
                document.getDocumentType()
        );

        /*
         * Step 1:
         * Extract readable text from the uploaded document.
         */
        OcrResult ocrResult = ocrService.extractText(document);

        if (!ocrResult.successful()) {

            log.warn(
                    "OCR extraction unsuccessful. documentId={}, reason={}",
                    document.getDocumentId(),
                    ocrResult.reason()
            );

            return new VerificationResult(
                    DocumentStatus.MANUAL_REVIEW,
                    ocrResult.reason()
            );
        }

        /*
         * OCR is only an extraction step.
         *
         * We do NOT mark the document VERIFIED
         * merely because text was extracted.
         */
        log.info(
                "OCR text extracted successfully. documentId={}, characterCount={}",
                document.getDocumentId(),
                ocrResult.extractedText().length()
        );

        return new VerificationResult(
                DocumentStatus.MANUAL_REVIEW,
                "Document text extracted successfully. Document verification rules are pending."
        );
    }
}