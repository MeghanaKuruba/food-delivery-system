import re


DATE_PATTERN = re.compile(
    r"\b\d{2}[-/]\d{2}[-/]\d{4}\b"
)

DL_PATTERN = re.compile(
    r"\b[A-Z]{2}\d{2}\s*\d{4}\s*\d{7}\b",
    re.IGNORECASE
)


def get_value(texts, index):

    text = texts[index]["text"].strip()

    if ":" in text:

        value = text.split(":", 1)[1].strip()

        if value:
            return value

    if index + 1 < len(texts):
        return texts[index + 1]["text"].strip()

    return None


def extract(texts):

    dl_number = None
    name = None
    date_of_birth = None
    address = None
    valid_from = None
    valid_to = None
    vehicle_classes = []

    for index, item in enumerate(texts):

        text = item["text"].strip()
        upper_text = text.upper()

        match = DL_PATTERN.search(text)

        if match:
            dl_number = match.group()

        if "NAME" in upper_text and "DL NO" not in upper_text:
            name = get_value(texts, index)

        if "D.O.B" in upper_text or "DOB" in upper_text:

            match = DATE_PATTERN.search(text)

            if match:
                date_of_birth = match.group()

            elif index + 1 < len(texts):

                next_text = texts[index + 1]["text"]

                match = DATE_PATTERN.search(next_text)

                if match:
                    date_of_birth = match.group()

        if upper_text.startswith("ADDRESS"):

            address = get_value(texts, index)

        if "DATE OF ISSUE" in upper_text:

            match = DATE_PATTERN.search(text)

            if match:
                valid_from = match.group()

            elif index + 1 < len(texts):

                match = DATE_PATTERN.search(
                    texts[index + 1]["text"]
                )

                if match:
                    valid_from = match.group()

        if "VALID TILL" in upper_text:

            match = DATE_PATTERN.search(text)

            if match:
                valid_to = match.group()

            elif index + 1 < len(texts):

                match = DATE_PATTERN.search(
                    texts[index + 1]["text"]
                )

                if match:
                    valid_to = match.group()

        if upper_text in {
            "LMV",
            "MCWG",
            "MCWOG",
            "HMV",
            "HGV"
        }:
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