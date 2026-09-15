package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.dto.VerificationDocumentResponse;
import com.ordertracking.verification.dto.VerificationResult;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.exception.VerificationDocumentNotFoundException;
import com.ordertracking.verification.mapper.VerificationMapper;
import com.ordertracking.verification.repository.VerificationDocumentRepository;
import com.ordertracking.verification.service.DocumentStatusTransitionService;
import com.ordertracking.verification.service.VerificationDocumentVerificationService;
import com.ordertracking.verification.service.VerificationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationDocumentVerificationServiceImpl implements VerificationDocumentVerificationService {

    private final VerificationDocumentRepository documentRepository;

    private final DocumentStatusTransitionService documentStatusTransitionService;

    private final VerificationEngine verificationEngine;

    private final VerificationMapper mapper;


    @Override
    @Transactional
    public void verifyDocuments(Long applicationId) {

        log.info(
                "Starting verification for application. applicationId={}",
                applicationId
        );

        List<VerificationDocument> documents =
                documentRepository.findByVerificationApplicationId(
                        applicationId
                );

        if (documents.isEmpty()) {

            log.warn(
                    "No documents found for verification application. applicationId={}",
                    applicationId
            );

            throw new IllegalStateException(
                    "No verification documents found."
            );
        }

        for (VerificationDocument document : documents) {

            verifyDocument(document.getDocumentId());
        }

        log.info(
                "Document verification processing completed. applicationId={}",
                applicationId
        );
    }

    @Override
    @Transactional
    public VerificationDocumentResponse verifyDocument(String documentId) {

        log.info(
                "Starting document verification. documentId={}",
                documentId
        );

        VerificationDocument document =
                documentRepository.findById(documentId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Verification document not found. documentId={}",
                                    documentId
                            );

                            return new VerificationDocumentNotFoundException(
                                    "Verification document not found."
                            );
                        });


        if (document.getStatus() == DocumentStatus.VERIFIED) {

            log.warn(
                    "Verification document is already verified. documentId={}",
                    documentId
            );

            throw new IllegalStateException(
                    "Verification document is already verified."
            );
        }


        /*
         * Verification starts here.
         *
         * UPLOADED means the document was successfully
         * uploaded and stored.
         *
         * UNDER_REVIEW means verification processing
         * has started.
         */
        documentStatusTransitionService.transition(document, DocumentStatus.UNDER_REVIEW);


        /*
         * Persist the workflow state before running
         * the verification engine.
         */
        documentRepository.save(document);

        /*
         * Run the verification engine.
         */
        VerificationResult result = verificationEngine.verify(document);

        /*
         * Apply the result through the centralized
         * status transition rules.
         */
        documentStatusTransitionService.transition(document, result.status());

        document.setRejectionReason(result.reason());


        if (result.status() == DocumentStatus.VERIFIED) {
            document.setVerifiedAt(LocalDateTime.now());
        } else {
            document.setVerifiedAt(null);
        }

        VerificationDocument saved = documentRepository.save(document);

        log.info(
                "Document verification completed. documentId={}, status={}",
                saved.getDocumentId(),
                saved.getStatus()
        );

        return mapper.toVerificationDocumentResponse(saved);
    }
}