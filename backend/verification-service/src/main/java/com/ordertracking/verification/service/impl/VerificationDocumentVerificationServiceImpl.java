package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.dto.VerificationDocumentResponse;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.exception.VerificationDocumentNotFoundException;
import com.ordertracking.verification.mapper.VerificationMapper;
import com.ordertracking.verification.repository.VerificationDocumentRepository;
import com.ordertracking.verification.service.DocumentStatusTransitionService;
import com.ordertracking.verification.service.VerificationDocumentVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationDocumentVerificationServiceImpl
        implements VerificationDocumentVerificationService {

    private final VerificationDocumentRepository documentRepository;

    private final DocumentStatusTransitionService
            documentStatusTransitionService;

    private final VerificationMapper verificationMapper;

    @Override
    @Transactional
    public VerificationDocumentResponse verifyDocument(String documentId) {

        log.info("Starting document verification. documentId={}", documentId);

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

        DocumentStatus currentStatus = document.getStatus();

        /*
         * A document should only enter verification
         * when it is waiting for review.
         */
        if (currentStatus != DocumentStatus.UNDER_REVIEW) {

            log.warn(
                    "Document verification not allowed. documentId={}, status={}",
                    documentId,
                    currentStatus
            );

            throw new IllegalStateException(
                    "Document can only be verified when it is UNDER_REVIEW."
            );
        }

        /*
         * For now we are establishing the verification
         * workflow. The actual verification engine will
         * be implemented next.
         */
        DocumentStatus verificationResult = determineVerificationResult(document);

        documentStatusTransitionService.validateTransition(currentStatus, verificationResult);

        document.setStatus(verificationResult);

        if (verificationResult == DocumentStatus.VERIFIED) {
            document.setVerifiedAt(java.time.LocalDateTime.now());

            document.setRejectionReason(null);
        }

        if (verificationResult == DocumentStatus.REUPLOAD_REQUIRED) {
            document.setVerifiedAt(null);
        }

        if (verificationResult == DocumentStatus.REJECTED) {
            document.setVerifiedAt(null);
        }

        VerificationDocument saved = documentRepository.save(document);

        log.info(
                "Document verification completed. documentId={}, previousStatus={}, newStatus={}",
                documentId,
                currentStatus,
                saved.getStatus()
        );

        return verificationMapper.toVerificationDocumentResponse(saved);
    }

    private DocumentStatus determineVerificationResult(VerificationDocument document) {

        /*
         * Temporary placeholder.
         *
         * We will replace this method with our actual
         * verification engine / mock government-data
         * verification flow.
         */
        return DocumentStatus.VERIFIED;
    }
}