import re


REGISTRATION_PATTERN = re.compile(
    r"\b[A-Z]{2}\d{1,2}[A-Z]{1,3}\d{1,4}\b",
    re.IGNORECASE
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

    registration_number = None
    owner_name = None
    vehicle_class = None
    manufacturer = None
    model = None
    fuel_type = None
    chassis_number = None
    engine_number = None

    for index, item in enumerate(texts):

        text = item["text"].strip()
        upper_text = text.upper()

        # Registration number
        registration_match = REGISTRATION_PATTERN.search(text)

        if registration_match and registration_number is None:
            registration_number = registration_match.group().upper()

        # Owner
        if "OWNER'S NAME" in upper_text:
            owner_name = get_value(texts, index)

        # Make / Model
        elif "MAKE / MODEL" in upper_text:
            model = get_value(texts, index)

        # Fuel
        elif "FUEL TYPE" in upper_text:
            fuel_type = get_value(texts, index)

        # Engine
        elif "ENGINE NO" in upper_text:
            engine_number = get_value(texts, index)

        # Chassis
        elif "CHASSIS NO" in upper_text:
            chassis_number = get_value(texts, index)

        # Vehicle class
        elif "CLASS OF VEHICLE" in upper_text:
            vehicle_class = get_value(texts, index)

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