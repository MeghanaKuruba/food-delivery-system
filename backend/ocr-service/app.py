from fastapi import FastAPI, File, UploadFile, HTTPException
from paddleocr import PaddleOCR
import re
import tempfile
import os


app = FastAPI(
    title="OCR Service",
    description="Document OCR service for the food delivery verification system",
    version="1.0.0"
)


ocr = PaddleOCR(
    lang="en",
    use_doc_orientation_classify=False,
    use_doc_unwarping=False,
    use_textline_orientation=False
)


def run_ocr(image_path):
    result = ocr.predict(image_path)

    texts = []

    for res in result:
        for text, score in zip(res["rec_texts"], res["rec_scores"]):
            text = text.strip()

            if text:
                texts.append({
                    "text": text,
                    "confidence": float(score)
                })

    return texts


def extract_pan_fields(texts):
    pan_number = None
    name = None
    father_name = None
    date_of_birth = None

    pan_pattern = re.compile(
        r"\b[A-Z]{5}[0-9]{4}[A-Z]\b"
    )

    dob_pattern = re.compile(
        r"\b\d{2}/\d{2}/\d{4}\b"
    )

    for index, item in enumerate(texts):

        text = item["text"]
        upper_text = text.upper()

        # PAN number
        pan_match = pan_pattern.search(upper_text)

        if pan_match:
            pan_number = pan_match.group()

        # Date of birth
        dob_match = dob_pattern.search(text)

        if dob_match:
            date_of_birth = dob_match.group()

        # Name
        if "NAME" in upper_text and "FATHER" not in upper_text:
            if index + 1 < len(texts):
                name = texts[index + 1]["text"]

        # Father's name
        if "FATHER'S NAME" in upper_text:
            if index + 1 < len(texts):
                father_name = texts[index + 1]["text"]

    return {
        "documentType": "PAN",
        "panNumber": pan_number,
        "name": name,
        "fatherName": father_name,
        "dateOfBirth": date_of_birth
    }


@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": "ocr-service"
    }


@app.post("/api/v1/ocr/pan")
async def extract_pan(file: UploadFile = File(...)):

    if not file.filename:
        raise HTTPException(
            status_code=400,
            detail="File name is required"
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
        with tempfile.NamedTemporaryFile(
            delete=False,
            suffix=os.path.splitext(file.filename)[1]
        ) as temp_file:

            temp_file.write(file_bytes)
            temp_path = temp_file.name

        texts = run_ocr(temp_path)

        extracted_data = extract_pan_fields(texts)

        return {
            "success": True,
            "documentType": "PAN",
            "data": extracted_data,
            "ocrText": texts
        }

    finally:
        if temp_path and os.path.exists(temp_path):
            os.remove(temp_path)