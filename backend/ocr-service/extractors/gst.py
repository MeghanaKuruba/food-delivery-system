import re


GST_PATTERN = re.compile(
    r"\b[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][A-Z0-9]Z[A-Z0-9]\b"
)


def extract(texts):

    gst_number = None
    legal_name = None
    trade_name = None
    business_address_parts = []

    collecting_address = False

    for item in texts:

        text = item["text"].strip()
        upper_text = text.upper()

        # GSTIN
        match = GST_PATTERN.search(upper_text)

        if match:
            gst_number = match.group()

        # Legal name
        if "LEGAL NAME:" in upper_text:

            if ":" in text:
                legal_name = text.split(":", 1)[1].strip()

        # Trade name
        elif "TRADE NAME:" in upper_text:

            if ":" in text:
                trade_name = text.split(":", 1)[1].strip()

        # Address starts
        if "ADDRESS OF" in upper_text:

            collecting_address = True
            continue

        # Address continues
        if collecting_address:

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