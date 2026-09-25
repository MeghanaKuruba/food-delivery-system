import re

from ocr.normalizer import (
    normalize_lines,
    value_after_label,
    clean_identifier
)


def extract(texts):

    lines = normalize_lines(texts)

    # --------------------------------------------------
    # REGISTRATION NUMBER
    # --------------------------------------------------

    registration_number = None

    for line in lines:

        # Explicit label + value
        match = re.search(
            r"(?:Regn\.?\s*No\.?|"
            r"Registration\s*(?:No\.?|Number)?)"
            r"\s*[:\-]?\s*"
            r"([A-Z]{2}\d{1,3}[A-Z]{1,3}\d{4})",
            line,
            flags=re.IGNORECASE
        )

        if match:
            registration_number = (
                match.group(1).upper()
            )
            break

    # Standalone registration number
    if not registration_number:

        for line in lines:

            match = re.fullmatch(
                r"\s*([A-Z]{2}\d{1,3}[A-Z]{1,3}\d{4})\s*",
                line.upper()
            )

            if match:

                registration_number = (
                    match.group(1).upper()
                )

                break

    # --------------------------------------------------
    # OWNER
    # --------------------------------------------------

    owner_name = value_after_label(
        lines,
        [
            "Owner's Name",
            "Owners Name",
            "Registered Owner",
            "Regd. Owner",
            "Regd Owner",
            "Owner Name"
        ]
    )

    # --------------------------------------------------
    # VEHICLE CLASS
    # --------------------------------------------------

    vehicle_class = value_after_label(
        lines,
        [
            "Class of Vehicle",
            "Vehicle Class"
        ]
    )

    # --------------------------------------------------
    # MAKE / MODEL
    # --------------------------------------------------

    model = value_after_label(
        lines,
        [
            "Make / Model",
            "Make/Model"
        ]
    )

    # --------------------------------------------------
    # FUEL
    # --------------------------------------------------

    fuel_type = value_after_label(
        lines,
        [
            "Fuel Type",
            "Fuel"
        ]
    )

    # --------------------------------------------------
    # ENGINE
    # --------------------------------------------------

    engine_number = value_after_label(
        lines,
        [
            "Engine No.",
            "Engine No",
            "Engine Number"
        ]
    )

    # --------------------------------------------------
    # CHASSIS
    # --------------------------------------------------

    chassis_number = value_after_label(
        lines,
        [
            "Chassis No.",
            "Chassis No",
            "Chassis Number",
            "Chasis No.",
            "Chasis Number"
        ]
    )

    # --------------------------------------------------
    # MANUFACTURER
    # --------------------------------------------------

    manufacturer = value_after_label(
        lines,
        [
            "Manufacturer",
            "Manufacturar"
        ]
    )

    # The supplied RC uses "Make / Model", so manufacturer
    # is legitimately unavailable in that document.
    if manufacturer in [
        "/ Model",
        "Model",
        None
    ]:
        manufacturer = None

    return {
        "registrationNumber": registration_number,
        "ownerName": owner_name,
        "vehicleClass": vehicle_class,
        "manufacturer": manufacturer,
        "model": model,
        "fuelType": fuel_type,
        "chassisNumber": chassis_number,
        "engineNumber": engine_number
    }