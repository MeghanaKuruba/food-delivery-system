package com.ordertracking.verification.repository;

import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, String> {

    List<VerificationDocument> findByVerificationApplicationId(Long applicationId);

    boolean existsByVerificationApplicationIdAndDocumentType(Long applicationId, DocumentType documentType);
}