package com.ordertracking.verification.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanDataDetailsResponse {

    private String documentId;
    private String panNumber;
    private String holderName;
}