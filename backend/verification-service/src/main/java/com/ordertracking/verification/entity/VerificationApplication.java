package com.ordertracking.verification.entity;

import com.ordertracking.verification.enums.ApplicantType;
import com.ordertracking.verification.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "verification_application",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_verification_reference_id",
                        columnNames = "reference_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User ID from Auth Service.
     */
    @Column(nullable = false)
    private Long authUserId;

    /**
     * Type of applicant being verified.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicantType applicantType;

    /**
     * ID of the entity being verified.
     *
     * RESTAURANT_OWNER -> Restaurant reference
     * DELIVERY_PARTNER -> Delivery partner reference
     */
    @Column(
            name = "reference_id",
            nullable = false,
            unique = true,
            updatable = false,
            length = 50
    )
    private String referenceId;

    /**
     * Overall verification application status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private VerificationStatus status = VerificationStatus.PENDING;

    /**
     * Overall reason when action/rejection/manual review is required.
     */
    @Column(length = 500)
    private String reason;

    /**
     * Time at which the application was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    /**
     * Last time the application was updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Documents belonging to this verification application.
     */
    @Builder.Default
    @OneToMany(
            mappedBy = "verificationApplication",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<VerificationDocument> documents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        submittedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}