from extractors import pan
from extractors import gst
from extractors import fssai
from extractors import business_registration
from extractors import driving_license
from extractors import rc
from extractors import insurance


EXTRACTORS = {
    "PAN": pan.extract,
    "GST": gst.extract,
    "FSSAI": fssai.extract,
    "BUSINESS_REGISTRATION": business_registration.extract,
    "DRIVING_LICENSE": driving_license.extract,
    "RC": rc.extract,
    "INSURANCE": insurance.extract
}


def extract_fields(document_type, texts):

    document_type = document_type.upper()

    extractor = EXTRACTORS.get(document_type)

    if not extractor:
        raise ValueError(
            f"Unsupported document type: {document_type}"
        )

    return extractor(texts)