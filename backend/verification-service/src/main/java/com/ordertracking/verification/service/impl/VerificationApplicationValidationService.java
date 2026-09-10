package com.ordertracking.verification.service;

import com.ordertracking.verification.config.VerificationDocumentRequirement;
import com.ordertracking.verification.entity.VerificationApplication;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.ApplicantType;
import com.ordertracking.verification.enums.DocumentType;
import com.ordertracking.verification.enums.VerificationStatus;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VerificationApplicationValidationService {

    public void validateRequiredDocuments(
            VerificationApplication application) {

        Set<DocumentType> requiredDocuments =
                VerificationDocumentRequirement.getRequiredDocuments(
                        application.getApplicantType()
                );

        Set<DocumentType> submittedDocuments =
                application.getDocuments()
                        .stream()
                        .map(VerificationDocument::getDocumentType)
                        .collect(Collectors.toSet());

        Set<DocumentType> missingDocuments =
                EnumSet.copyOf(requiredDocuments);

        missingDocuments.removeAll(submittedDocuments);

        if (!missingDocuments.isEmpty()) {
            throw new IllegalStateException(
                    "Required verification documents are missing: "
                            + missingDocuments
            );
        }
    }

    public void validateDocumentAllowedForApplicant(
            VerificationApplication application,
            DocumentType documentType) {

        Set<DocumentType> allowedDocuments = VerificationDocumentRequirement.getRequiredDocuments(application.getApplicantType());

        if (!allowedDocuments.contains(documentType)) {

            throw new IllegalStateException(
                    "Document type "
                            + documentType
                            + " is not required for applicant type "
                            + application.getApplicantType()
            );
        }
    }

    public void validateApplicationCanBeSubmitted(
            VerificationApplication application) {

        if (application.getStatus() != VerificationStatus.PENDING) {

            throw new IllegalStateException(
                    "Verification application cannot be submitted in its current status."
            );
        }
    }
}