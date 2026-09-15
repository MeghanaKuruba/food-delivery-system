package com.ordertracking.verification.dto;

public record OcrResult(
        boolean successful,
        String extractedText,
        String reason
) {

    public static OcrResult success(String extractedText) {
        return new OcrResult(
                true,
                extractedText,
                null
        );
    }

    public static OcrResult failure(String reason) {
        return new OcrResult(
                false,
                null,
                reason
        );
    }
}