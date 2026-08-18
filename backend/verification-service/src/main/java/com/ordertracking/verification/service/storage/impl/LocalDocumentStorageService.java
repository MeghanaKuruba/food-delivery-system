package com.ordertracking.verification.service.storage.impl;

import com.ordertracking.verification.service.storage.DocumentStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class LocalDocumentStorageService implements DocumentStorageService {

    private final Path storageRoot;

    /**
     * Initializes the local document storage service with the specified storage location.
     *
     * @param storageLocation The root directory where documents will be stored.
     */
    public LocalDocumentStorageService(@Value("${document.storage.location}") String storageLocation) {

        log.info(
                "Initializing LocalDocumentStorageService with location={}",
                storageLocation
        );

        this.storageRoot =
                Paths.get(storageLocation)
                        .toAbsolutePath()
                        .normalize();

        try {
            Files.createDirectories(storageRoot);

            log.info(
                    "Document storage initialized. location={}",
                    storageRoot
            );

        } catch (IOException exception) {

            log.error(
                    "Failed to initialize document storage. location={}",
                    storageRoot,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to initialize document storage.",
                    exception
            );
        }
    }

    /**
     * Stores the provided document file in the local storage.
     *
     * @param file         The document file to be stored.
     * @param documentId   The unique identifier for the document.
     * @param documentType The type of the document (e.g., passport, license).
     * @return The relative path to the stored document.
     */
    @Override
    public String store(MultipartFile file, String documentId, String documentType) {

        String extension = getFileExtension(file.getOriginalFilename());

        String safeDocumentType = documentType.toLowerCase();

        Path documentDirectory =
                storageRoot.resolve(
                        "verification"
                ).resolve(
                        safeDocumentType
                ).normalize();

        try {

            Files.createDirectories(documentDirectory);

            String storedFileName = documentId + extension;

            Path target = documentDirectory.resolve(storedFileName).normalize();

            if (!target.startsWith(documentDirectory)) {

                log.error(
                        "Invalid document storage path generated. documentId={}",
                        documentId
                );

                throw new IllegalStateException("Invalid document storage path.");
            }

            Files.copy(
                    file.getInputStream(),
                    target
            );

            String storageReference = storageRoot
                            .relativize(target)
                            .toString()
                            .replace("\\", "/");

            log.info(
                    "Verification document stored successfully. documentId={}, documentType={}, size={}",
                    documentId,
                    documentType,
                    file.getSize()
            );

            return storageReference;

        } catch (IOException exception) {

            log.error(
                    "Failed to store verification document. documentId={}, documentType={}",
                    documentId,
                    documentType,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to store verification document.",
                    exception
            );
        }
    }

    /**
     * Deletes the document from local storage based on the provided storage reference.
     *
     * @param storageReference The relative path to the document to be deleted.
     */
    @Override
    public void delete(String storageReference) {

        if (storageReference == null || storageReference.isBlank()) {
            return;
        }

        Path target = storageRoot.resolve(storageReference).normalize();

        if (!target.startsWith(storageRoot)) {

            log.warn("Rejected invalid document deletion path.");
            return;
        }

        try {

            boolean deleted = Files.deleteIfExists(target);

            if (deleted) {

                log.info("Verification document deleted from storage. reference={}", storageReference);
            }

        } catch (IOException exception) {

            log.error(
                    "Failed to delete verification document from storage. reference={}",
                    storageReference,
                    exception
            );
        }
    }

    /**
     * Extracts the file extension from the original filename and validates it against supported types.
     *
     * @param originalFilename The original filename of the uploaded document.
     * @return The file extension (including the dot) in lowercase.
     * @throws IllegalArgumentException If the file type is unsupported or if the filename is invalid.
     */
    private String getFileExtension(String originalFilename) {

        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!extension.matches("\\.(pdf|png|jpg|jpeg)$")) {

            throw new IllegalArgumentException("Unsupported document file type.");
        }
        return extension;
    }
}