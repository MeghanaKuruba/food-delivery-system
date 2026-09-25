import re


def clean_text(value):
    if value is None:
        return None

    value = str(value)
    value = value.replace("\n", " ")
    value = re.sub(r"\s+", " ", value)

    return value.strip(" :;-|\\")


def normalize_label(value):
    value = clean_text(value)

    if not value:
        return ""

    value = value.lower()

    value = value.replace(":", " ")
    value = value.replace(".", " ")
    value = value.replace("/", " ")
    value = value.replace("-", " ")

    value = re.sub(r"\s+", " ", value)

    return value.strip()


def normalize_lines(texts):
    return [
        clean_text(item["text"])
        for item in texts
        if clean_text(item["text"])
    ]


def same_line_value(line, aliases):
    """
    Extract value when label and value are on the same OCR line.

    Example:
        Owner Name: RAHUL SHARMA
        Fuel Type: PETROL
    """

    original = clean_text(line)

    if not original:
        return None

    normalized_original = normalize_label(original)

    # Longest alias first
    sorted_aliases = sorted(
        aliases,
        key=lambda x: len(normalize_label(x)),
        reverse=True
    )

    for alias in sorted_aliases:

        normalized_alias = normalize_label(alias)

        if not normalized_alias:
            continue

        # Exact label only or label followed by separator.
        pattern = (
                r"^\s*"
                + re.escape(normalized_alias)
                + r"(?:\s*[:\-]\s*|\s+)(.*)$"
        )

        match = re.match(
            pattern,
            normalized_original,
            flags=re.IGNORECASE
        )

        if match:

            value = clean_text(match.group(1))

            if value:
                return value

    return None


def exact_or_starts_with_label(line, aliases):
    """
    Checks whether an OCR line represents one of the labels.
    Avoids dangerous substring matching.

    For example:
        'Name' matches 'Name'
        'Owner Name' does NOT match 'Name'
    """

    normalized_line = normalize_label(line)

    for alias in aliases:

        normalized_alias = normalize_label(alias)

        if not normalized_alias:
            continue

        if normalized_line == normalized_alias:
            return True

        if normalized_line.startswith(
                normalized_alias + " "
        ):
            return True

    return False


def next_line_value(lines, index, aliases):

    for next_index in range(
            index + 1,
            min(index + 3, len(lines))
    ):

        candidate = clean_text(lines[next_index])

        if not candidate:
            continue

        # Don't take another label as a value.
        if exact_or_starts_with_label(
                candidate,
                aliases
        ):
            continue

        return candidate

    return None


def value_after_label(lines, aliases):
    """
    Safe field extraction.

    Priority:
    1. Label + value on same line
    2. Label on one line + value on next line

    Does NOT use substring matching such as
    'Name' inside 'Owner Name'.
    """

    for index, line in enumerate(lines):

        value = same_line_value(
            line,
            aliases
        )

        if value:
            return value

        normalized_line = normalize_label(line)

        for alias in aliases:

            normalized_alias = normalize_label(alias)

            if normalized_line == normalized_alias:

                value = next_line_value(
                    lines,
                    index,
                    aliases
                )

                if value:
                    return value

    return None


def multiline_value_after_label(
        lines,
        aliases,
        stop_aliases,
        max_lines=8
):
    """
    Used mainly for addresses.

    Handles:
        Address:
        line 1
        line 2

    Stops when another known field starts.
    """

    for index, line in enumerate(lines):

        normalized_line = normalize_label(line)

        matched = False

        for alias in aliases:

            normalized_alias = normalize_label(alias)

            if normalized_line == normalized_alias:
                matched = True
                break

        if not matched:
            continue

        values = []

        for next_index in range(
                index + 1,
                min(index + 1 + max_lines, len(lines))
        ):

            candidate = clean_text(
                lines[next_index]
            )

            if not candidate:
                continue

            normalized_candidate = normalize_label(
                candidate
            )

            # Stop at another field.
            stop = False

            for stop_alias in stop_aliases:

                normalized_stop = normalize_label(
                    stop_alias
                )

                if (
                        normalized_candidate == normalized_stop
                        or normalized_candidate.startswith(
                    normalized_stop + " "
                )
                ):
                    stop = True
                    break

            # Stop at numbered sections.
            if re.match(
                    r"^\d+\s*[\.\):\-]",
                    candidate
            ):
                stop = True

            if stop:
                break

            values.append(candidate)

        if values:
            return " ".join(values)

    return None


def clean_identifier(value):

    if not value:
        return None

    value = value.strip()

    value = value.replace("\\", "")
    value = value.lstrip(":").strip()

    return value if value else None