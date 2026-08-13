package com.ordertracking.verification.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@Slf4j
public class DocumentFileValidationService {

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize MAX_FILE_SIZE;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "application/pdf",
                    "image/png",
                    "image/jpeg",
                    "image/jpg"
            );

    public void validate(MultipartFile file) {

        if (file == null ||
                file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Document file is required."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE.toBytes()) {

            throw new IllegalArgumentException(
                    "Document file size must not exceed 5 MB."
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(
                        contentType.toLowerCase()
                )) {

            log.warn(
                    "Unsupported verification document content type. contentType={}",
                    contentType
            );

            throw new IllegalArgumentException(
                    "Only PDF, PNG and JPEG documents are supported."
            );
        }

        log.debug(
                "Verification document file validation successful. contentType={}, size={}",
                contentType,
                file.getSize()
        );
    }
}