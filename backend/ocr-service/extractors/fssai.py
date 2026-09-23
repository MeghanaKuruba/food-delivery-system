import re


LICENSE_PATTERN = re.compile(
    r"\b\d{14}\b"
)

DATE_PATTERN = re.compile(
    r"\d{2}[/-]\d{2}[/-]\d{4}"
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

    license_number = None
    business_name = None
    premises_address = None
    kind_of_business = None
    license_category = None
    valid_from = None
    valid_to = None

    for index, item in enumerate(texts):

        text = item["text"].strip()
        upper_text = text.upper()

        if "LICENSE NO." in upper_text:

            match = LICENSE_PATTERN.search(text)

            if match:
                license_number = match.group()

        elif "NAME OF LICENSEE" in upper_text:

            business_name = get_value(texts, index)

        elif upper_text.startswith("ADDRESS"):

            premises_address = get_value(texts, index)

        elif "KIND OF BUSINESS" in upper_text:

            kind_of_business = get_value(texts, index)

        elif "LICENSE VALIDITY" in upper_text:

            dates = DATE_PATTERN.findall(text)

            if len(dates) >= 2:

                valid_from = dates[0]
                valid_to = dates[1]

            elif index + 1 < len(texts):

                dates = DATE_PATTERN.findall(
                    texts[index + 1]["text"]
                )

                if len(dates) >= 2:
                    valid_from = dates[0]
                    valid_to = dates[1]

    return {
        "licenseNumber": license_number,
        "businessName": business_name,
        "premisesAddress": premises_address,
        "kindOfBusiness": kind_of_business,
        "licenseCategory": license_category,
        "validFrom": valid_from,
        "validTo": valid_to
    }