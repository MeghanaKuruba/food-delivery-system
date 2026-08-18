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
public class UpdateVerificationDocumentRequest {

    @NotBlank
    private String documentNumber;

    @NotNull
    private DocumentScope documentScope;

    private LocalDate issuedAt;

    private LocalDate expiryDate;
}