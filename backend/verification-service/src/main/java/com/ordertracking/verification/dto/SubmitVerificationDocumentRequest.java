package com.ordertracking.verification.dto;

import com.ordertracking.verification.enums.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitVerificationDocumentRequest {

    @NotNull
    private DocumentType documentType;

    @NotNull
    private MultipartFile document;
}