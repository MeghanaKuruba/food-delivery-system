package com.ordertracking.verification.entity;

import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "verification_documents",
        indexes = {
                @Index(
                        name = "idx_verification_document_application",
                        columnList = "verification_application_id"
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
    @Column(
            name = "document_id",
            nullable = false,
            unique = true,
            length = 20
    )
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

    /**
     * Reference to the stored document.
     *
     * This is a storage reference, not the actual file contents.
     */
    @Column(
            name = "document_url",
            length = 500
    )
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @Column(length = 500)
    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}