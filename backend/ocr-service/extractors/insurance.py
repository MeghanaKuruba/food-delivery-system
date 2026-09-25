import re

from ocr.normalizer import (
    normalize_lines,
    value_after_label
)


def extract(texts):

    lines = normalize_lines(texts)

    vehicle_registration_number = value_after_label(
        lines,
        [
            "Registration Number",
            "Registration No",
            "Vehicle Registration Number",
            "Vehicle Regn No"
        ]
    )

    owner_name = value_after_label(
        lines,
        [
            "Owner Name",
            "Registered Owner",
            "Insured Name"
        ]
    )

    insurance_company = value_after_label(
        lines,
        [
            "Insurance Company",
            "Insurer Name",
            "Insurer"
        ]
    )

    policy_number = value_after_label(
        lines,
        [
            "Policy Number",
            "Policy No",
            "Policy No."
        ]
    )

    valid_until = value_after_label(
        lines,
        [
            "Valid Upto",
            "Valid Up To",
            "Valid Until",
            "Validity",
            "Policy Validity"
        ]
    )

    valid_from = value_after_label(
        lines,
        [
            "Valid From",
            "Policy Start Date",
            "Start Date"
        ]
    )

    # Sometimes registration number is badly positioned
    # and appears in a line without being captured by the label logic.
    if not vehicle_registration_number:

        for line in lines:

            match = re.search(
                r"\b([A-Z]{2}\d{1,2}[A-Z]{1,3}\d{4})\b",
                line.upper()
            )

            if match:
                vehicle_registration_number = match.group(1)
                break

    return {
        "policyNumber": policy_number,
        "insuranceCompany": insurance_company,
        "vehicleRegistrationNumber":
            vehicle_registration_number,
        "insuredName": owner_name,
        "validFrom": valid_from,
        "validTo": valid_until
    }