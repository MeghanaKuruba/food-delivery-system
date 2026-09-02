package com.ordertracking.verification.service;

import com.ordertracking.verification.dto.VerificationResult;
import com.ordertracking.verification.entity.VerificationDocument;

public interface VerificationEngine {

    VerificationResult verify(VerificationDocument document);
}