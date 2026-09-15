package com.ordertracking.verification.service;

import com.ordertracking.verification.dto.OcrResult;
import com.ordertracking.verification.entity.VerificationDocument;

public interface OcrService {

    OcrResult extractText(VerificationDocument document);
}