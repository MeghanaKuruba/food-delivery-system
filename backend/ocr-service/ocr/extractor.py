import re
from typing import Any, Dict, List, Optional, Tuple


# ============================================================
# BASIC HELPERS
# ============================================================

def clean_text(value: str) -> str:
    if not value:
        return ""

    value = value.replace("\\:", ":")
    value = value.replace("\\", "")
    value = re.sub(r"\s+", " ", value)

    return value.strip(" :,-")


def normalize_label(value: str) -> str:
    value = clean_text(value).lower()

    replacements = {
        "manufacturar": "manufacturer",
        "manufactuer": "manufacturer",
        "licence": "license",
        "regn": "registration",
        "regd": "registered",
        "chasis": "chassis",
        "upto": "up to",
    }

    for old, new in replacements.items():
        value = value.replace(old, new)

    value = re.sub(r"[^a-z0-9 ]", " ", value)
    value = re.sub(r"\s+", " ", value)

    return value.strip()


def bbox(item: Dict[str, Any]) -> List[float]:
    value = item.get("bbox") or [0, 0, 0, 0]

    if len(value) < 4:
        return [0, 0, 0, 0]

    return [
        float(value[0]),
        float(value[1]),
        float(value[2]),
        float(value[3]),
    ]


def x1(item):
    return bbox(item)[0]


def y1(item):
    return bbox(item)[1]


def x2(item):
    return bbox(item)[2]


def y2(item):
    return bbox(item)[3]


def center_y(item):
    return (y1(item) + y2(item)) / 2


def height(item):
    return max(1, y2(item) - y1(item))


def same_row(a, b) -> bool:
    """
    Two OCR boxes belong to the same visual row when
    their vertical centers are close relative to text height.
    """

    tolerance = max(
        height(a),
        height(b),
        12
    ) * 0.8

    return abs(center_y(a) - center_y(b)) <= tolerance


# ============================================================
# OCR TOKEN PREPARATION
# ============================================================

def prepare_tokens(texts: List[Any]) -> List[Dict[str, Any]]:

    tokens = []

    for item in texts:

        if not isinstance(item, dict):
            continue

        text = clean_text(
            str(item.get("text", ""))
        )

        if not text:
            continue

        tokens.append({
            "text": text,
            "confidence": float(
                item.get("confidence", 0)
            ),
            "bbox": item.get("bbox") or [0, 0, 0, 0]
        })

    return tokens


# ============================================================
# FIELD ALIASES
# ============================================================

