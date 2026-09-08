package com.ordertracking.verification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVerificationDocumentRequest {

    @NotBlank
    private String documentNumber;

    private LocalDate issuedAt;

    private LocalDate expiryDate;
}