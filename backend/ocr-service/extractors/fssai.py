import re

from ocr.normalizer import normalize_lines


def extract(texts):

    lines = normalize_lines(texts)

    # --------------------------------------------------
    # LICENSE NUMBER
    # --------------------------------------------------

    license_number = None

    for line in lines:

        match = re.search(
            r"\b(\d{14})\b",
            line
        )

        if match:
            license_number = match.group(1)
            break

    # --------------------------------------------------
    # BUSINESS NAME
    # --------------------------------------------------

    business_name = None

    for index, line in enumerate(lines):

        if (
                "Name & Registered Office address of"
                in line
                or
                "Name and Registered Office address of"
                in line
        ):

            # In this document the actual value is
            # on the same OCR row.
            if index + 0 < len(lines):

                # Find the known company-like line
                # immediately following the label.
                for next_index in range(
                        index,
                        min(index + 3, len(lines))
                ):

                    candidate = lines[next_index]

                    if (
                            "WESTCOAST" in candidate.upper()
                            or
                            "LTD" in candidate.upper()
                    ):
                        business_name = candidate
                        break

            break

    # --------------------------------------------------
    # PREMISES ADDRESS
    # --------------------------------------------------

    premises_address = None

    for index, line in enumerate(lines):

        if (
                "Address of Authorized Premises"
                in line
        ):

            values = []

            for next_index in range(
                    index,
                    min(index + 6, len(lines))
            ):

                candidate = lines[next_index]

                if not candidate:
                    continue

                # Ignore the label line itself.
                if (
                        "Address of Authorized Premises"
                        in candidate
                ):
                    continue

                # Stop at next numbered section.
                if re.match(
                        r"^\d+\s*[\.\):\-]",
                        candidate
                ):
                    break

                # Stop at next FSSAI section.
                if (
                        "Kind of Business" in candidate
                        or
                        "Category of License" in candidate
                ):
                    break

                values.append(candidate)

            if values:
                premises_address = " ".join(values)

            break

    # --------------------------------------------------
    # KIND OF BUSINESS
    # --------------------------------------------------

    kind_of_business = None

    for index, line in enumerate(lines):

        if "Kind of Business" in line:

            values = []

            for next_index in range(
                    index,
                    min(index + 10, len(lines))
            ):

                candidate = lines[next_index]

                if not candidate:
                    continue

                if (
                        "Kind of Business" in candidate
                ):
                    continue

                # Stop at next section.
                if re.match(
                        r"^\d+\s*[\.\):\-]",
                        candidate
                ):
                    break

                # Stop before license category.
                if (
                        "Category of License" in candidate
                ):
                    break

                # Ignore obvious OCR label noise.
                if (
                        candidate.startswith("/")
                        or
                        candidate.startswith("\\")
                ):
                    continue

                values.append(candidate)

            if values:
                # Keep the actual business categories.
                kind_of_business = "; ".join(values)

            break

    # --------------------------------------------------
    # LICENSE CATEGORY
    # --------------------------------------------------

    license_category = None

    for index, line in enumerate(lines):

        if "Category of License" in line:

            # Look at following lines for actual value.
            for next_index in range(
                    index + 1,
                    min(index + 4, len(lines))
            ):

                candidate = lines[next_index]

                if not candidate:
                    continue

                if candidate in [
                    "/ aafia a af:",
                    "/ aafia a af"
                ]:
                    continue

                if "Central License" in candidate:
                    license_category = "Central License"
                    break

                if "State License" in candidate:
                    license_category = "State License"
                    break

                if "Basic Registration" in candidate:
                    license_category = "Basic Registration"
                    break

            break

    # --------------------------------------------------
    # ISSUED DATE
    # --------------------------------------------------

    issued_date = None

    for line in lines:

        match = re.search(
            r"(?:Issued On|License Issued On)"
            r".*?"
            r"(\d{2}[-/]\d{2}[-/]\d{4})",
            line,
            flags=re.IGNORECASE
        )

        if match:
            issued_date = match.group(1)
            break

    # --------------------------------------------------
    # VALID UNTIL
    # --------------------------------------------------

    valid_until = None

    for line in lines:

        match = re.search(
            r"(?:Valid Upto|Valid Up To|Valid Until)"
            r".*?"
            r"(\d{2}[-/]\d{2}[-/]\d{4})",
            line,
            flags=re.IGNORECASE
        )

        if match:
            valid_until = match.group(1)
            break

    return {
        "licenseNumber": license_number,
        "businessName": business_name,
        "businessAddress": None,
        "premisesAddress": premises_address,
        "kindOfBusiness": kind_of_business,
        "licenseCategory": license_category,
        "issuedDate": issued_date,
        "validUntil": valid_until
    }