package com.ordertracking.verification.service.ocr;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.leptonica.PIX;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BasicImagePreprocessor implements ImagePreprocessor {

    @Override
    public PIX preprocess(PIX image) {

        if (image == null || image.isNull()) {
            throw new IllegalArgumentException(
                    "Image cannot be null."
            );
        }

        log.debug("Image preprocessing started.");

        /*
         * For the first iteration we keep the original image.
         *
         * The preprocessing pipeline will be expanded
         * after comparing OCR results.
         */
        return image;
    }
}