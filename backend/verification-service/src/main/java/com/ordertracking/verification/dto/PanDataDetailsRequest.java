package com.ordertracking.verification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanDataDetailsRequest {

    @NotBlank
    @Pattern(
            regexp = "[A-Z]{5}[0-9]{4}[A-Z]",
            message = "Invalid PAN format"
    )
    private String panNumber;

    @NotBlank
    private String holderName;
}