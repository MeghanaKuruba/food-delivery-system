package com.ordertracking.verification.controller;

import com.ordertracking.verification.dto.*;
import com.ordertracking.verification.enums.DocumentScope;
import com.ordertracking.verification.enums.DocumentType;
import com.ordertracking.verification.service.VerificationDocumentService;
import com.ordertracking.verification.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/verifications")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    private final VerificationDocumentService verificationDocumentService;

    /**
     * Create a new verification application.
     *
     * @param request the request body containing the details of the verification application
     * @return ResponseEntity containing the created verification application response
     */
    @PostMapping
    public ResponseEntity<VerificationApplicationResponse> createApplication(
            @Valid @RequestBody CreateVerificationApplicationRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(verificationService.createApplication(request));
    }

    /**
     * Get a verification application by its ID.
     *
     * @param applicationId the ID of the verification application
     * @return ResponseEntity containing the verification application response
     */
    @GetMapping("/{applicationId}")
    public ResponseEntity<VerificationApplicationResponse> getApplication(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(verificationService.getApplication(applicationId));
    }

    /**
     * Get all verification applications for a specific user.
     *
     * @param authUserId the ID of the authenticated user
     * @return ResponseEntity containing a list of verification application responses
     */
    @GetMapping("/user/{authUserId}")
    public ResponseEntity<List<VerificationApplicationResponse>> getApplicationsByUser(
            @PathVariable Long authUserId) {

        return ResponseEntity.ok(verificationService.getApplicationsByUser(authUserId));
    }

    /**
     * Submit a verification document for a specific application.
     *
     * @param applicationId the ID of the verification application
     * @param request       the request body containing the details of the verification document
     * @return ResponseEntity containing the created verification document response
     */
    @PostMapping(
            value = "/{applicationId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<VerificationDocumentResponse> submitDocument(
            @PathVariable Long applicationId,
            @Valid @ModelAttribute SubmitVerificationDocumentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        verificationDocumentService.submitDocument(
                                applicationId,
                                request
                        )
                );
    }

    /**
     * Get a verification document by its ID.
     *
     * @param documentId the ID of the verification document
     * @return ResponseEntity containing the verification document response
     */
    @GetMapping("/documents/{documentId}")
    public ResponseEntity<VerificationDocumentResponse> getDocument(
            @PathVariable String documentId) {

        return ResponseEntity.ok(verificationDocumentService.getDocument(documentId));
    }

    /**
     * Get all verification documents for a specific application.
     *
     * @param applicationId the ID of the verification application
     * @return ResponseEntity containing a list of verification document responses
     */
    @GetMapping("/{applicationId}/documents")
    public ResponseEntity<List<VerificationDocumentResponse>> getDocuments(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(verificationDocumentService.getDocuments(applicationId));
    }

    /**
     * Update a verification document by its ID.
     *
     * @param documentId     the ID of the verification document
     * @param documentNumber the number of the document
     * @param documentScope  the scope of the document
     * @param issuedAt       the issue date of the document (optional)
     * @param expiryDate     the expiry date of the document (optional)
     * @param document       the new document file (optional)
     * @return ResponseEntity containing the updated verification document response
     */
    @PutMapping(
            value = "/documents/{documentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<VerificationDocumentResponse> updateDocument(
            @PathVariable String documentId,

            @RequestParam("documentNumber")
            String documentNumber,

            @RequestParam("documentScope")
            DocumentScope documentScope,

            @RequestParam(value = "issuedAt", required = false)
            LocalDate issuedAt,

            @RequestParam(value = "expiryDate", required = false)
            LocalDate expiryDate,

            @RequestPart("document")
            MultipartFile document) {

        UpdateVerificationDocumentRequest request =
                UpdateVerificationDocumentRequest.builder()
                        .documentNumber(documentNumber)
                        .documentScope(documentScope)
                        .issuedAt(issuedAt)
                        .expiryDate(expiryDate)
                        .build();

        return ResponseEntity.ok(verificationDocumentService.updateDocument(documentId, request, document));
    }
}