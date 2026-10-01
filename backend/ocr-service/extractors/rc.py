import re
from ocr.normalizer import normalize_lines


FIELD_LABELS = {
    "registrationDate": [
        "Registration Date",
        "Regn. Date",
        "Regn Date"
    ],
    "vehicleClass": [
        "Vehicle Class",
        "Class of Vehicle"
    ],
    "model": [
        "Vehicle Model",
        "Model No.",
        "Model No",
        "Model Number",
        "Make / Model",
        "Make/Model"
    ],
    "fuelType": [
        "Fuel Type",
        "Fuel"
    ],
    "colour": [
        "Vehicle Color",
        "Vehicle Colour",
        "Colour",
        "Color"
    ],
    "chassisNumber": [
        "Chasis Number",
        "Chassis Number",
        "Chasis No.",
        "Chassis No.",
        "Chasis No",
        "Chassis No"
    ],
    "engineNumber": [
        "Engine Number",
        "Engine No.",
        "Engine No",
        "Engine Number"
    ],
    "ownerName": [
        "Owner Name",
        "Regd. Owner",
        "Regd Owner",
        "Registered Owner",
        "Owner's Name"
    ],
    "registrationValidity": [
        "Regd. Validity",
        "Regd Validity",
        "Registration Validity"
    ]
}


def clean_line(value):
    if value is None:
        return None

    value = str(value).strip()
    value = re.sub(r"\s+", " ", value)

    return value.strip(" :;-/\\")


def is_field_label(line):
    """
    Determines whether an OCR line is another known field label.

    This prevents:
        Engine Number
        Cubic Capacity

    from becoming:
        engineNumber = "Cubic Capacity"
    """

    if not line:
        return False

    lower_line = line.lower().strip(" :")

    known_labels = []

    for labels in FIELD_LABELS.values():
        known_labels.extend(labels)

    known_labels.extend([
        "Registration Number",
        "Present Address",
        "Permanent Address",
        "S/W/D Name",
        "Mon/Year of Mfg.",
        "Cubic Capacity",
        "Name of Financer",
        "RC Status",
        "RC Blacklist status",
        "Tax Up to",
        "Owner Sno.",
        "Insurance",
        "Company Policy",
        "Valid",
        "Number",
        "upto",
        "PUC Certificate",
        "Certificate Number",
        "Fitness Certificate",
        "Issuing Authority"
    ])

    for label in sorted(known_labels, key=len, reverse=True):

        normalized_label = label.lower().strip(" :")

        if lower_line == normalized_label:
            return True

    return False


def inline_value(line, labels):
    if not line:
        return None

    lower_line = line.lower()

    for label in sorted(labels, key=len, reverse=True):

        lower_label = label.lower()

        if lower_label not in lower_line:
            continue

        index = lower_line.find(lower_label)

        value = line[index + len(label):]

        value = re.sub(
            r"^[\s:/\\\-]+",
            "",
            value
        )

        value = clean_line(value)

        if value:
            return value

    return None


def value_after_label(lines, labels):
    """
    Handles:

        Label: VALUE

    and:

        Label
        VALUE

    But never accepts another field label as the value.
    """

    for index, line in enumerate(lines):

        # Same-line value.
        value = inline_value(line, labels)

        if value:

            if not is_field_label(value):
                return value

        lower_line = line.lower()

        matched_label = None

        for label in sorted(labels, key=len, reverse=True):

            if label.lower() in lower_line:
                matched_label = label
                break

        if not matched_label:
            continue

        # Look at the next OCR item.
        if index + 1 >= len(lines):
            continue

        candidate = clean_line(lines[index + 1])

        if not candidate:
            continue

        # Critical protection.
        if is_field_label(candidate):
            continue

        return candidate

    return None


def extract_registration_number(lines):
    """
    Registration number must be attached to an explicit
    registration-number label.

    This avoids unrelated identifiers such as MH2687796.
    """

    pattern = re.compile(
        r"\b([A-Z]{2})\s*-?\s*(\d{1,2})\s*-?\s*"
        r"([A-Z]{1,3})\s*-?\s*(\d{4})\b",
        re.IGNORECASE
    )

    labels = [
        "Registration Number",
        "Registration No.",
        "Registration No",
        "Regn. No.",
        "Regn No."
    ]

    for line in lines:

        lower_line = line.lower()

        if not any(
                label.lower() in lower_line
                for label in labels
        ):
            continue

        match = pattern.search(line)

        if match:
            return (
                    match.group(1).upper()
                    + match.group(2)
                    + match.group(3).upper()
                    + match.group(4)
            )

    return None


def collect_address(lines):
    address_labels = [
        "Present Address",
        "Permanent Address",
        "Address"
    ]

    for index, line in enumerate(lines):

        matched = False

        for label in address_labels:
            if label.lower() in line.lower():
                matched = True
                break

        if not matched:
            continue

        address_lines = []

        # Same-line value.
        value = inline_value(line, address_labels)

        if value:
            address_lines.append(value)

        # Following lines.
        for next_index in range(
                index + 1,
                min(index + 6, len(lines))
        ):

            candidate = clean_line(lines[next_index])

            if not candidate:
                continue

            lower_candidate = candidate.lower()

            if any(
                    stop in lower_candidate
                    for stop in [
                        "permanent address",
                        "present address",
                        "name of financer",
                        "rc status",
                        "insurance details",
                        "issuing authority"
                    ]
            ):
                break

            address_lines.append(candidate)

            # Stop after Indian PIN code.
            if re.search(r"\b\d{6}\b", candidate):
                break

        if address_lines:
            return " ".join(address_lines)

    return None


def extract(texts):
    lines = normalize_lines(texts)

    registration_number = extract_registration_number(lines)

    registration_date = value_after_label(
        lines,
        FIELD_LABELS["registrationDate"]
    )

    vehicle_class = value_after_label(
        lines,
        FIELD_LABELS["vehicleClass"]
    )

    model = value_after_label(
        lines,
        FIELD_LABELS["model"]
    )

    fuel_type = value_after_label(
        lines,
        FIELD_LABELS["fuelType"]
    )

    colour = value_after_label(
        lines,
        FIELD_LABELS["colour"]
    )

    chassis_number = value_after_label(
        lines,
        FIELD_LABELS["chassisNumber"]
    )

    engine_number = value_after_label(
        lines,
        FIELD_LABELS["engineNumber"]
    )

    owner_name = value_after_label(
        lines,
        FIELD_LABELS["ownerName"]
    )

    registration_validity = value_after_label(
        lines,
        FIELD_LABELS["registrationValidity"]
    )

    address = collect_address(lines)

    return {
        "registrationNumber": registration_number,
        "registrationDate": registration_date,
        "vehicleClass": vehicle_class,
        "model": model,
        "fuelType": fuel_type,
        "colour": colour,
        "chassisNumber": chassis_number,
        "engineNumber": engine_number,
        "ownerName": owner_name,
        "registrationValidity": registration_validity,
        "address": address
    }