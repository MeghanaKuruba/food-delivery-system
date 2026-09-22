import re


DATE_PATTERN = re.compile(
    r"\b\d{2}[-/]\d{2}[-/]\d{4}\b"
)


def extract(texts):

    policy_number = None
    insurer_name = None
    vehicle_registration_number = None
    insured_name = None
    valid_from = None
    valid_to = None

    for index, item in enumerate(texts):

        text = item["text"].strip()
        upper_text = text.upper()

        # Policy number
        if "POLICY NUMBER:" in upper_text:

            value = text.split(":", 1)[1].strip()

            if value:
                policy_number = value

        # Policy period
        elif "FROM:" in upper_text:

            match = DATE_PATTERN.search(text)

            if match:
                valid_from = match.group()

        elif "TO:" in upper_text:

            match = DATE_PATTERN.search(text)

            if match:
                valid_to = match.group()

        # Insured name
        elif "INSURED'S NAME:" in upper_text:

            insured_name = text.split(":", 1)[1].strip()

        # Vehicle registration
        elif "VEHICLE REGISTRATION NO.:" in upper_text:

            vehicle_registration_number = (
                text.split(":", 1)[1].strip()
            )

        # Insurer
        elif "INSURANCE COMPANY:" in upper_text:

            insurer_name = text.split(":", 1)[1].strip()

    return {
        "policyNumber": policy_number,
        "insurerName": insurer_name,
        "vehicleRegistrationNumber": vehicle_registration_number,
        "insuredName": insured_name,
        "validFrom": valid_from,
        "validTo": valid_to
    }