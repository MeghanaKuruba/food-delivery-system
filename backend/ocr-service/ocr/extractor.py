import re
from typing import Any, Dict, List


def _normalize_texts(texts: List[Any]) -> List[Dict[str, Any]]:
    """
    Normalize OCR output into:
    [
        {
            "text": "...",
            "confidence": 0.99,
            "bbox": [...]
        }
    ]

    Supports both dictionary-based OCR results and plain strings.
    """

    normalized = []

    for item in texts:
        if isinstance(item, dict):
            text = str(item.get("text", "")).strip()

            if not text:
                continue

            normalized.append({
                "text": text,
                "confidence": float(item.get("confidence", 0.0)),
                "bbox": item.get("bbox")
            })

        elif isinstance(item, str):
            text = item.strip()

            if text:
                normalized.append({
                    "text": text,
                    "confidence": 0.0,
                    "bbox": None
                })

    return normalized


def _clean_text(text: str) -> str:
    """Normalize spaces and remove unnecessary whitespace."""

    return re.sub(r"\s+", " ", text).strip()


def _find_value_after_label(
        texts: List[Dict[str, Any]],
        aliases: List[str]
) -> str | None:
    """
    Find the OCR line containing one of the supplied labels
    and return the value appearing after the label.

    Example:
        Licence No. : DL-0420110149646
        -> DL-0420110149646
    """

    normalized_aliases = [
        alias.upper().strip()
        for alias in aliases
    ]

    for i, item in enumerate(texts):
        text = _clean_text(item["text"])
        upper_text = text.upper()

        for alias in normalized_aliases:

            # Case 1:
            # "Licence No. : DL-12345"
            if alias in upper_text:

                value = re.sub(
                    re.escape(alias),
                    "",
                    text,
                    flags=re.IGNORECASE
                )

                value = value.strip(" :-/")

                if value:
                    return value

                # Case 2:
                # Label is on one line and value is on next line
                if i + 1 < len(texts):
                    next_value = _clean_text(
                        texts[i + 1]["text"]
                    )

                    if next_value:
                        return next_value

    return None


def _find_pattern(
        texts: List[Dict[str, Any]],
        pattern: str,
        flags: int = re.IGNORECASE
) -> str | None:
    """Search all OCR text for a regular-expression pattern."""

    compiled = re.compile(pattern, flags)

    for item in texts:
        match = compiled.search(item["text"])

        if match:
            return match.group().strip()

    return None


def _extract_date(
        texts: List[Dict[str, Any]],
        aliases: List[str]
) -> str | None:
    """
    Extract a date associated with a label.

    Supports:
        DD/MM/YYYY
        DD-MM-YYYY
        DD.MM.YYYY
    """

    normalized_aliases = [
        alias.upper().strip()
        for alias in aliases
    ]

    date_pattern = re.compile(
        r"\b\d{2}[\/\-.]\d{2}[\/\-.]\d{4}\b"
    )

    for i, item in enumerate(texts):
        text = _clean_text(item["text"])
        upper_text = text.upper()

        if any(alias in upper_text for alias in normalized_aliases):

            match = date_pattern.search(text)

            if match:
                return match.group()

            if i + 1 < len(texts):
                next_text = _clean_text(
                    texts[i + 1]["text"]
                )

                match = date_pattern.search(next_text)

                if match:
                    return match.group()

    return None


