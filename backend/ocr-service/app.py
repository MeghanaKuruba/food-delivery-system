from fastapi import (
    FastAPI,
    File,
    UploadFile,
    Form,
    HTTPException
)

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
    "DRIVING_LICENSE",
    "RC",
    "INSURANCE"
}


ALLOWED_CONTENT_TYPES = {
    "image/jpeg",
    "image/png",
    "image/jpg"
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

    document_type = document_type.upper().strip()

    if document_type not in SUPPORTED_DOCUMENT_TYPES:

        raise HTTPException(
            status_code=400,
            detail=(
                f"Unsupported document type: "
                f"{document_type}. "
                f"Supported types: "
                f"{sorted(SUPPORTED_DOCUMENT_TYPES)}"
            )
        )

    if file.content_type not in ALLOWED_CONTENT_TYPES:

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
            file.filename or ".png"
        )[1]

        if not suffix:
            suffix = ".png"

        with tempfile.NamedTemporaryFile(
                delete=False,
                suffix=suffix
        ) as temp_file:

            temp_file.write(file_bytes)
            temp_path = temp_file.name

        texts = ocr_engine.extract_text(
            temp_path
        )

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

    except Exception as error:

        raise HTTPException(
            status_code=500,
            detail=f"OCR processing failed: {str(error)}"
        )

    finally:

        if (
                temp_path
                and os.path.exists(temp_path)
        ):
            os.remove(temp_path)