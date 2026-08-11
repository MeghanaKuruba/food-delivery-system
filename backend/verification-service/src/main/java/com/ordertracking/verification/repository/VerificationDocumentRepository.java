package com.ordertracking.verification.repository;

import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, String> {

    List<VerificationDocument> findByVerificationApplicationId(Long applicationId);

    List<VerificationDocument> findByStatus(DocumentStatus status);

    boolean existsByDocumentNumberIgnoreCase(String documentNumber);
}