package com.ordertracking.verification.service.ocr;

import org.bytedeco.leptonica.PIX;

public interface ImagePreprocessor {

    PIX preprocess(PIX image);
}