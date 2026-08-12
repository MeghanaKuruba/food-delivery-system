package com.ordertracking.verification.mapper;

import com.ordertracking.verification.dto.SubmitVerificationDocumentRequest;
import com.ordertracking.verification.dto.VerificationApplicationResponse;
import com.ordertracking.verification.dto.VerificationDocumentResponse;
import com.ordertracking.verification.entity.VerificationApplication;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VerificationMapper {

    public VerificationApplicationResponse toVerificationAppResponse(
            VerificationApplication application) {

        return VerificationApplicationResponse.builder()
                .id(application.getId())
                .authUserId(application.getAuthUserId())
                .applicantType(application.getApplicantType())
                .referenceId(application.getReferenceId())
                .status(application.getStatus())
                .reason(application.getReason())
                .submittedAt(application.getSubmittedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    public VerificationDocumentResponse toVerificationDocumentResponse(
            VerificationDocument document) {

        return VerificationDocumentResponse.builder()
                .documentId(document.getDocumentId())
                .referenceId(
                        document.getVerificationApplication()
                                .getReferenceId()
                )
                .documentType(document.getDocumentType())
                .documentNumber(document.getDocumentNumber())
                .documentUrl(document.getDocumentUrl())
                .issuedAt(document.getIssuedAt())
                .expiryDate(document.getExpiryDate())
                .status(document.getStatus())
                .rejectionReason(document.getRejectionReason())
                .uploadedAt(document.getUploadedAt())
                .verifiedAt(document.getVerifiedAt())
                .build();
    }

    public VerificationDocument toVerificationDocument(
            SubmitVerificationDocumentRequest request,
            VerificationApplication application) {

        return VerificationDocument.builder()
                .documentId(generateDocumentId(request.getDocumentType()))
                .verificationApplication(application)
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber())
                .documentUrl(request.getDocumentUrl())
                .issuedAt(request.getIssuedAt())
                .expiryDate(request.getExpiryDate())
                .build();
    }

    public String generateReferenceId() {

        return "VER-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();
    }

    public String generateDocumentId(DocumentType documentType) {

        String uniquePart =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 6)
                        .toUpperCase();

        return getDocumentPrefix(documentType)
                + "-"
                + uniquePart;
    }

    private String getDocumentPrefix(DocumentType documentType) {

        return switch (documentType) {

            case PAN -> "PAN";

            case DRIVING_LICENSE -> "DL";

            case VEHICLE_RC -> "RC";

            case VEHICLE_INSURANCE -> "INS";

            case GST -> "GST";

            case FSSAI -> "FSSAI";

            case BUSINESS_REGISTRATION -> "BR";

            case OTHER -> "DOC";
        };
    }
}