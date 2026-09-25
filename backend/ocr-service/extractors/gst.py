import re

from ocr.normalizer import (
    normalize_lines,
    value_after_label,
    multiline_value_after_label
)


def extract(texts):

    lines = normalize_lines(texts)

    # --------------------------------------------------
    # GST NUMBER
    # --------------------------------------------------

    gst_number = None

    for line in lines:

        match = re.search(
            r"\b\d{2}[A-Z]{5}\d{4}[A-Z][A-Z0-9]Z[A-Z0-9]\b",
            line.upper()
        )

        if match:
            gst_number = match.group(0)
            break

    # --------------------------------------------------
    # LEGAL NAME
    # --------------------------------------------------

    legal_name = value_after_label(
        lines,
        [
            "Legal Name"
        ]
    )

    # --------------------------------------------------
    # TRADE NAME
    # --------------------------------------------------

    trade_name = value_after_label(
        lines,
        [
            "Trade Name, if any",
            "Trade Name"
        ]
    )

    # If OCR captured the label fragment itself,
    # treat it as no value.
    if trade_name:

        normalized_trade_name = (
            trade_name
            .strip()
            .lower()
        )

        if normalized_trade_name in [
            ", if any",
            "if any"
        ]:
            trade_name = None

    # --------------------------------------------------
    # ADDRESS
    # --------------------------------------------------

    business_address = None

    for index, line in enumerate(lines):

        normalized = line.lower()

        if (
                "address of principal place of" in normalized
        ):

            values = []

            # The GST document places "Business"
            # on the label's continuation line.
            # We only take the actual address values.
            for next_index in range(
                    index + 1,
                    min(index + 4, len(lines))
            ):

                candidate = lines[next_index]

                if not candidate:
                    continue

                # Skip the word "Business" when it is
                # the continuation of the label.
                if candidate.strip().lower() == "business":
                    continue

                # Stop at next numbered section.
                if re.match(
                        r"^\d+\s*[\.\):\-]",
                        candidate
                ):
                    break

                values.append(candidate)

            if values:
                business_address = " ".join(values)

            break

    return {
        "gstNumber": gst_number,
        "legalName": legal_name,
        "tradeName": trade_name,
        "businessAddress": business_address
    }