package com.ordertracking.verification.dto;

import com.ordertracking.verification.enums.DocumentStatus;

import java.util.Map;

public record VerificationResult(
	DocumentStatus status,
	String reason,
	Map<String, Object> extractedData
) {
}