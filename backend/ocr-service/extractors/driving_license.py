import re


DATE_PATTERN = re.compile(
    r"\b\d{2}[-/]\d{2}[-/]\d{4}\b"
)

DL_PATTERN = re.compile(
    r"\b[A-Z]{2}\d{2}\s*\d{4}\s*\d{7}\b",
    re.IGNORECASE
)


def extract(texts):

    dl_number = None
    name = None
    date_of_birth = None
    address = None
    valid_from = None
    valid_to = None
    vehicle_classes = []

    for item in texts:

        text = item["text"].strip()
        upper_text = text.upper()

        # DL number
        if "DL NO" in upper_text:
            match = DL_PATTERN.search(text)

            if match:
                dl_number = match.group()

        # Name
        if upper_text.startswith("NAME:"):
            name = text.split(":", 1)[1].strip()

        # Date of birth
        if "D.O.B." in upper_text or "DOB" in upper_text:

            match = DATE_PATTERN.search(text)

            if match:
                date_of_birth = match.group()

        # Address
        if upper_text.startswith("ADDRESS:"):
            address = text.split(":", 1)[1].strip()

        # Issue date
        if "DATE OF ISSUE:" in upper_text:

            match = DATE_PATTERN.search(text)

            if match:
                valid_from = match.group()

        # Valid till
        if "VALID TILL:" in upper_text:

            match = DATE_PATTERN.search(text)

            if match:
                valid_to = match.group()

        # Vehicle category
        if upper_text in {"LMV", "MCWG", "MCWOG", "HMV", "HGV"}:
            vehicle_classes.append(text)

    return {
        "dlNumber": dl_number,
        "name": name,
        "dateOfBirth": date_of_birth,
        "address": address,
        "validFrom": valid_from,
        "validTo": valid_to,
        "vehicleClasses": vehicle_classes
    }