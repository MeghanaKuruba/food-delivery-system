package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.dto.SubmitVerificationDocumentRequest;
import com.ordertracking.verification.dto.UpdateVerificationDocumentRequest;
import com.ordertracking.verification.dto.VerificationDocumentResponse;
import com.ordertracking.verification.entity.VerificationApplication;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.exception.DuplicateVerificationDocumentException;
import com.ordertracking.verification.exception.VerificationApplicationNotFoundException;
import com.ordertracking.verification.exception.VerificationDocumentNotFoundException;
import com.ordertracking.verification.mapper.VerificationMapper;
import com.ordertracking.verification.repository.VerificationApplicationRepository;
import com.ordertracking.verification.repository.VerificationDocumentRepository;
import com.ordertracking.verification.service.VerificationDocumentService;
import com.ordertracking.verification.service.storage.DocumentFileValidationService;
import com.ordertracking.verification.service.storage.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VerificationDocumentServiceImpl implements VerificationDocumentService {

    private final VerificationApplicationRepository applicationRepository;

    private final VerificationDocumentRepository documentRepository;

    private final VerificationMapper verificationMapper;

    private final DocumentStorageService documentStorageService;

    private final DocumentFileValidationService documentFileValidationService;

    @Override
    public VerificationDocumentResponse submitDocument(
            Long applicationId,
            SubmitVerificationDocumentRequest request) {

        log.info(
                "Submitting verification document. applicationId={}, documentType={}, scope={}",
                applicationId,
                request.getDocumentType(),
                request.getDocumentScope()
        );

        VerificationApplication application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Verification application not found. applicationId={}",
                                    applicationId
                            );

                            return new VerificationApplicationNotFoundException(
                                    "Verification application not found."
                            );
                        });

        validateDocumentTypeNotAlreadySubmitted(
                applicationId,
                request.getDocumentType()
        );

        validateDuplicateDocumentNumber(
                request.getDocumentNumber()
        );

        documentFileValidationService.validate(
                request.getDocument()
        );

        VerificationDocument document =
                verificationMapper.toVerificationDocument(
                        request,
                        application
                );

        document.setDocumentScope(
                request.getDocumentScope()
        );

        document.setStatus(
                DocumentStatus.UNDER_REVIEW
        );

        String documentId =
                document.getDocumentId();

        String storageReference = null;

        try {

            storageReference =
                    documentStorageService.store(
                            request.getDocument(),
                            documentId,
                            request.getDocumentType().name()
                    );

            document.setDocumentUrl(
                    storageReference
            );

            VerificationDocument saved =
                    documentRepository.save(document);

            log.info(
                    "Verification document submitted successfully. applicationId={}, documentId={}, documentType={}",
                    applicationId,
                    saved.getDocumentId(),
                    saved.getDocumentType()
            );

            return verificationMapper.toVerificationDocumentResponse(
                    saved
            );

        } catch (RuntimeException exception) {

            log.error(
                    "Verification document submission failed. applicationId={}, documentId={}, documentType={}",
                    applicationId,
                    documentId,
                    request.getDocumentType(),
                    exception
            );

            if (storageReference != null) {

                documentStorageService.delete(
                        storageReference
                );
            }

            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationDocumentResponse getDocument(
            String documentId) {

        log.debug(
                "Fetching verification document. documentId={}",
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

        return verificationMapper.toVerificationDocumentResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationDocumentResponse> getDocuments(
            Long applicationId) {

        log.debug(
                "Fetching verification documents. applicationId={}",
                applicationId
        );

        if (!applicationRepository.existsById(applicationId)) {

            log.warn(
                    "Verification application not found. applicationId={}",
                    applicationId
            );

            throw new VerificationApplicationNotFoundException(
                    "Verification application not found."
            );
        }

        List<VerificationDocument> documents =
                documentRepository
                        .findByVerificationApplicationId(applicationId);

        log.info(
                "Verification documents fetched. applicationId={}, count={}",
                applicationId,
                documents.size()
        );

        return documents
                .stream()
                .map(verificationMapper::toVerificationDocumentResponse)
                .toList();
    }

    @Override
    public VerificationDocumentResponse updateDocument(
            String documentId,
            UpdateVerificationDocumentRequest request) {

        log.info(
                "Updating verification document. documentId={}, documentType={}",
                documentId,
                request.getDocumentType()
        );


        VerificationDocument document =
                documentRepository.findById(documentId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Verification document not found for update. documentId={}",
                                    documentId
                            );

                            return new VerificationDocumentNotFoundException(
                                    "Verification document not found."
                            );
                        });
        /*
         * PAN cannot be changed to GST during update.
         *
         * PAN -> PAN    allowed
         * PAN -> GST    rejected
         */
        if (document.getDocumentType()
                != request.getDocumentType()) {

            log.warn(
                    "Document type change rejected. documentId={}, existingType={}, requestedType={}",
                    documentId,
                    document.getDocumentType(),
                    request.getDocumentType()
            );

            throw new IllegalArgumentException(
                    "Document type cannot be changed during document update."
            );
        }

        /*
         * If the user is replacing the document with another
         * document number, check whether that number is already
         * being used elsewhere.
         */
        boolean documentNumberChanged =
                !document.getDocumentNumber()
                        .equalsIgnoreCase(
                                request.getDocumentNumber()
                        );


        if (documentNumberChanged &&
                documentRepository.existsByDocumentNumberIgnoreCase(
                        request.getDocumentNumber()
                )) {

            log.warn(
                    "Document update rejected because document number already exists. documentId={}, documentType={}",
                    documentId,
                    request.getDocumentType()
            );

            throw new DuplicateVerificationDocumentException(
                    "This document number has already been submitted."
            );
        }
        document.setDocumentNumber(
                request.getDocumentNumber()
        );

        document.setDocumentUrl(
                request.getDocumentUrl()
        );

        document.setIssuedAt(
                request.getIssuedAt()
        );

        document.setExpiryDate(
                request.getExpiryDate()
        );


        /*
         * Any updated/re-uploaded document must be verified again.
         */
        document.setStatus(
                DocumentStatus.UNDER_REVIEW
        );

        document.setRejectionReason(null);

        document.setVerifiedAt(null);

        VerificationDocument saved =
                documentRepository.save(document);


        log.info(
                "Verification document updated successfully. documentId={}, documentType={}, status={}",
                saved.getDocumentId(),
                saved.getDocumentType(),
                saved.getStatus()
        );


        return verificationMapper.toVerificationDocumentResponse(
                saved
        );
    }

    private void validateDocumentTypeNotAlreadySubmitted(
            Long applicationId,
            com.ordertracking.verification.enums.DocumentType documentType) {

        boolean exists =
                documentRepository
                        .existsByVerificationApplicationIdAndDocumentType(
                                applicationId,
                                documentType
                        );


        if (exists) {

            log.warn(
                    "Duplicate document type submission rejected. applicationId={}, documentType={}",
                    applicationId,
                    documentType
            );

            throw new DuplicateVerificationDocumentException(
                    "A " + documentType +
                            " document has already been submitted " +
                            "for this verification application. " +
                            "Use the update document operation to replace it."
            );
        }
    }


    private void validateDuplicateDocumentNumber(
            String documentNumber) {

        boolean exists =
                documentRepository
                        .existsByDocumentNumberIgnoreCase(
                                documentNumber
                        );


        if (exists) {

            log.warn(
                    "Duplicate verification document submission attempted. documentType={}, documentNumber={}",
                    "REDACTED",
                    maskDocumentNumber(documentNumber)
            );

            throw new DuplicateVerificationDocumentException(
                    "This document has already been submitted."
            );
        }
    }

    private String maskDocumentNumber(String documentNumber) {

        if (documentNumber == null || documentNumber.length() <= 4) {
            return "****";
        }

        return "****" +
                documentNumber.substring(
                        documentNumber.length() - 4
                );
    }
}