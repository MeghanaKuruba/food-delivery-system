package com.ordertracking.verification.dto;

import com.ordertracking.verification.enums.ApplicantType;
import com.ordertracking.verification.enums.VerificationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationApplicationResponse {

    /** ID of this application; use authUserId to identify its owner. */
    private Long id;

    private Long authUserId;

    private ApplicantType applicantType;

    private String referenceId;

    private VerificationStatus status;

    private String reason;

    private LocalDateTime submittedAt;

    private LocalDateTime updatedAt;
}