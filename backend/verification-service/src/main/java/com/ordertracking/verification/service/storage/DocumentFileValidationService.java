package com.ordertracking.verification.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
@Slf4j
public class DocumentFileValidationService {

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "application/pdf",
                    "image/png",
                    "image/jpeg"
            );

    private static final String GENERIC_CONTENT_TYPE = "application/octet-stream";

    private final Tika tika = new Tika();

    /**
     * Validates the uploaded document file and detects its actual content type.
     *
     * @param file The uploaded document file.
     * @return The detected actual content type of the file.
     * @throws IllegalArgumentException If the file is invalid or does not meet the requirements.
     */
    public String validateAndDetectType(MultipartFile file) {

        if (file == null || file.isEmpty()) {

            log.warn("Verification document upload rejected because file is empty.");

            throw new IllegalArgumentException(
                    "Document file is required."
            );
        }

        if (file.getSize() > maxFileSize.toBytes()) {

            log.warn(
                    "Verification document upload rejected because file exceeds maximum size. size={}",
                    file.getSize()
            );

            throw new IllegalArgumentException(
                    "Document file size must not exceed 5 MB."
            );
        }

        String declaredContentType =
                normalizeContentType(file.getContentType());

        log.debug(
                "Validating verification document. declaredContentType={}, size={}, originalFilename={}",
                declaredContentType,
                file.getSize(),
                file.getOriginalFilename()
        );

        String actualContentType =
                detectActualContentType(file);

        log.debug(
                "Detected actual verification document content type. declaredContentType={}, actualContentType={}",
                declaredContentType,
                actualContentType
        );

        validateActualContentType(actualContentType);

        validateDeclaredContentType(
                declaredContentType,
                actualContentType
        );

        log.info(
                "Verification document file validation successful. actualContentType={}, size={}",
                actualContentType,
                file.getSize()
        );

        return actualContentType;
    }

    /**
     * Normalizes the content type string by trimming whitespace and converting to lowercase.
     *
     * @param contentType The content type string to normalize.
     * @return The normalized content type string, or null if the input is null.
     */
    private String normalizeContentType(String contentType) {

        if (contentType == null) {
            return null;
        }

        return contentType
                .trim()
                .toLowerCase();
    }

    /**
     * Detects the actual content type of the uploaded file using Apache Tika.
     *
     * @param file The uploaded document file.
     * @return The detected actual content type, or null if it cannot be determined.
     * @throws IllegalArgumentException If there is an error reading the file.
     */
    private String detectActualContentType(MultipartFile file) {

        try (InputStream inputStream = file.getInputStream()) {

            String detectedContentType =
                    tika.detect(inputStream);

            if (GENERIC_CONTENT_TYPE.equals(
                    detectedContentType)) {

                return null;
            }

            return normalizeDetectedContentType(
                    detectedContentType
            );

        } catch (IOException exception) {

            log.error(
                    "Unable to read verification document file for content validation.",
                    exception
            );

            throw new IllegalArgumentException(
                    "Unable to validate document file.",
                    exception
            );
        }
    }

    /**
     * Validates the actual content type of the uploaded file against allowed types.
     *
     * @param actualContentType The detected actual content type of the file.
     * @throws IllegalArgumentException If the actual content type is not allowed.
     */
    private void validateActualContentType(String actualContentType) {

        if (actualContentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(actualContentType)) {

            log.warn(
                    "Unsupported verification document content detected. actualContentType={}",
                    actualContentType
            );

            throw new IllegalArgumentException(
                    "The uploaded file is not a valid PDF, PNG or JPEG document."
            );
        }
    }

    /**
     * Validates the declared content type against the actual content type of the uploaded file.
     *
     * @param declaredContentType The content type declared by the client.
     * @param actualContentType   The detected actual content type of the file.
     * @throws IllegalArgumentException If the declared content type is not allowed or does not match the actual content type.
     */
    private void validateDeclaredContentType(String declaredContentType, String actualContentType) {

        if (declaredContentType == null ||
                GENERIC_CONTENT_TYPE.equals(declaredContentType)) {

            /*
             * The client did not provide a reliable MIME type.
             * Apache Tika has already validated the actual content.
             */
            return;
        }

        if (isJpeg(declaredContentType) &&
                "image/jpeg".equals(actualContentType)) {

            return;
        }

        if (!ALLOWED_CONTENT_TYPES.contains(declaredContentType)) {

            log.warn(
                    "Unsupported declared MIME type. declaredContentType={}, actualContentType={}",
                    declaredContentType,
                    actualContentType
            );

            throw new IllegalArgumentException(
                    "Unsupported document MIME type."
            );
        }

        if (!declaredContentType.equals(actualContentType)) {

            log.warn(
                    "Verification document MIME type does not match actual file content. declaredContentType={}, actualContentType={}",
                    declaredContentType,
                    actualContentType
            );

            throw new IllegalArgumentException(
                    "The uploaded file type does not match its actual content."
            );
        }
    }

    /**
     * Normalizes the detected content type string for specific cases.
     *
     * @param contentType The detected content type string.
     * @return The normalized content type string.
     */
    private String normalizeDetectedContentType(String contentType) {

        if ("image/jpg".equals(contentType)) {
            return "image/jpeg";
        }

        return contentType;
    }

    /**
     * Checks if the given content type represents a JPEG image.
     *
     * @param contentType The content type to check.
     * @return True if the content type is "image/jpeg" or "image/jpg", false otherwise.
     */
    private boolean isJpeg(String contentType) {

        return "image/jpeg".equals(contentType) || "image/jpg".equals(contentType);
    }
}