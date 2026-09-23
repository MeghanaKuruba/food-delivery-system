import re


GST_PATTERN = re.compile(
    r"\b[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][A-Z0-9]Z[A-Z0-9]\b"
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

    gst_number = None
    legal_name = None
    trade_name = None
    business_address_parts = []

    collecting_address = False

    for index, item in enumerate(texts):

        text = item["text"].strip()
        upper_text = text.upper()

        match = GST_PATTERN.search(upper_text)

        if match:
            gst_number = match.group()

        if "LEGAL NAME" in upper_text:

            legal_name = get_value(texts, index)

        elif "TRADE NAME" in upper_text:

            trade_name = get_value(texts, index)

        elif "ADDRESS OF" in upper_text:

            collecting_address = True

            if ":" in text:
                value = text.split(":", 1)[1].strip()

                if value:
                    business_address_parts.append(value)

            continue

        elif collecting_address:

            if (
                    "DATE OF LIABILITY" in upper_text
                    or "PERIOD OF VALIDITY" in upper_text
                    or "TYPE OF REGISTRATION" in upper_text
            ):
                collecting_address = False
                continue

            business_address_parts.append(text)

    business_address = (
        " ".join(business_address_parts).strip()
        if business_address_parts
        else None
    )

    return {
        "gstNumber": gst_number,
        "legalName": legal_name,
        "tradeName": trade_name,
        "businessAddress": business_address
    }