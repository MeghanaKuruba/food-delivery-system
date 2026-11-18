package com.ordertracking.verification.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record OcrResult(
        boolean successful,
        String extractedText,
    Map<String, Object> extractedData,
        String reason
) {

    public static OcrResult success(String extractedText) {
    return success(extractedText, Map.of());
    }

    public static OcrResult success(String extractedText, Map<String, Object> extractedData) {
        return new OcrResult(
                true,
                extractedText,
        immutableCopy(extractedData),
                null
        );
    }

    public static OcrResult failure(String reason) {
        return new OcrResult(
                false,
                null,
                Map.of(),
                reason
        );
    }

    private static Map<String, Object> immutableCopy(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}