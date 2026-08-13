package com.ordertracking.verification.controller;

import com.ordertracking.verification.dto.*;
import com.ordertracking.verification.service.VerificationDocumentService;
import com.ordertracking.verification.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/verifications")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    private final VerificationDocumentService verificationDocumentService;

    @PostMapping
    public ResponseEntity<VerificationApplicationResponse> createApplication(
            @Valid @RequestBody CreateVerificationApplicationRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(verificationService.createApplication(request));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<VerificationApplicationResponse> getApplication(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(verificationService.getApplication(applicationId));
    }

    @GetMapping("/user/{authUserId}")
    public ResponseEntity<List<VerificationApplicationResponse>> getApplicationsByUser(
            @PathVariable Long authUserId) {

        return ResponseEntity.ok(verificationService.getApplicationsByUser(authUserId));
    }

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

    @GetMapping("/documents/{documentId}")
    public ResponseEntity<VerificationDocumentResponse> getDocument(
            @PathVariable String documentId) {

        return ResponseEntity.ok(verificationDocumentService.getDocument(documentId));
    }

    @GetMapping("/{applicationId}/documents")
    public ResponseEntity<List<VerificationDocumentResponse>> getDocuments(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(verificationDocumentService.getDocuments(applicationId));
    }

    @PutMapping("/documents/{documentId}")
    public ResponseEntity<VerificationDocumentResponse> updateDocument(
            @PathVariable String documentId,
            @Valid @RequestBody UpdateVerificationDocumentRequest request) {

        return ResponseEntity.ok(verificationDocumentService.updateDocument(documentId, request));
    }
}