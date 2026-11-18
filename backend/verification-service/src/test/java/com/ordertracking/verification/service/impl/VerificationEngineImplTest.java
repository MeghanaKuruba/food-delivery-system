package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.dto.OcrResult;
import com.ordertracking.verification.dto.VerificationResult;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.service.OcrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationEngineImplTest {

    @Mock
    private OcrService ocrService;

    @InjectMocks
    private VerificationEngineImpl verificationEngine;

    @Test
    void verifyPropagatesStructuredPanFieldsAndKeepsManualReviewStatus() {
        VerificationDocument document = VerificationDocument.builder()
                .documentId("PAN-12345678")
                .build();
        Map<String, Object> panFields = Map.of(
                "panNumber", "ABCDE1234F",
                "name", "Example Person",
                "fatherName", "Example Parent",
                "dateOfBirth", "01/01/1990"
        );
        when(ocrService.extractText(document))
                .thenReturn(OcrResult.success("recognized PAN text", panFields));

        VerificationResult result = verificationEngine.verify(document);

        assertEquals(DocumentStatus.MANUAL_REVIEW, result.status());
        assertEquals(panFields, result.extractedData());
    }
}
