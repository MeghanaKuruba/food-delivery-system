def extract(texts):

    registration_number = None
    owner_name = None
    vehicle_class = None
    manufacturer = None
    model = None
    fuel_type = None
    chassis_number = None
    engine_number = None

    for item in texts:

        text = item["text"].strip()
        upper_text = text.upper()

        if upper_text.startswith("OWNER'S NAME:"):

            owner_name = text.split(":", 1)[1].strip()

        elif upper_text.startswith("MAKE / MODEL:"):

            model = text.split(":", 1)[1].strip()

        elif upper_text.startswith("FUEL TYPE:"):

            fuel_type = text.split(":", 1)[1].strip()

        elif upper_text.startswith("ENGINE NO.:"):

            engine_number = text.split(":", 1)[1].strip()

        elif upper_text.startswith("CHASSIS NO.:"):

            chassis_number = text.split(":", 1)[1].strip()

        elif upper_text.startswith("CLASS OF VEHICLE:"):

            vehicle_class = text.split(":", 1)[1].strip()

        # Registration number is usually a standalone value
        elif (
                len(text.replace(" ", "")) >= 8
                and upper_text.replace(" ", "").isalnum()
        ):

            if (
                    registration_number is None
                    and upper_text.replace(" ", "").startswith(("KA", "MH", "DL"))
            ):
                registration_number = text

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