FIELD_ALIASES = {

    "RC": {

        "registrationNumber": [
            "regn no",
            "registration no",
            "registration number",
            "vehicle registration number"
        ],

        "ownerName": [
            "regd owner",
            "registered owner",
            "owner name",
            "name of registered owner"
        ],

        "registrationDate": [
            "regn date",
            "registration date"
        ],

        "colour": [
            "colour",
            "color"
        ],

        "fuelType": [
            "fuel",
            "fuel type"
        ],

        "vehicleClass": [
            "vehicle class",
            "class of vehicle"
        ],

        "bodyType": [
            "body type"
        ],

        "manufacturer": [
            "manufacturar",
            "manufacturer",
            "manufacturer name",
            "maker"
        ],

        "chassisNumber": [
            "chassis no",
            "chassis number"
        ],

        "engineNumber": [
            "engine no",
            "engine number"
        ],

        "model": [
            "model no",
            "model number",
            "model"
        ],

        "registrationValidity": [
            "regd validity",
            "registration validity"
        ],

        "address": [
            "address"
        ],

        "seatingCapacity": [
            "seat capacity",
            "seating capacity"
        ]
    },

    "DRIVING_LICENSE": {

        "dlNumber": [
            "licence no",
            "license no",
            "licence number",
            "license number",
            "dl no",
            "dl number"
        ],

        "name": [
            "name"
        ],

        "dateOfBirth": [
            "dob",
            "date of birth"
        ],

        "address": [
            "address"
        ],

        "authorisationToDrive": [
            "authorisation to drive",
            "authorization to drive"
        ],

        "validFrom": [
            "date of issue",
            "issue date"
        ],

        "validTo": [
            "validity",
            "valid upto",
            "valid up to",
            "valid till"
        ]
    },

    "GST": {

        "gstNumber": [
            "registration number",
            "gstin",
            "gst number"
        ],

        "legalName": [
            "legal name"
        ],

        "tradeName": [
            "trade name",
            "trade name if any"
        ],

        "businessAddress": [
            "address of principal place of business",
            "address of principal place of",
            "principal place of business",
            "business address"
        ],

        "constitutionOfBusiness": [
            "constitution of business"
        ],

        "registrationType": [
            "type of registration"
        ],

        "dateOfIssue": [
            "date of issue of certificate",
            "date of issue"
        ]
    },

    "FSSAI": {

        "licenseNumber": [
            "license number",
            "licence number",
            "license no",
            "licence no"
        ],

        "businessName": [
            "name of licensee",
            "name & registered office address of",
            "name and registered office address of"
        ],

        "businessAddress": [
            "registered office address"
        ],

        "premisesAddress": [
            "address of authorized premises",
            "address of authorised premises"
        ],

        "kindOfBusiness": [
            "kind of business"
        ],

        "licenseCategory": [
            "category of license",
            "license category"
        ],

        "issuedDate": [
            "issued on",
            "date of issue"
        ],

        "validUntil": [
            "valid upto",
            "valid up to",
            "valid until"
        ]
    },

    "INSURANCE": {

        "registrationNumber": [
            "registration number",
            "vehicle registration number"
        ],

        "ownerName": [
            "owner name",
            "insured name"
        ],

        "insuranceCompany": [
            "insurance company",
            "insurer"
        ],

        "policyNumber": [
            "policy number",
            "policy no"
        ],

        "insuranceValidUntil": [
            "valid upto",
            "valid up to",
            "valid until"
        ],

        "vehicleModel": [
            "vehicle model",
            "model"
        ],

        "fuelType": [
            "fuel type",
            "fuel"
        ],

        "chassisNumber": [
            "chassis number",
            "chassis no",
            "chasis number"
        ],

        "engineNumber": [
            "engine number",
            "engine no"
        ]
    },

    "PAN": {

        "panNumber": [
            "pan number",
            "permanent account number",
            "pan"
        ],

        "name": [
            "name"
        ],

        "fatherName": [
            "father name",
            "fathers name",
            "father's name"
        ],

        "dateOfBirth": [
            "date of birth",
            "dob"
        ]
    },

    "BUSINESS_REGISTRATION": {

        "registrationNumber": [
            "registration number",
            "registration no",
            "certificate number"
        ],

        "businessName": [
            "business name",
            "company name",
            "name of company",
            "name of organization",
            "name of organisation"
        ],

        "organisationType": [
            "organisation type",
            "organization type",
            "entity type",
            "constitution of business"
        ],

        "address": [
            "registered address",
            "business address",
            "registered office address"
        ],

        "registrationDate": [
            "registration date",
            "date of registration",
            "date of incorporation"
        ],

        "validUntil": [
            "valid until",
            "valid upto",
            "valid up to",
            "expiry date"
        ]
    }
}


# ============================================================
# LABEL DETECTION
# ============================================================

def find_field(
        text: str,
        document_type: str
) -> Optional[Tuple[str, str]]:

    normalized = normalize_label(text)

    aliases = FIELD_ALIASES.get(
        document_type,
        {}
    )

    matches = []

    for field, values in aliases.items():

        for alias in values:

            normalized_alias = normalize_label(
                alias
            )

            if normalized_alias in normalized:

                matches.append(
                    (
                        len(normalized_alias),
                        field,
                        alias
                    )
                )

    if not matches:
        return None

    # Longest matching alias wins.
    matches.sort(
        key=lambda x: x[0],
        reverse=True
    )

    _, field, original_alias = matches[0]

    # Return the ORIGINAL alias.
    # This is important because extract_inline_value()
    # searches the original OCR text.
    return field, original_alias


# ============================================================
# INLINE VALUE
# ============================================================

