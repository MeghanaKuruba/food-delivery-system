package com.ordertracking.verification.service;

import com.ordertracking.verification.dto.PanDataDetailsRequest;
import com.ordertracking.verification.dto.PanDataDetailsResponse;

public interface VerificationDocumentDataService {

    PanDataDetailsResponse addPanData(String documentId, PanDataDetailsRequest request);

    PanDataDetailsResponse getPanData(String documentId);
}