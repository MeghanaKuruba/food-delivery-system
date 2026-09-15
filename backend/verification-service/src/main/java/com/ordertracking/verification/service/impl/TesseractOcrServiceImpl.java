package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.dto.OcrResult;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentType;
import com.ordertracking.verification.service.OcrService;
import com.ordertracking.verification.service.storage.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import org.bytedeco.tesseract.global.tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.bytedeco.leptonica.global.leptonica.pixDestroy;
import static org.bytedeco.leptonica.global.leptonica.pixReadMem;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TesseractOcrServiceImpl
        implements OcrService {

    private final DocumentStorageService documentStorageService;

    @Value("${verification.ocr.language:eng}")
    private String language;

    @Override
    public OcrResult extractText(VerificationDocument document) {

        log.info(
                "Starting OCR extraction. documentId={}, documentType={}",
                document.getDocumentId(),
                document.getDocumentType()
        );

        if (document.getDocumentUrl() == null || document.getDocumentUrl().isBlank()) {

            log.warn(
                    "OCR skipped because document storage reference is missing. documentId={}",
                    document.getDocumentId()
            );

            return OcrResult.failure(
                    "Document storage reference is missing."
            );
        }

        /*
         * PDF OCR will be handled separately.
         * Today's implementation handles image documents.
         */
        if (isPdf(document)) {

            log.info(
                    "OCR skipped for PDF document. PDF OCR processing will be added separately. documentId={}",
                    document.getDocumentId()
            );

            return OcrResult.failure(
                    "PDF OCR processing is not implemented yet."
            );
        }

        TessBaseAPI api = new TessBaseAPI();
        PIX image = null;
        BytePointer text = null;

        try {

            String tessDataPath =
                    new java.io.File("backend/verification-service/tessdata").getAbsolutePath();

            log.info("Tesseract tessdata path: {}", tessDataPath);

            int initResult = api.Init(tessDataPath, language);

            if (initResult != 0) {

                log.error(
                        "Failed to initialize Tesseract OCR. documentId={}, initResult={}, language={}",
                        document.getDocumentId(),
                        initResult,
                        language
                );

                return OcrResult.failure(
                        "Unable to initialize OCR engine."
                );
            }

            try (InputStream inputStream = documentStorageService.load(document.getDocumentUrl())) {

                byte[] imageBytes = inputStream.readAllBytes();

                image = pixReadMem(imageBytes, imageBytes.length);
            }

            if (image == null || image.isNull()) {

                log.warn(
                        "OCR could not decode document image. documentId={}",
                        document.getDocumentId()
                );

                return OcrResult.failure(
                        "Unable to decode document image."
                );
            }

            api.SetImage(image);

            text = api.GetUTF8Text();

            if (text == null || text.isNull()) {

                log.warn(
                        "OCR returned no text. documentId={}",
                        document.getDocumentId()
                );

                return OcrResult.failure(
                        "No text could be extracted from document."
                );
            }

            String extractedText = text.getString().trim();

            if (extractedText.isBlank()) {

                log.warn(
                        "OCR returned blank text. documentId={}",
                        document.getDocumentId()
                );

                return OcrResult.failure(
                        "No text could be extracted from document."
                );
            }

            log.info(
                    "OCR extracted text for documentId={}:\n{}",
                    document.getDocumentId(),
                    extractedText
            );

            log.info(
                    "OCR extraction completed. documentId={}, characterCount={}",
                    document.getDocumentId(),
                    extractedText.length()
            );

            return OcrResult.success(extractedText);

        } catch (Exception exception) {

            log.error(
                    "OCR extraction failed. documentId={}",
                    document.getDocumentId(),
                    exception
            );

            return OcrResult.failure(
                    "OCR extraction failed."
            );

        } finally {

            if (text != null) {
                text.deallocate();
            }

            if (image != null && !image.isNull()) {
                pixDestroy(image);
            }

            api.End();
        }
    }

    private boolean isPdf(
            VerificationDocument document) {

        String reference =
                document.getDocumentUrl()
                        .toLowerCase();

        return reference.endsWith(".pdf");
    }
}