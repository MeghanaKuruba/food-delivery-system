def extract(texts):

    registration_number = None
    business_name = None
    proprietor_name = None
    address = None
    date_of_registration = None
    valid_to = None

    for item in texts:

        text = item["text"].strip()
        upper_text = text.upper()

        if upper_text.startswith("REGISTRATION NUMBER:"):

            registration_number = text.split(":", 1)[1].strip()

        elif upper_text.startswith("1. NAME OF ESTABLISHMENT:"):

            business_name = text.split(":", 1)[1].strip()

        elif upper_text.startswith("2. NAME OF PROPRIETOR:"):

            proprietor_name = text.split(":", 1)[1].strip()

        elif upper_text.startswith("3. ADDRESS:"):

            address = text.split(":", 1)[1].strip()

        elif "10. DATE OF REGISTRATION:" in upper_text:

            date_of_registration = text.split(":", 1)[1].strip()

        elif "11. VALID UP TO:" in upper_text:

            valid_to = text.split(":", 1)[1].strip()

    return {
        "registrationNumber": registration_number,
        "businessName": business_name,
        "proprietorName": proprietor_name,
        "address": address,
        "dateOfRegistration": date_of_registration,
        "validTo": valid_to
    }