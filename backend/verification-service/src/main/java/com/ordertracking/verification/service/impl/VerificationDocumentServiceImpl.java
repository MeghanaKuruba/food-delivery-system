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
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * Submits a verification document for a specific application.
     *
     * @param applicationId The ID of the verification application.
     * @param request       The request containing document details and the file.
     * @return A response containing the submitted document's details.
     * @throws VerificationApplicationNotFoundException If the application does not exist.
     * @throws DuplicateVerificationDocumentException   If a document of the same type has already been submitted for this application or if the document number is already in use.
     */
    @Override
    public VerificationDocumentResponse submitDocument(Long applicationId, SubmitVerificationDocumentRequest request) {

        log.info(
                "Submitting verification document. applicationId={}, documentType={}, scope={}",
                applicationId,
                request.getDocumentType()
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

        validateDocumentTypeNotAlreadySubmitted(applicationId, request.getDocumentType());

        documentFileValidationService.validateAndDetectType(request.getDocument());

        VerificationDocument document =
                verificationMapper.toVerificationDocument(
                        request,
                        application
                );

        document.setStatus(DocumentStatus.UNDER_REVIEW);

        String documentId = document.getDocumentId();

        String storageReference = null;

        try {

            storageReference = documentStorageService.store(
                            request.getDocument(),
                            documentId,
                            request.getDocumentType().name()
                    );

            document.setDocumentUrl(storageReference);

            VerificationDocument saved = documentRepository.save(document);

            log.info(
                    "Verification document submitted successfully. applicationId={}, documentId={}, documentType={}",
                    applicationId,
                    saved.getDocumentId(),
                    saved.getDocumentType()
            );

            return verificationMapper.toVerificationDocumentResponse(saved);

        } catch (RuntimeException exception) {

            log.error(
                    "Verification document submission failed. applicationId={}, documentId={}, documentType={}",
                    applicationId,
                    documentId,
                    request.getDocumentType(),
                    exception
            );

            if (storageReference != null) {

                documentStorageService.delete(storageReference);
            }

            throw exception;
        }
    }

    /**
     * Retrieves a verification document by its ID.
     *
     * @param documentId The ID of the verification document.
     * @return A response containing the document's details.
     * @throws VerificationDocumentNotFoundException If the document does not exist.
     */
    @Override
    @Transactional(readOnly = true)
    public VerificationDocumentResponse getDocument(String documentId) {

        log.debug("Fetching verification document. documentId={}", documentId);

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

    /**
     * Retrieves all verification documents associated with a specific application.
     *
     * @param applicationId The ID of the verification application.
     * @return A list of responses containing the documents' details.
     * @throws VerificationApplicationNotFoundException If the application does not exist.
     */
    @Override
    @Transactional(readOnly = true)
    public List<VerificationDocumentResponse> getDocuments(Long applicationId) {

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

    /**
     * Updates an existing verification document.
     *
     * @param documentId The ID of the verification document to update.
     * @param request    The request containing updated document details.
     * @param document   The new document file to upload.
     * @return A response containing the updated document's details.
     * @throws VerificationDocumentNotFoundException If the document does not exist.
     * @throws IllegalStateException                  If the document cannot be updated in its current status.
     * @throws DuplicateVerificationDocumentException If a document with the same number already exists.
     */
    @Override
    public VerificationDocumentResponse updateDocument(String documentId, UpdateVerificationDocumentRequest request, MultipartFile document) {

        VerificationDocument existingDocument = documentRepository.findById(documentId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Verification document not found for update. documentId={}",
                                    documentId
                            );

                            return new VerificationDocumentNotFoundException(
                                    "Verification document not found."
                            );
                        });

        log.info(
                "Updating verification document. documentId={}, documentType={}, scope={}",
                documentId,
                existingDocument.getDocumentType()
        );

        DocumentStatus currentStatus = existingDocument.getStatus();

        /*
         * A document can only be re-uploaded when
         * verification has explicitly requested it
         * or the document has been rejected.
         */
        if (currentStatus != DocumentStatus.REUPLOAD_REQUIRED
                && currentStatus != DocumentStatus.REJECTED) {

            log.warn(
                    "Document update not allowed for current status. documentId={}, status={}",
                    documentId,
                    currentStatus
            );

            throw new IllegalStateException(
                    "Document cannot be updated in its current status."
            );
        }

        /*
         * Validate the uploaded file using the same
         * validation pipeline used during initial submission.
         */
        documentFileValidationService.validateAndDetectType(document);

        String oldStorageReference =
                existingDocument.getDocumentUrl();

        String newStorageReference = null;

        try {

            /*
             * Store the new file first.
             *
             * We do this before changing the DB record so that
             * the existing document remains usable if storage fails.
             */
            newStorageReference = documentStorageService.store(document, documentId, existingDocument.getDocumentType().name());

            /*
             * Update the existing document record instead
             * of creating a new VerificationDocument.
             */
            existingDocument.setDocumentType(existingDocument.getDocumentType());

            existingDocument.setDocumentUrl(newStorageReference);

            /*
             * A re-upload starts the verification process again.
             */
            existingDocument.setStatus(DocumentStatus.UNDER_REVIEW);

            existingDocument.setRejectionReason(null);

            existingDocument.setVerifiedAt(null);

            VerificationDocument saved =
                    documentRepository.save(existingDocument);

            /*
             * Database update succeeded.
             *
             * The old file is now no longer required.
             */
            if (oldStorageReference != null && !oldStorageReference.equals(newStorageReference)) {

                documentStorageService.delete(oldStorageReference);
            }

            log.info(
                    "Verification document updated successfully. documentId={}, status={}",
                    saved.getDocumentId(),
                    saved.getStatus()
            );

            return verificationMapper.toVerificationDocumentResponse(saved);

        } catch (RuntimeException exception) {

            /*
             * If a new file was stored but the DB update failed,
             * remove the newly stored file to avoid orphaned files.
             */
            if (newStorageReference != null) {

                log.warn(
                        "Cleaning up newly stored document after update failure. documentId={}",
                        documentId
                );

                documentStorageService.delete(
                        newStorageReference
                );
            }

            log.error(
                    "Failed to update verification document. documentId={}",
                    documentId,
                    exception
            );

            throw exception;
        }
    }

    /**
     * Validates that a document of the specified type has not already been submitted for the given application.
     *
     * @param applicationId The ID of the verification application.
     * @param documentType  The type of the document being submitted.
     * @throws DuplicateVerificationDocumentException If a document of the same type has already been submitted for this application.
     */
    private void validateDocumentTypeNotAlreadySubmitted(
            Long applicationId,
            com.ordertracking.verification.enums.DocumentType documentType) {

        boolean exists = documentRepository.existsByVerificationApplicationIdAndDocumentType(
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

    /**
     * Masks a document number for logging purposes, showing only the last 4 characters.
     *
     * @param documentNumber The document number to mask.
     * @return A masked version of the document number.
     */
    private String maskDocumentNumber(String documentNumber) {

        if (documentNumber == null || documentNumber.length() <= 4) {
            return "****";
        }

        return "****" + documentNumber.substring(documentNumber.length() - 4);
    }
}