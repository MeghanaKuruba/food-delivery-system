package com.ordertracking.verification.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {

    String store(MultipartFile file, String documentId, String documentType);

    void delete(String storageReference);
}