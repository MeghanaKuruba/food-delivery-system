import re


DATE_PATTERN = re.compile(
    r"\b\d{2}[-/]\d{2}[-/]\d{4}\b"
)

LICENSE_NUMBER_PATTERN = re.compile(
    r"\b\d{14}\b"
)


def clean_line(text):
    """Clean OCR text without changing its meaning."""
    return re.sub(r"\s+", " ", text.strip())


def find_license_number(texts):
    """
    Extract the FSSAI license number.

    Expected format:
    14 digits
    """

    for item in texts:
        text = clean_line(item["text"])

        if "license number" in text.lower():
            match = LICENSE_NUMBER_PATTERN.search(text)

            if match:
                return match.group(0)

    # Fallback: search for a standalone 14-digit number
    for item in texts:
        text = clean_line(item["text"])

        match = LICENSE_NUMBER_PATTERN.fullmatch(text)

        if match:
            return match.group(0)

    return None


def find_business_name(texts):
    """
    Extract the business/licensee name.

    Typical FSSAI layouts contain:
        1. Name & Registered Office address of
        <business name>

    or:
        Name & Registered Office address of Licensee
        <business name>
    """

    for i, item in enumerate(texts):
        text = clean_line(item["text"])
        lower_text = text.lower()

        if (
                "name & registered office address" in lower_text
                or "name and registered office address" in lower_text
        ):

            # Case 1:
            # Label and value are on the same OCR line
            #
            # Example:
            # Name & Registered Office address of Licensee: ABC Pvt Ltd
            colon_index = text.find(":")

            if colon_index != -1:
                value = text[colon_index + 1:].strip()

                if value:
                    return value

            # Case 2:
            # Value is on the next OCR line
            #
            # Example:
            # 1. Name & Registered Office address of
            # Homlee Ecommerce Private Limited

            for next_item in texts[i + 1:]:
                next_text = clean_line(next_item["text"])

                if not next_text:
                    continue

                next_lower = next_text.lower()

                # Ignore labels that belong to the same section
                if next_lower in {
                    "licensee:",
                    "licensee",
                    "1.",
                }:
                    continue

                if (
                        "address of authorized premises" in next_lower
                        or "kind of business" in next_lower
                        or "category of license" in next_lower
                ):
                    break

                # Avoid accidentally returning another field label
                if next_lower.startswith(
                        (
                                "2.",
                                "3.",
                                "4.",
                                "5.",
                                "license number",
                        )
                ):
                    break

                return next_text

    return None


ADDRESS_STOP_KEYWORDS = [
    "kind of business",
    "category of license",
    "dairy business details",
    "issued on",
    "valid upto",
    "valid up to",
    "valid until",
    "place:",
    "designated officer",
]


def looks_like_address(text):
    """
    Basic heuristic for an address line.

    This is intentionally generic because address formats
    differ between states and certificates.
    """

    lower_text = text.lower()

    if any(keyword in lower_text for keyword in ADDRESS_STOP_KEYWORDS):
        return False

    if re.search(r"\b\d{6}\b", text):
        return True

    address_words = [
        "road",
        "rd",
        "street",
        "st",
        "nagar",
        "layout",
        "main",
        "cross",
        "chennai",
        "bangalore",
        "bengaluru",
        "mumbai",
        "delhi",
        "kolkata",
        "hyderabad",
        "tamil",
        "karnataka",
        "maharashtra",
        "uttar",
        "pradesh",
        "kerala",
        "gujarat",
        "building",
        "floor",
        "plot",
        "shop",
        "village",
        "district",
        "taluk",
        "town",
        "area",
    ]

    return any(word in lower_text for word in address_words)


def find_premises_address(texts):
    """
    Extract authorized premises address.

    Example:

    2. Address of Authorized Premises:
    53 1st Main Road, Anand Nagar,
    Thoraipakkam, Chengalpattu Mpty
    Kancheepuram, Tamil Nadu-600097

    The address may span multiple OCR lines.
    """

    for i, item in enumerate(texts):
        text = clean_line(item["text"])
        lower_text = text.lower()

        if "address of authorized premises" not in lower_text:
            continue

        address_parts = []

        # Check whether the address starts on the same OCR line
        colon_index = text.find(":")

        if colon_index != -1:
            same_line_value = text[colon_index + 1:].strip()

            if same_line_value:
                address_parts.append(same_line_value)

        # Read following OCR lines
        for next_item in texts[i + 1:]:
            next_text = clean_line(next_item["text"])

            if not next_text:
                continue

            next_lower = next_text.lower()

            # Stop at the next numbered/known FSSAI field
            if any(
                    keyword in next_lower
                    for keyword in ADDRESS_STOP_KEYWORDS
            ):
                break

            if re.match(r"^\d+\.", next_text):
                break

            # Ignore obvious unrelated labels
            if next_lower in {
                "licensee:",
                "licensee",
                "designated officer",
            }:
                break

            address_parts.append(next_text)

            # Once we have a PIN code, the address is normally complete
            if re.search(r"\b\d{6}\b", next_text):
                break

        if address_parts:
            return " ".join(address_parts)

    return None


