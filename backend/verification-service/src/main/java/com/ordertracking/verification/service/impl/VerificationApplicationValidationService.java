package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.config.VerificationDocumentRequirement;
import com.ordertracking.verification.entity.VerificationApplication;
import com.ordertracking.verification.enums.DocumentType;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VerificationApplicationValidationService {

    public void validateRequiredDocuments(VerificationApplication application) {

        Set<DocumentType> requiredDocuments = VerificationDocumentRequirement.getRequiredDocuments(application.getApplicantType());

        Set<DocumentType> submittedDocuments =
                application.getDocuments()
                        .stream()
                        .map(document -> document.getDocumentType())
                        .collect(Collectors.toSet());

        Set<DocumentType> missingDocuments = EnumSet.copyOf(requiredDocuments);

        missingDocuments.removeAll(submittedDocuments);

        if (!missingDocuments.isEmpty()) {

            throw new IllegalStateException("Required verification documents are missing: " + missingDocuments);
        }
    }
}