def _extract_dl(texts: List[Dict[str, Any]]) -> Dict[str, Any]:
    """
    Extract canonical fields from a Driving Licence.

    The extractor accepts different common labels and maps
    them to a stable response structure.
    """

    dl_number = _find_pattern(
        texts,
        r"\b[A-Z]{2}[-\s]?\d{2}[-\s]?\d{4,}\b"
    )

    if not dl_number:
        dl_number = _find_value_after_label(
            texts,
            [
                "LICENCE NO.",
                "LICENSE NO.",
                "LICENCE NO",
                "LICENSE NO",
                "DL NO.",
                "DL NO",
                "DRIVING LICENCE NO.",
                "DRIVING LICENSE NO.",
                "DRIVING LICENCE NUMBER",
                "DRIVING LICENSE NUMBER"
            ]
        )

    name = _find_value_after_label(
        texts,
        [
            "NAME",
            "NAME OF HOLDER",
            "NAME OF THE HOLDER"
        ]
    )

    date_of_birth = _extract_date(
        texts,
        [
            "DOB",
            "DATE OF BIRTH",
            "DATE OF BIRTH:"
        ]
    )

    address = _find_value_after_label(
        texts,
        [
            "ADDRESS",
            "PERMANENT ADDRESS",
            "PRESENT ADDRESS"
        ]
    )

    valid_from = _extract_date(
        texts,
        [
            "ISSUE DATE",
            "DATE OF ISSUE",
            "VALID FROM",
            "VALID FROM:"
        ]
    )

    valid_to = _extract_date(
        texts,
        [
            "VALID TO",
            "VALID UPTO",
            "VALID UP TO",
            "EXPIRY DATE",
            "VALIDITY"
        ]
    )

    return {
        "dlNumber": dl_number,
        "name": name,
        "dateOfBirth": date_of_birth,
        "address": address,
        "validFrom": valid_from,
        "validTo": valid_to,
        "vehicleClasses": []
    }


def _extract_pan(texts: List[Dict[str, Any]]) -> Dict[str, Any]:
    """Extract canonical fields from a PAN document."""

    pan_number = _find_pattern(
        texts,
        r"\b[A-Z]{5}[0-9]{4}[A-Z]\b",
        flags=re.IGNORECASE
    )

    name = _find_value_after_label(
        texts,
        [
            "NAME",
            "/NAME"
        ]
    )

    father_name = _find_value_after_label(
        texts,
        [
            "FATHER'S NAME",
            "/FATHER'S NAME",
            "FATHER NAME"
        ]
    )

    date_of_birth = _extract_date(
        texts,
        [
            "DATE OF BIRTH",
            "DOB"
        ]
    )

    return {
        "panNumber": pan_number,
        "name": name,
        "fatherName": father_name,
        "dateOfBirth": date_of_birth
    }


def _extract_gst(texts: List[Dict[str, Any]]) -> Dict[str, Any]:
    """Extract canonical fields from a GST document."""

    gst_number = _find_pattern(
        texts,
        r"\b\d{2}[A-Z]{5}\d{4}[A-Z][A-Z\d]Z[A-Z\d]\b"
    )

    legal_name = _find_value_after_label(
        texts,
        [
            "LEGAL NAME",
            "LEGAL NAME OF BUSINESS"
        ]
    )

    trade_name = _find_value_after_label(
        texts,
        [
            "TRADE NAME",
            "TRADE NAME OF BUSINESS"
        ]
    )

    business_address = _find_value_after_label(
        texts,
        [
            "ADDRESS",
            "PRINCIPAL PLACE OF BUSINESS"
        ]
    )

    return {
        "gstNumber": gst_number,
        "legalName": legal_name,
        "tradeName": trade_name,
        "businessAddress": business_address
    }


def extract_fields(
        document_type: str,
        texts: List[Any]
) -> Dict[str, Any]:
    """
    Main document extraction entry point.

    PaddleOCR produces OCR data.
    This function converts that OCR data into
    document-specific structured fields.
    """

    normalized_texts = _normalize_texts(texts)

    document_type = document_type.upper().strip()

    if document_type == "DRIVING_LICENSE":
        return _extract_dl(normalized_texts)

    if document_type == "PAN":
        return _extract_pan(normalized_texts)

    if document_type == "GST":
        return _extract_gst(normalized_texts)

    # Unknown document type.
    # Return raw OCR-derived information without
    # inventing document-specific fields.
    return {}