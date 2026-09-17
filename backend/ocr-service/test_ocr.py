from paddleocr import PaddleOCR
import re

ocr = PaddleOCR(
    lang="en",
    use_doc_orientation_classify=False,
    use_doc_unwarping=False,
    use_textline_orientation=False
)

result = ocr.predict("test-document.jpg")

texts = []

for res in result:
    for text, score in zip(res["rec_texts"], res["rec_scores"]):
        if text.strip():
            texts.append({
                "text": text.strip(),
                "confidence": float(score)
            })


def extract_pan(texts):
    pan_number = None
    name = None
    father_name = None
    date_of_birth = None

    # PAN format: 5 letters + 4 digits + 1 letter
    pan_pattern = re.compile(r"\b[A-Z]{5}[0-9]{4}[A-Z]\b")

    # Date format: DD/MM/YYYY
    dob_pattern = re.compile(r"\b\d{2}/\d{2}/\d{4}\b")

    for i, item in enumerate(texts):
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
        if "/NAME" in upper_text or upper_text == "NAME":
            if i + 1 < len(texts):
                name = texts[i + 1]["text"]

        # Father's name
        if "FATHER'S NAME" in upper_text or "/FATHER'S NAME" in upper_text:
            if i + 1 < len(texts):
                father_name = texts[i + 1]["text"]

    return {
        "documentType": "PAN",
        "panNumber": pan_number,
        "name": name,
        "fatherName": father_name,
        "dateOfBirth": date_of_birth
    }


pan_data = extract_pan(texts)

print("\n--- EXTRACTED PAN DATA ---")
for key, value in pan_data.items():
    print(f"{key}: {value}")