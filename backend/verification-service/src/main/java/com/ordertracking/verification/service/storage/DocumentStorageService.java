package com.ordertracking.verification.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface DocumentStorageService {

    String store(MultipartFile file, String documentId, String documentType);

    void delete(String storageReference);

    InputStream load(String storageReference);
}