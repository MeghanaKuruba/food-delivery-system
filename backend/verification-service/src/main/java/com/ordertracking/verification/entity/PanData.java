package com.ordertracking.verification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "verification_pan_documents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_verification_pan_number",
                        columnNames = "pan_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanData {

    @Id
    @Column(name = "document_id", nullable = false, unique = true, length = 20)
    private String documentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_id",
            referencedColumnName = "document_id",
            nullable = false
    )
    @MapsId
    private VerificationDocument verificationDocument;

    @Column(
            name = "pan_number",
            nullable = false,
            unique = true,
            length = 10
    )
    private String panNumber;

    @Column(length = 100)
    private String holderName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}