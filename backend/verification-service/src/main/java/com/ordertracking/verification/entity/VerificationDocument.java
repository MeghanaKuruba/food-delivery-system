package com.ordertracking.verification.entity;

import com.ordertracking.verification.enums.DocumentScope;
import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "verification_documents",
        indexes = {
                @Index(
                        name = "idx_verification_document_application",
                        columnList = "verification_application_id"
                ),
                @Index(
                        name = "idx_verification_document_number",
                        columnList = "document_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationDocument {

    @Id
    @Column(name = "document_id", nullable = false, unique = true, length = 20)
    private String documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "verification_application_id",
            nullable = false
    )
    private VerificationApplication verificationApplication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DocumentType documentType;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    /**
     * Temporary reference to the uploaded document.
     *
     * For now this can be a local/mock path.
     * Later this will point to object storage.
     */
    @Column(name = "document_url", length = 500)
    private String documentUrl;

    private LocalDate issuedAt;

    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @Column(length = 500)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentScope documentScope;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}