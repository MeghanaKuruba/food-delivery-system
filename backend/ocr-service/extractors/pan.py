import re

from ocr.normalizer import (
    normalize_lines,
    value_after_label
)


def extract(texts):

    lines = normalize_lines(texts)

    pan_number = None

    # Standard PAN pattern
    for line in lines:

        match = re.search(
            r"\b([A-Z]{5}[0-9]{4}[A-Z])\b",
            line.upper()
        )

        if match:
            pan_number = match.group(1)
            break

    name = value_after_label(
        lines,
        [
            "Name",
            "/Name"
        ]
    )

    father_name = value_after_label(
        lines,
        [
            "Father's Name",
            "Father Name",
            "/Father's Name"
        ]
    )

    date_of_birth = value_after_label(
        lines,
        [
            "Date of Birth",
            "Date Of Birth",
            "DOB",
            "/Date of Birth"
        ]
    )

    return {
        "panNumber": pan_number,
        "name": name,
        "fatherName": father_name,
        "dateOfBirth": date_of_birth
    }