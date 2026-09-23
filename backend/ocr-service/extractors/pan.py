import re


PAN_PATTERN = re.compile(
    r"\b[A-Z]{5}[0-9]{4}[A-Z]\b"
)

DATE_PATTERN = re.compile(
    r"\b\d{2}[/-]\d{2}[/-]\d{4}\b"
)


def extract(texts):

    pan_number = None
    name = None
    father_name = None
    date_of_birth = None

    for index, item in enumerate(texts):

        text = item["text"].strip()
        upper_text = text.upper()

        # PAN number
        pan_match = PAN_PATTERN.search(upper_text)

        if pan_match:
            pan_number = pan_match.group()

        # Date of birth
        date_match = DATE_PATTERN.search(text)

        if date_match:
            date_of_birth = date_match.group()

        # Name
        if "NAME" in upper_text and "FATHER" not in upper_text:

            value = text.split(":", 1)[1].strip() if ":" in text else ""

            if value:
                name = value

            elif index + 1 < len(texts):
                name = texts[index + 1]["text"].strip()

        # Father's name
        if "FATHER" in upper_text and "NAME" in upper_text:

            value = text.split(":", 1)[1].strip() if ":" in text else ""

            if value:
                father_name = value

            elif index + 1 < len(texts):
                father_name = texts[index + 1]["text"].strip()

    return {
        "panNumber": pan_number,
        "name": name,
        "fatherName": father_name,
        "dateOfBirth": date_of_birth
    }