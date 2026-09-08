package com.ordertracking.verification.dto;

import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.enums.DocumentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationDocumentResponse {

    private String documentId;

    private String referenceId;

    private DocumentType documentType;

    private String documentUrl;

    private DocumentStatus status;

    private String rejectionReason;

    private LocalDateTime uploadedAt;

    private LocalDateTime verifiedAt;
}