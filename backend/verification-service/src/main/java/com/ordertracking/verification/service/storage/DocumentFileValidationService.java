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

    private String normalizeContentType(String contentType) {

        if (contentType == null) {
            return null;
        }

        return contentType
                .trim()
                .toLowerCase();
    }

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

    private void validateActualContentType(
            String actualContentType) {

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

    private void validateDeclaredContentType(
            String declaredContentType,
            String actualContentType) {

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

    private String normalizeDetectedContentType(
            String contentType) {

        if ("image/jpg".equals(contentType)) {
            return "image/jpeg";
        }

        return contentType;
    }

    private void validateContentTypeMatch(
            String declaredContentType,
            String actualContentType) {

        if (actualContentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(
                        actualContentType)) {

            log.warn(
                    "Unsupported or invalid verification document file content. declaredContentType={}, actualContentType={}",
                    declaredContentType,
                    actualContentType
            );

            throw new IllegalArgumentException("The uploaded file is not a valid PDF, PNG or JPEG document.");
        }

        /*
         * application/octet-stream means the client did not
         * provide a specific MIME type.
         *
         * In that case, rely on the actual file content
         * detected by Apache Tika.
         */
        if (GENERIC_CONTENT_TYPE.equals(declaredContentType)) {

            return;
        }

        /*
         * image/jpg and image/jpeg represent the same
         * actual JPEG file format.
         */
        if (isJpeg(declaredContentType) && "image/jpeg".equals(actualContentType)) {

            return;
        }

        if (!declaredContentType.equals(actualContentType)) {

            log.warn(
                    "Verification document MIME type does not match actual file content. declaredContentType={}, actualContentType={}",
                    declaredContentType,
                    actualContentType
            );

            throw new IllegalArgumentException("The uploaded file type does not match its actual content.");
        }
    }

    private boolean isJpeg(String contentType) {

        return "image/jpeg".equals(contentType) || "image/jpg".equals(contentType);
    }
}