def extract_inline_value(
        text: str,
        alias: str
) -> Optional[str]:

    original = clean_text(text)

    if not original or not alias:
        return None

    pattern = re.compile(
        re.escape(alias).replace(
            "\\ ",
            r"\s+"
        ),
        re.IGNORECASE
    )

    match = pattern.search(original)

    if not match:
        return None

    value = original[
            match.end():
            ]

    value = re.sub(
        r"^[\s:.\-\\/]+",
        "",
        value
    )

    value = clean_text(value)

    return value if value else None


# ============================================================
# RIGHT-SIDE MATCH
# ============================================================

def find_same_row_value(
        tokens: List[Dict[str, Any]],
        label_index: int,
        alias: str
) -> Optional[str]:

    label = tokens[label_index]

    # First: value may be inside the same OCR token.
    inline = extract_inline_value(
        label["text"],
        alias
    )

    if inline:
        return inline

    candidates = []

    for index, token in enumerate(tokens):

        if index == label_index:
            continue

        # Must actually be on the same visual row.
        if not same_row(
                label,
                token
        ):
            continue

        # Value must be to the RIGHT.
        if x1(token) < x2(label) - 5:
            continue

        distance = (
                x1(token) - x2(label)
        )

        # Don't jump across the page.
        if distance > 700:
            continue

        candidates.append(
            (
                distance,
                index,
                token
            )
        )

    if not candidates:
        return None

    candidates.sort(
        key=lambda x: x[0]
    )

    return clean_text(
        candidates[0][2]["text"]
    )


# ============================================================
# BELOW-LABEL MATCH
# ============================================================

def find_below_value(
        tokens: List[Dict[str, Any]],
        label_index: int
) -> Optional[str]:

    label = tokens[label_index]

    candidates = []

    for index, token in enumerate(tokens):

        if index == label_index:
            continue

        # Must be below.
        if y1(token) < y2(label) - 3:
            continue

        vertical_gap = (
                y1(token) - y2(label)
        )

        # Don't jump too far.
        if vertical_gap > 80:
            continue

        # Similar horizontal area.
        horizontal_distance = abs(
            x1(token) - x1(label)
        )

        if horizontal_distance > 250:
            continue

        candidates.append(
            (
                vertical_gap + horizontal_distance,
                index,
                token
            )
        )

    if not candidates:
        return None

    candidates.sort(
        key=lambda x: x[0]
    )

    return clean_text(
        candidates[0][2]["text"]
    )


# ============================================================
# ADDRESS COLLECTION
# ============================================================

def collect_address(
        tokens: List[Dict[str, Any]],
        label_index: int,
        first_value: str,
        document_type: str
) -> str:

    value_index = None

    for index, token in enumerate(tokens):

        if clean_text(
                token["text"]
        ) == clean_text(
            first_value
        ):

            value_index = index
            break

    if value_index is None:
        return first_value

    result = [
        first_value
    ]

    previous = tokens[value_index]

    # --------------------------------------------------------
    # GST ONLY
    #
    # GST address OCR can be split like:
    #
    # Address of Principal Place of
    # 1, CDRI, ... Lucknow, Uttar
    # Business
    # Pradesh, 226021
    #
    # "Business" is part of the label, not the next field.
    # The continuation value can also slightly overlap the
    # previous OCR box vertically.
    #
    # Keep this logic isolated to GST so no other document
    # extraction behavior is changed.
    # --------------------------------------------------------

    if document_type == "GST":

        previous_value = tokens[value_index]

        for index in range(
                value_index + 1,
                len(tokens)
        ):

            token = tokens[index]

            text = clean_text(
                token["text"]
            )

            normalized_text = normalize_label(
                text
            )

            # Skip the continuation word "Business"
            # from "Address of Principal Place of Business".
            if normalized_text == "business":
                continue

            # Numbered section = next field.
            if re.match(
                    r"^\d+\.",
                    text
            ):
                break

            # A complete known field label means the address
            # has ended.
            field_match = find_field(
                text,
                document_type
            )

            if field_match:
                continue

            # GST continuation values are normally aligned
            # with the first address value.
            horizontal_distance = abs(
                x1(token) - x1(previous_value)
            )

            if horizontal_distance > 40:
                continue

            # Allow OCR boxes that overlap slightly vertically.
            # This handles:
            # first value y=347..360
            # continuation value y=359..372
            vertical_gap = (
                    y1(token) - y2(previous_value)
            )

            if vertical_gap > 45:
                break

            # Ignore text that is clearly above the current
            # address line.
            if center_y(token) < center_y(previous_value) - 5:
                continue

            result.append(text)

            previous_value = token

        return " ".join(result)

    # --------------------------------------------------------
    # EXISTING ADDRESS LOGIC FOR ALL OTHER DOCUMENTS
    # --------------------------------------------------------

    for index in range(
            value_index + 1,
            len(tokens)
    ):

        token = tokens[index]

        # Must be below.
        if y1(token) < y2(previous):
            continue

        vertical_gap = (
                y1(token) - y2(previous)
        )

        if vertical_gap > 45:
            break

        text = clean_text(
            token["text"]
        )

        # Numbered section = next field.
        if re.match(
                r"^\d+\.",
                text
        ):
            break

        # Another known label = next field.
        field_match = find_field(
            text,
            document_type
        )

        if field_match:
            break

        # Keep address lines reasonably aligned.
        if abs(
                x1(token) - x1(previous)
        ) > 180:

            break

        result.append(text)

        previous = token

    return " ".join(result)