def find_kind_of_business(texts):
    """
    Extract Kind of Business.

    A certificate may contain one or multiple business activities.

    Example:
        Trade/Retail - Retailer

    Or:
        Food Services - Caterer
        Food Services - Restaurants
    """

    for i, item in enumerate(texts):
        text = clean_line(item["text"])
        lower_text = text.lower()

        if "kind of business" not in lower_text:
            continue

        values = []

        # Same-line value
        colon_index = text.find(":")

        if colon_index != -1:
            value = text[colon_index + 1:].strip()

            if value:
                values.append(value)

        # Following OCR lines
        for next_item in texts[i + 1:]:
            next_text = clean_line(next_item["text"])

            if not next_text:
                continue

            next_lower = next_text.lower()

            if any(
                    keyword in next_lower
                    for keyword in [
                        "dairy business details",
                        "category of license",
                        "issued on",
                        "valid upto",
                        "valid up to",
                        "valid until",
                        "place:",
                        "designated officer",
                    ]
            ):
                break

            if re.match(r"^\d+\.", next_text):
                break

            # Avoid collecting unrelated labels
            if next_lower in {
                "no",
                "yes",
            }:
                continue

            values.append(next_text)

            # Most FSSAI KOB values contain these patterns
            if not any(
                    keyword in next_lower
                    for keyword in [
                        "food services",
                        "trade",
                        "retail",
                        "manufacturer",
                        "manufacturing",
                        "distributor",
                        "wholesaler",
                        "importer",
                        "exporter",
                        "transporter",
                        "storage",
                    ]
            ):
                # Don't keep collecting arbitrary text
                if values:
                    break

        # Remove duplicates while preserving order
        unique_values = []

        for value in values:
            if value not in unique_values:
                unique_values.append(value)

        if not unique_values:
            return None

        if len(unique_values) == 1:
            return unique_values[0]

        return unique_values

    return None


def find_license_category(texts):
    """
    Extract:
        State License
        Central License
        Basic Registration
        etc.
    """

    for i, item in enumerate(texts):
        text = clean_line(item["text"])
        lower_text = text.lower()

        if "category of license" not in lower_text:
            continue

        # Same-line value
        colon_index = text.find(":")

        if colon_index != -1:
            value = text[colon_index + 1:].strip()

            if value:
                return value

        # Next OCR line
        for next_item in texts[i + 1:]:
            next_text = clean_line(next_item["text"])

            if not next_text:
                continue

            next_lower = next_text.lower()

            if (
                    "issued on" in next_lower
                    or "valid upto" in next_lower
                    or "valid up to" in next_lower
                    or "valid until" in next_lower
            ):
                break

            if re.match(r"^\d+\.", next_text):
                break

            if next_lower in {
                "state license",
                "central license",
                "basic registration",
            }:
                return next_text

    return None


def find_date_after_label(texts, labels):
    """
    Extract a date ONLY when the date appears on the SAME OCR line
    as the requested label.

    This prevents incorrect results such as:

        Valid Upto: 13-02-2024
        User Id:
        doXXXIm

    becoming:

        validUntil = "User Id"

    """

    for item in texts:
        text = clean_line(item["text"])
        lower_text = text.lower()

        for label in labels:
            if label.lower() in lower_text:

                match = DATE_PATTERN.search(text)

                if match:
                    return match.group(0)

    return None


def extract(texts):
    """
    Main FSSAI extractor.

    Returns canonical fields that can be consumed
    by the Java Verification Service.
    """

    issued_date = find_date_after_label(
        texts,
        [
            "Issued On",
            "Issued On /",
            "Issue Date",
            "Issued",
        ]
    )

    valid_until = find_date_after_label(
        texts,
        [
            "Valid Upto",
            "Valid Up To",
            "Valid Until",
            "Validity",
        ]
    )

    return {
        "licenseNumber": find_license_number(texts),
        "businessName": find_business_name(texts),
        "premisesAddress": find_premises_address(texts),
        "kindOfBusiness": find_kind_of_business(texts),
        "licenseCategory": find_license_category(texts),
        "issuedDate": issued_date,
        "validUntil": valid_until,
    }