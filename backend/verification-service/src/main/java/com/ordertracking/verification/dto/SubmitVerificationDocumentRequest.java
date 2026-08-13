package com.ordertracking.verification.dto;

import com.ordertracking.verification.enums.DocumentScope;
import com.ordertracking.verification.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitVerificationDocumentRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotNull
    private DocumentScope documentScope;

    private LocalDate issuedAt;

    private LocalDate expiryDate;

    @NotNull
    private MultipartFile document;
}