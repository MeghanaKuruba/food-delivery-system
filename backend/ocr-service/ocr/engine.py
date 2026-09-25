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

            try:
                rec_texts = res["rec_texts"]
            except Exception:
                rec_texts = []

            try:
                rec_scores = res["rec_scores"]
            except Exception:
                rec_scores = []

            try:
                rec_boxes = res["rec_boxes"]
            except Exception:
                rec_boxes = []

            for index, text in enumerate(rec_texts):

                text = str(text).strip()

                if not text:
                    continue

                confidence = 0.0

                if index < len(rec_scores):
                    try:
                        confidence = float(rec_scores[index])
                    except Exception:
                        confidence = 0.0

                bbox = None

                if index < len(rec_boxes):
                    try:
                        bbox = rec_boxes[index].tolist()
                    except Exception:
                        try:
                            bbox = list(rec_boxes[index])
                        except Exception:
                            bbox = None

                texts.append({
                    "text": text,
                    "confidence": confidence,
                    "bbox": bbox
                })

        return texts