# ============================================================
# VALUE VALIDATORS
# ============================================================

def validate_value(
        document_type: str,
        field: str,
        value: str
) -> Optional[str]:

    value = clean_text(value)

    if not value:
        return None

    upper = value.upper()

    # --------------------------------------------------------
    # GST
    # --------------------------------------------------------

    if (
            document_type == "GST"
            and field == "gstNumber"
    ):

        match = re.search(
            r"\b\d{2}[A-Z]{5}\d{4}[A-Z][A-Z0-9]Z[A-Z0-9]\b",
            upper
        )

        return (
            match.group(0)
            if match
            else None
        )

    # --------------------------------------------------------
    # PAN
    # --------------------------------------------------------

    if (
            document_type == "PAN"
            and field == "panNumber"
    ):

        match = re.search(
            r"\b[A-Z]{5}\d{4}[A-Z]\b",
            upper
        )

        return (
            match.group(0)
            if match
            else None
        )

    # --------------------------------------------------------
    # DL
    # --------------------------------------------------------

    if (
            document_type == "DRIVING_LICENSE"
            and field == "dlNumber"
    ):

        match = re.search(
            r"\b[A-Z]{2}[-\s]?\d{2}[-\s]?\d{4}[-\s]?\d{7}\b",
            upper
        )

        if not match:
            return None

        raw = re.sub(
            r"[-\s]",
            "",
            match.group(0)
        )

        return (
                raw[:2]
                + "-"
                + raw[2:]
        )

    # --------------------------------------------------------
    # Dates
    # --------------------------------------------------------

    if field in {
        "registrationDate",
        "registrationValidity",
        "dateOfBirth",
        "validFrom",
        "validTo",
        "issuedDate",
        "validUntil",
        "insuranceValidUntil",
        "dateOfIssue"
    }:

        date_patterns = [

            # 18-12-2022
            r"\b\d{1,2}[/-]\d{1,2}[/-]\d{2,4}\b",

            # 06-Jul-2014
            r"\b\d{1,2}-[A-Za-z]{3}-\d{2,4}\b",

            # 06 July 2014
            r"\b\d{1,2}\s+[A-Za-z]{3,9}\s+\d{2,4}\b"
        ]

        for pattern in date_patterns:

            match = re.search(
                pattern,
                value,
                re.IGNORECASE
            )

            if match:
                return match.group(0)

    # IMPORTANT:
    # Return normal values for all fields
    # that do not need special validation.
    return value


# ============================================================
# SPECIAL REGEX FALLBACKS
# ============================================================

