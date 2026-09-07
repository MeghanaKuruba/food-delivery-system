package com.ordertracking.verification.config;

import com.ordertracking.verification.enums.ApplicantType;
import com.ordertracking.verification.enums.DocumentType;

import java.util.EnumSet;
import java.util.Set;

public final class VerificationDocumentRequirement {

    private VerificationDocumentRequirement() {
    }

    public static Set<DocumentType> getRequiredDocuments(ApplicantType applicantType) {

        return switch (applicantType) {

            case RESTAURANT_OWNER -> EnumSet.of(
                    DocumentType.PAN,
                    DocumentType.BUSINESS_REGISTRATION,
                    DocumentType.GST,
                    DocumentType.FSSAI
            );

            case DELIVERY_PARTNER -> EnumSet.of(
                    DocumentType.PAN,
                    DocumentType.DRIVING_LICENSE,
                    DocumentType.VEHICLE_RC,
                    DocumentType.VEHICLE_INSURANCE
            );
        };
    }
}