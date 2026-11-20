package com.ordertracking.verification.dto;

import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationDocumentResponse {

    private String documentId;

    private String referenceId;

    private DocumentType documentType;

    private String documentUrl;

    private DocumentStatus status;

    private String rejectionReason;

    private LocalDateTime uploadedAt;

    private LocalDateTime verifiedAt;

    @JsonInclude(value = JsonInclude.Include.NON_NULL, content = JsonInclude.Include.NON_NULL)
    private Map<String, Object> extractedData;
}