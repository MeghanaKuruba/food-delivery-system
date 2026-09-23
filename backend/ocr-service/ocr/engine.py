from paddleocr import PaddleOCR


class OcrEngine:

    def __init__(self):
        self.ocr = PaddleOCR(
            lang="en",
            use_doc_orientation_classify=False,
            use_doc_unwarping=False,
            use_textline_orientation=False
        )

    def extract_text(self, image_path):

        result = self.ocr.predict(image_path)

        texts = []

        for res in result:

            for text, score in zip(
                    res["rec_texts"],
                    res["rec_scores"]
            ):

                text = text.strip()

                if text:
                    texts.append({
                        "text": text,
                        "confidence": float(score)
                    })

        return texts