package com.ordertracking.verification.controller;

import com.ordertracking.verification.dto.*;
import com.ordertracking.verification.enums.DocumentScope;
import com.ordertracking.verification.service.VerificationDocumentService;
import com.ordertracking.verification.service.VerificationDocumentVerificationService;
import com.ordertracking.verification.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/verifications")
@RequiredArgsConstructor
@Tag(
        name = "Verification Management",
        description = "APIs for verification applications and document verification workflow"
)
public class VerificationController {

    private final VerificationService verificationService;

    private final VerificationDocumentService verificationDocumentService;

    private final VerificationDocumentVerificationService verificationDocumentVerificationService;

    /**
     * Create a new verification application.
     *
     * @param request the request body containing the details of the verification application
     * @return ResponseEntity containing the created verification application response
     */
    @Operation(
            summary = "Create verification application",
            description = "Creates a new verification application for a user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Application created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @Operation(
            summary = "Get verification application by ID",
            description = "Retrieves a verification application using its application ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{applicationId}")
    public ResponseEntity<VerificationApplicationResponse> getApplication(
            @Parameter(description = "Verification Application ID", example = "1")
            @PathVariable Long applicationId) {


        return ResponseEntity.ok(verificationService.getApplication(applicationId));
    }

    /**
     * Get all verification applications for a specific user.
     *
     * @param authUserId the ID of the authenticated user
     * @return ResponseEntity containing a list of verification application responses
     */
    @Operation(
            summary = "Get applications by user",
            description = "Retrieves all verification applications belonging to a user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{authUserId}")
    public ResponseEntity<List<VerificationApplicationResponse>> getApplicationsByUser(
            @Parameter(description = "Authenticated User ID", example = "101")
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
    @Operation(
            summary = "Submit verification document",
            description = "Uploads a verification document for an application."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid document data"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PostMapping(
            value = "/{applicationId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<VerificationDocumentResponse> submitDocument(
            @Parameter(description = "Application ID", example = "1")
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
    @Operation(
            summary = "Get verification document",
            description = "Retrieves a verification document by document ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/documents/{documentId}")
    public ResponseEntity<VerificationDocumentResponse> getDocument(
            @Parameter(description = "Document ID")
            @PathVariable String documentId) {

        return ResponseEntity.ok(verificationDocumentService.getDocument(documentId));
    }

    /**
     * Get all verification documents for a specific application.
     *
     * @param applicationId the ID of the verification application
     * @return ResponseEntity containing a list of verification document responses
     */
    @Operation(
            summary = "Get all documents for application",
            description = "Retrieves all documents associated with a verification application."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{applicationId}/documents")
    public ResponseEntity<List<VerificationDocumentResponse>> getDocuments(
            @Parameter(description = "Application ID", example = "1")
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
    @Operation(
            summary = "Update verification document",
            description = "Updates document metadata and uploads a replacement document."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PutMapping(
            value = "/documents/{documentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<VerificationDocumentResponse> updateDocument(
            @Parameter(description = "Document ID")
            @PathVariable String documentId,

            @Parameter(description = "Document Number", example = "123456")
            @RequestParam("documentNumber") String documentNumber,

            @Parameter(description = "Document Scope", example = "PERSON")
            @RequestParam("documentScope") DocumentScope documentScope,

            @Parameter(description = "Issue Date", example = "2025-01-01")
            @RequestParam(required = false) LocalDate issuedAt,

            @Parameter(description = "Expiry Date", example = "2035-01-01")
            @RequestParam(required = false) LocalDate expiryDate,

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

    /**
     * Verify a verification document by its ID.
     *
     * @param documentId the ID of the verification document
     * @return ResponseEntity containing the verification document response after verification
     */
    @Operation(
            summary = "Verify document",
            description = "Performs verification of a submitted document and updates its verification status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document verified successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "409", description = "Document already verified")
    })
    @PostMapping("/documents/{documentId}/verify")
    public ResponseEntity<VerificationDocumentResponse> verifyDocument(
            @Parameter(description = "Document ID")
            @PathVariable String documentId) {

        return ResponseEntity.ok(
                verificationDocumentVerificationService
                        .verifyDocument(documentId)
        );
    }
}