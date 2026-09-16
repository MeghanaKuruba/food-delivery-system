package com.ordertracking.verification.service.ocr;

import com.ordertracking.verification.dto.OcrResult;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.service.OcrService;
import com.ordertracking.verification.service.storage.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

import static org.bytedeco.leptonica.global.leptonica.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TesseractOcrServiceImpl implements OcrService {

    // OCR is the process of extracting text from an image or scanned document.
    // Tesseract is an open-source OCR engine that can recognize text in various languages and formats.
    // It is widely used for document processing, data extraction, and text recognition tasks.
    // In this implementation, we use Tesseract to extract text from verification documents.
    // The extracted text can then be used for further processing, such as verification or validation.
    // The Tesseract OCR engine requires a trained data file for the specified language to perform text recognition.
    // The trained data files are typically stored in a "tessdata" directory, which should be included in the application's resources.
    // Tesseract is an OCR engine that can recognize text from images.

    private final DocumentStorageService documentStorageService;

    @Value("${verification.ocr.language:eng}")
    private String language;

    /**
     * Extracts text from the provided verification document using Tesseract OCR.
     *
     * @param document The verification document from which to extract text.
     * @return An OcrResult containing the extracted text or an error message if extraction fails.
     */
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
                    new File("backend/verification-service/tessdata").getAbsolutePath();

            log.info("Tesseract tessdata path: {}", tessDataPath);

            int initResult = api.Init(tessDataPath, language);

            if (initResult != 0) {

                log.error(
                        "Failed to initialize Tesseract OCR. documentId={}, initResult={}, language={}",
                        document.getDocumentId(),
                        initResult,
                        language
                );

                return OcrResult.failure("Unable to initialize OCR engine.");
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

                return OcrResult.failure("Unable to decode document image.");
            }

            api.SetPageSegMode(6);

            PIX grayscaleImage = null;
            PIX binaryImage = null;

            try {
                // Convert the color document into grayscale.
                grayscaleImage = pixConvertRGBToGray(
                        image,
                        0.3f,
                        0.4f,
                        0.3f
                );

                if (grayscaleImage == null || grayscaleImage.isNull()) {
                    log.warn(
                            "OCR grayscale preprocessing failed. documentId={}",
                            document.getDocumentId()
                    );
                    return OcrResult.failure("Unable to preprocess document image.");
                }

                // Convert grayscale image into a black-and-white image
                // using adaptive thresholding.
                binaryImage = pixAdaptThresholdToBinary(
                        grayscaleImage,
                        null,
                        1.0f
                );

                if (binaryImage == null || binaryImage.isNull()) {
                    log.warn(
                            "OCR threshold preprocessing failed. documentId={}",
                            document.getDocumentId()
                    );
                    return OcrResult.failure("Unable to threshold document image.");
                }

                api.SetImage(binaryImage);

                text = api.GetUTF8Text();

            } finally {

                if (binaryImage != null && !binaryImage.isNull()) {
                    pixDestroy(binaryImage);
                }

                if (grayscaleImage != null && !grayscaleImage.isNull()) {
                    pixDestroy(grayscaleImage);
                }
            }
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