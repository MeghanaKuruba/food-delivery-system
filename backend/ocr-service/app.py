from fastapi import FastAPI, File, UploadFile, Form, HTTPException
from ocr.engine import OcrEngine
from ocr.extractor import extract_fields

import tempfile
import os


app = FastAPI(
    title="Food Delivery OCR Service",
    description="Document OCR service for verification",
    version="1.0.0"
)


ocr_engine = OcrEngine()


SUPPORTED_DOCUMENT_TYPES = {
    "PAN",
    "GST",
    "FSSAI",
    "BUSINESS_REGISTRATION",
    "DRIVING_LICENSE",
    "RC",
    "INSURANCE"
}


@app.get("/health")
def health_check():

    return {
        "status": "UP",
        "service": "ocr-service"
    }


@app.post("/api/v1/ocr/extract")
async def extract_document(
        document_type: str = Form(...),
        file: UploadFile = File(...)
):

    document_type = document_type.upper()

    if document_type not in SUPPORTED_DOCUMENT_TYPES:
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported document type: {document_type}"
        )

    allowed_types = {
        "image/jpeg",
        "image/png",
        "image/jpg"
    }

    if file.content_type not in allowed_types:
        raise HTTPException(
            status_code=400,
            detail="Only JPG and PNG images are supported"
        )

    file_bytes = await file.read()

    if not file_bytes:
        raise HTTPException(
            status_code=400,
            detail="Uploaded file is empty"
        )

    temp_path = None

    try:

        suffix = os.path.splitext(
            file.filename
        )[1]

        with tempfile.NamedTemporaryFile(
                delete=False,
                suffix=suffix
        ) as temp_file:

            temp_file.write(file_bytes)
            temp_path = temp_file.name

        texts = ocr_engine.extract_text(temp_path)

        extracted_fields = extract_fields(
            document_type,
            texts
        )

        return {
            "success": True,
            "documentType": document_type,
            "data": extracted_fields,
            "ocrText": texts
        }

    except ValueError as error:

        raise HTTPException(
            status_code=400,
            detail=str(error)
        )

    finally:

        if temp_path and os.path.exists(temp_path):
            os.remove(temp_path)