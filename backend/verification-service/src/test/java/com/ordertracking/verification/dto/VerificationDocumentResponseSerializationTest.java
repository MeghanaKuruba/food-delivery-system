package com.ordertracking.verification.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationDocumentResponseSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void omitsNullResponseFieldsAndNullExtractedValues() throws Exception {
        Map<String, Object> extractedData = new HashMap<>();
        extractedData.put("panNumber", "ABCDE1234F");
        extractedData.put("name", null);

        VerificationDocumentResponse response = VerificationDocumentResponse.builder()
                .documentId("PAN-12345678")
            .extractedData(extractedData)
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"documentId\":\"PAN-12345678\""));
        assertTrue(json.contains("\"panNumber\":\"ABCDE1234F\""));
        assertFalse(json.contains("null"));
        assertFalse(json.contains("name"));
        assertFalse(json.contains("referenceId"));
        assertFalse(json.contains("verifiedAt"));
    }
}
