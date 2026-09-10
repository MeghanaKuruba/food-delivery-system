package com.ordertracking.verification.service;

import com.ordertracking.verification.dto.CreateVerificationApplicationRequest;
import com.ordertracking.verification.dto.VerificationApplicationResponse;

import java.util.List;

public interface VerificationService {

    VerificationApplicationResponse createApplication(CreateVerificationApplicationRequest request);

    VerificationApplicationResponse getApplication(Long applicationId);

    List<VerificationApplicationResponse> getApplicationsByUser(Long authUserId);

    VerificationApplicationResponse submitApplication(Long applicationId);
}