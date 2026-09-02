package com.ordertracking.verification.dto;

import com.ordertracking.verification.enums.DocumentStatus;

public record VerificationResult(DocumentStatus status, String reason) {
}