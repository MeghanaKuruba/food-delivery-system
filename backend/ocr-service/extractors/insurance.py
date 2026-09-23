import re


DATE_PATTERN = re.compile(
    r"\b\d{2}[-/]\d{2}[-/]\d{4}\b"
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

    policy_number = None
    insurer_name = None
    vehicle_registration_number = None
    insured_name = None
    valid_from = None
    valid_to = None

    for index, item in enumerate(texts):

        text = item["text"].strip()
        upper_text = text.upper()

        if "POLICY NUMBER" in upper_text:

            policy_number = get_value(texts, index)

        elif "FROM:" in upper_text:

            match = DATE_PATTERN.search(text)

            if match:
                valid_from = match.group()

        elif "TO:" in upper_text:

            match = DATE_PATTERN.search(text)

            if match:
                valid_to = match.group()

        elif "INSURED'S NAME" in upper_text:

            insured_name = get_value(texts, index)

        elif "VEHICLE REGISTRATION NO" in upper_text:

            vehicle_registration_number = get_value(
                texts,
                index
            )

        elif "INSURANCE COMPANY" in upper_text:

            insurer_name = get_value(texts, index)

    return {
        "policyNumber": policy_number,
        "insurerName": insurer_name,
        "vehicleRegistrationNumber": vehicle_registration_number,
        "insuredName": insured_name,
        "validFrom": valid_from,
        "validTo": valid_to
    }