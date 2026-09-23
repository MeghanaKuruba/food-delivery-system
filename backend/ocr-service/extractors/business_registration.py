def extract(texts):

    registration_number = None
    business_name = None
    proprietor_name = None
    address = None
    date_of_registration = None
    valid_to = None

    for index, item in enumerate(texts):

        text = item["text"].strip()
        upper_text = text.upper()

        # Registration number
        if "REGISTRATION NUMBER:" in upper_text:

            value = text.split(":", 1)[1].strip()

            if value:
                registration_number = value

        # Name of establishment
        elif "NAME OF ESTABLISHMENT:" in upper_text:

            value = text.split(":", 1)[1].strip()

            if value:
                business_name = value

            elif index + 1 < len(texts):
                business_name = texts[index + 1]["text"].strip()

        # Proprietor
        elif "NAME OF PROPRIETOR:" in upper_text:

            value = text.split(":", 1)[1].strip()

            if value:
                proprietor_name = value

            elif index + 1 < len(texts):
                proprietor_name = texts[index + 1]["text"].strip()

        # Address
        elif upper_text.startswith("3. ADDRESS"):

            value = text.split(":", 1)[1].strip()

            if value:
                address = value

            elif index + 1 < len(texts):
                address = texts[index + 1]["text"].strip()

        # Date of registration
        elif "DATE OF REGISTRATION:" in upper_text:

            value = text.split(":", 1)[1].strip()

            if value:
                date_of_registration = value

            elif index + 1 < len(texts):
                date_of_registration = texts[index + 1]["text"].strip()

        # Valid up to
        elif "VALID UP TO:" in upper_text:

            value = text.split(":", 1)[1].strip()

            if value:
                valid_to = value

            elif index + 1 < len(texts):
                valid_to = texts[index + 1]["text"].strip()

    return {
        "registrationNumber": registration_number,
        "businessName": business_name,
        "proprietorName": proprietor_name,
        "address": address,
        "dateOfRegistration": date_of_registration,
        "validTo": valid_to
    }