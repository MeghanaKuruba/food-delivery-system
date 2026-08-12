package com.ordertracking.verification.dto;

import com.ordertracking.verification.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVerificationDocumentRequest {

    @NotNull
    private DocumentType documentType;

    @NotBlank
    private String documentNumber;

    @NotBlank
    private String documentUrl;

    private LocalDate issuedAt;

    private LocalDate expiryDate;
}