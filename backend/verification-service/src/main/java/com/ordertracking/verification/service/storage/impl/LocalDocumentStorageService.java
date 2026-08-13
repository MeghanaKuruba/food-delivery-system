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

    @Override
    public String store(
            MultipartFile file,
            String documentId,
            String documentType) {

        String extension =
                getFileExtension(
                        file.getOriginalFilename()
                );

        String safeDocumentType =
                documentType.toLowerCase();

        Path documentDirectory =
                storageRoot.resolve(
                        "verification"
                ).resolve(
                        safeDocumentType
                ).normalize();

        try {

            Files.createDirectories(documentDirectory);

            String storedFileName =
                    documentId + extension;

            Path target =
                    documentDirectory
                            .resolve(storedFileName)
                            .normalize();

            if (!target.startsWith(documentDirectory)) {

                log.error(
                        "Invalid document storage path generated. documentId={}",
                        documentId
                );

                throw new IllegalStateException(
                        "Invalid document storage path."
                );
            }

            Files.copy(
                    file.getInputStream(),
                    target
            );

            String storageReference =
                    storageRoot
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

    @Override
    public void delete(String storageReference) {

        if (storageReference == null ||
                storageReference.isBlank()) {
            return;
        }

        Path target =
                storageRoot
                        .resolve(storageReference)
                        .normalize();

        if (!target.startsWith(storageRoot)) {

            log.warn(
                    "Rejected invalid document deletion path."
            );

            return;
        }

        try {

            boolean deleted =
                    Files.deleteIfExists(target);

            if (deleted) {

                log.info(
                        "Verification document deleted from storage. reference={}",
                        storageReference
                );
            }

        } catch (IOException exception) {

            log.error(
                    "Failed to delete verification document from storage. reference={}",
                    storageReference,
                    exception
            );
        }
    }

    private String getFileExtension(
            String originalFilename) {

        if (originalFilename == null ||
                !originalFilename.contains(".")) {

            return "";
        }

        String extension =
                originalFilename.substring(
                        originalFilename.lastIndexOf(".")
                ).toLowerCase();

        if (!extension.matches(
                "\\.(pdf|png|jpg|jpeg)$")) {

            throw new IllegalArgumentException(
                    "Unsupported document file type."
            );
        }

        return extension;
    }
}