def regex_fallback(
        document_type: str,
        tokens: List[Dict[str, Any]],
        result: Dict[str, Any]
):

    full_text = " ".join(
        clean_text(
            token["text"]
        )
        for token in tokens
    )

    upper = full_text.upper()

    # --------------------------------------------------------
    # GST
    # --------------------------------------------------------

    if document_type == "GST":

        if "gstNumber" not in result:

            match = re.search(
                r"\b\d{2}[A-Z]{5}\d{4}[A-Z][A-Z0-9]Z[A-Z0-9]\b",
                upper
            )

            if match:
                result[
                    "gstNumber"
                ] = match.group(0)

    # --------------------------------------------------------
    # PAN
    # --------------------------------------------------------

    if document_type == "PAN":

        if "panNumber" not in result:

            match = re.search(
                r"\b[A-Z]{5}\d{4}[A-Z]\b",
                upper
            )

            if match:
                result[
                    "panNumber"
                ] = match.group(0)

    # --------------------------------------------------------
    # RC
    # --------------------------------------------------------

    if document_type == "RC":

        if "registrationNumber" not in result:

            # Prefer text containing Regn. No.
            for token in tokens:

                text = upper_text(
                    token
                )

                if (
                        "REGN" in text
                        and "NO" in text
                ):

                    match = re.search(
                        r"\b[A-Z]{2}\d{1,2}[A-Z]{1,3}\d{4}\b",
                        text
                    )

                    if match:

                        result[
                            "registrationNumber"
                        ] = match.group(0)

                        break

    # --------------------------------------------------------
    # DL
    # --------------------------------------------------------

    if document_type == "DRIVING_LICENSE":

        if "dlNumber" not in result:

            for token in tokens:

                text = upper_text(
                    token
                )

                if "DL-" in text:

                    match = re.search(
                        r"\b[A-Z]{2}[-\s]?\d{2}[-\s]?\d{4}[-\s]?\d{7}\b",
                        text
                    )

                    if match:

                        raw = re.sub(
                            r"[-\s]",
                            "",
                            match.group(0)
                        )

                        result[
                            "dlNumber"
                        ] = (
                                raw[:2]
                                + "-"
                                + raw[2:]
                        )

                        break


def upper_text(token):
    return clean_text(
        token["text"]
    ).upper()


# ============================================================
# MAIN EXTRACTION
# ============================================================

def extract_generic(
        document_type: str,
        texts: List[Any]
) -> Dict[str, Any]:

    document_type = document_type.upper()

    if document_type not in FIELD_ALIASES:

        raise ValueError(
            f"Unsupported document type: {document_type}"
        )

    tokens = prepare_tokens(
        texts
    )

    result = {}

    # --------------------------------------------------------
    # Detect labels
    # --------------------------------------------------------

    for index, token in enumerate(tokens):

        match = find_field(
            token["text"],
            document_type
        )

        if not match:
            continue

        field, alias = match

        # Don't allow a later occurrence
        # to overwrite an already extracted field.
        if field in result:
            continue

        value = None

        # ----------------------------------------------------
        # 1. Same-line value
        #
        # EXCEPTION:
        # Driving License "Authorisation to Drive" has
        # another field ("Date of Issue") on its right.
        # Its actual value is directly below the label.
        # ----------------------------------------------------

        if (
                document_type == "DRIVING_LICENSE"
                and field == "authorisationToDrive"
        ):

            value = find_below_value(
                tokens,
                index
            )

        else:

            value = find_same_row_value(
                tokens,
                index,
                alias
            )

        # ----------------------------------------------------
        # 2. Next-line value
        # ----------------------------------------------------

        if not value:

            value = find_below_value(
                tokens,
                index
            )

        if not value:
            continue

        # ----------------------------------------------------
        # 3. Normalize / validate
        # ----------------------------------------------------

        value = validate_value(
            document_type,
            field,
            value
        )

        if not value:
            continue

        # ----------------------------------------------------
        # 4. Address continuation
        # ----------------------------------------------------

        if field in {
            "address",
            "businessAddress",
            "premisesAddress"
        }:

            value = collect_address(
                tokens,
                index,
                value,
                document_type
            )

        result[field] = value

    # --------------------------------------------------------
    # Regex fallback
    # --------------------------------------------------------

    regex_fallback(
        document_type,
        tokens,
        result
    )

    return result


# ============================================================
# PUBLIC FUNCTION
# ============================================================

def extract_fields(
        document_type: str,
        texts: List[Any]
) -> Dict[str, Any]:

    return extract_generic(
        document_type.upper(),
        texts
    )
