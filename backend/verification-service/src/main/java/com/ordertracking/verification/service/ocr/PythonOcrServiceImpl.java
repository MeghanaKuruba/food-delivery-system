package com.ordertracking.verification.service.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.ordertracking.verification.dto.OcrResult;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentType;
import com.ordertracking.verification.service.OcrService;
import com.ordertracking.verification.service.storage.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@ConditionalOnProperty(
        name = "verification.ocr.provider",
        havingValue = "python",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class PythonOcrServiceImpl implements OcrService {

    private final DocumentStorageService documentStorageService;
    private final RestClient.Builder restClientBuilder;

    @Value("${verification.ocr.url}")
    private String ocrUrl;

    @Override
    public OcrResult extractText(VerificationDocument document) {
        String storageReference = document.getDocumentUrl();
        if (storageReference == null || storageReference.isBlank()) {
            return OcrResult.failure("Document storage reference is missing.");
        }

        String extension = getExtension(storageReference);
        if (".pdf".equals(extension)) {
            return OcrResult.failure("PDF OCR processing is not implemented yet.");
        }

        String pythonDocumentType = toPythonDocumentType(document.getDocumentType());
        if (pythonDocumentType == null) {
            return OcrResult.failure(
                    "OCR is not supported for document type " + document.getDocumentType() + "."
            );
        }

        MediaType contentType = getImageContentType(extension);
        if (contentType == null) {
            return OcrResult.failure("Only PNG and JPEG image documents can be processed by OCR.");
        }

        try (InputStream inputStream = documentStorageService.load(storageReference)) {
            byte[] imageBytes = inputStream.readAllBytes();
            if (imageBytes.length == 0) {
                return OcrResult.failure("Document image is empty.");
            }

            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return document.getDocumentId() + extension;
                }
            };

            MultipartBodyBuilder multipart = new MultipartBodyBuilder();
            multipart.part("document_type", pythonDocumentType);
            multipart.part("file", imageResource)
                    .filename(imageResource.getFilename())
                    .contentType(contentType);

            JsonNode response = restClientBuilder.clone()
                    .build()
                    .post()
                    .uri(ocrUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(multipart.build())
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.path("success").asBoolean(false)) {
                log.warn("Python OCR returned an unsuccessful response. documentId={}", document.getDocumentId());
                return OcrResult.failure("OCR service could not process the document.");
            }

            String extractedText = toPlainText(response.path("ocrText"));
            if (extractedText.isBlank()) {
                return OcrResult.failure("No text could be extracted from document.");
            }

            log.info(
                    "Python OCR extraction completed. documentId={}, characterCount={}",
                    document.getDocumentId(),
                    extractedText.length()
            );
                return OcrResult.success(
                    extractedText,
                    extractPanFields(response.path("data"), document.getDocumentType())
                );
        } catch (IOException | RestClientException exception) {
            log.error("Python OCR request failed. documentId={}", document.getDocumentId(), exception);
            return OcrResult.failure("OCR service is unavailable or failed to process the document.");
        }
    }

    private String toPythonDocumentType(DocumentType documentType) {
        return switch (documentType) {
            case PAN, GST, FSSAI, DRIVING_LICENSE -> documentType.name();
            case VEHICLE_RC -> "RC";
            case VEHICLE_INSURANCE -> "INSURANCE";
            case BUSINESS_REGISTRATION, OTHER -> null;
        };
    }

    private String getExtension(String storageReference) {
        int dot = storageReference.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return storageReference.substring(dot).toLowerCase(Locale.ROOT);
    }

    private MediaType getImageContentType(String extension) {
        return switch (extension) {
            case ".png" -> MediaType.IMAGE_PNG;
            case ".jpg", ".jpeg" -> MediaType.IMAGE_JPEG;
            default -> null;
        };
    }

    private String toPlainText(JsonNode ocrText) {
        if (ocrText.isTextual()) {
            return ocrText.asText().trim();
        }
        if (!ocrText.isArray()) {
            return "";
        }

        ArrayList<String> lines = new ArrayList<>();
        for (JsonNode token : ocrText) {
            String text = token.path("text").asText("").trim();
            if (!text.isEmpty()) {
                lines.add(text);
            }
        }
        return String.join("\n", lines);
    }

    private Map<String, Object> extractPanFields(JsonNode data, DocumentType documentType) {
        if (documentType != DocumentType.PAN || data == null || !data.isObject()) {
            return Map.of();
        }

        Map<String, Object> fields = new LinkedHashMap<>();
        for (String fieldName : new String[]{"panNumber", "name", "fatherName", "dateOfBirth"}) {
            JsonNode value = data.get(fieldName);
            if (value != null && !value.isNull()) {
                fields.put(fieldName, value.asText());
            }
        }
        return fields;
    }
}