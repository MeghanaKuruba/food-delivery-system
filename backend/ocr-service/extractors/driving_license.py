import re

from ocr.normalizer import (
    normalize_lines,
    value_after_label,
    multiline_value_after_label,
    clean_identifier
)


DATE_PATTERN = (
    r"\b\d{1,2}[/-]\d{1,2}[/-]\d{2,4}\b"
)


def extract(texts):

    lines = normalize_lines(texts)

    # --------------------------------------------------
    # DL NUMBER
    # --------------------------------------------------

    dl_number = None

    for line in lines:

        # Indian DL examples:
        # DL-0420110149646
        # DL0420110149646

        matches = re.findall(
            r"\b(DL[- ]?[A-Z0-9]{8,20})\b",
            line.upper()
        )

        for candidate in matches:

            candidate = candidate.replace(
                " ",
                ""
            )

            if candidate.startswith("DL"):

                dl_number = candidate
                break

        if dl_number:
            break

    # --------------------------------------------------
    # NAME
    # --------------------------------------------------

    name = value_after_label(
        lines,
        [
            "Name"
        ]
    )

    # --------------------------------------------------
    # DATE OF BIRTH
    # --------------------------------------------------

    date_of_birth = None

    for line in lines:

        match = re.search(
            r"(?:DOB|Date of Birth)"
            r"\s*[:\-]?\s*"
            + DATE_PATTERN,
            line,
            flags=re.IGNORECASE
        )

        if match:
            date_of_birth = re.search(
                DATE_PATTERN,
                match.group(0)
            ).group(0)

            break

    # --------------------------------------------------
    # ADDRESS
    # --------------------------------------------------

    address = multiline_value_after_label(
        lines,
        [
            "Address"
        ],
        [
            "Authorisation to Drive",
            "Date of Issue",
            "Issue Date",
            "Validity",
            "Valid Upto",
            "Valid Up To",
            "Issuing Authority"
        ],
        max_lines=5
    )

    # --------------------------------------------------
    # VALID FROM
    # --------------------------------------------------

    valid_from = None

    # Prefer explicit Issue Date lines.
    for line in lines:

        if (
                "date of issue" in line.lower()
                or line.lower().startswith("issue date")
        ):

            match = re.search(
                DATE_PATTERN,
                line
            )

            if match:
                valid_from = match.group(0)
                break

    # If date is in a separate OCR line, find dates
    # near the lower document section.
    if not valid_from:

        date_candidates = []

        for line in lines:

            matches = re.findall(
                DATE_PATTERN,
                line
            )

            date_candidates.extend(matches)

        # In this document:
        # 09/02/1976 = DOB
        # 01/03/2011 = issue date
        # 08/02/2026 = validity
        if len(date_candidates) >= 2:

            for candidate in date_candidates[1:]:

                if candidate != date_of_birth:
                    valid_from = candidate
                    break

    # --------------------------------------------------
    # VALID TO
    # --------------------------------------------------

    valid_to = None

    for line in lines:

        if (
                "validity" in line.lower()
                or "valid upto" in line.lower()
                or "valid up to" in line.lower()
                or "valid till" in line.lower()
        ):

            match = re.search(
                DATE_PATTERN,
                line
            )

            if match:
                valid_to = match.group(0)
                break

    # --------------------------------------------------
    # VEHICLE CLASSES
    # --------------------------------------------------

    vehicle_classes = []

    class_patterns = [
        r"\bMCWG\b",
        r"\bMCWOG\b",
        r"\bLMV\b",
        r"\bHMV\b",
        r"\bHGV\b",
        r"\bMGV\b",
        r"\bTransport\b"
    ]

    for line in lines:

        upper_line = line.upper()

        # Don't treat government/department headers
        # as vehicle classes.
        if (
                "DEPARTMENT" in upper_line
                or "GOVERNMENT OF" in upper_line
                or "LICENCE TO DRIVE" in upper_line
        ):
            continue

        for pattern in class_patterns:

            match = re.search(
                pattern,
                upper_line
            )

            if match:

                value = match.group(0)

                if value not in vehicle_classes:
                    vehicle_classes.append(value)

    return {
        "dlNumber": dl_number,
        "name": name,
        "dateOfBirth": date_of_birth,
        "address": address,
        "validFrom": valid_from,
        "validTo": valid_to,
        "vehicleClasses": vehicle_classes
    }