# OCR Service

Python-based OCR service for the Food Delivery System, built using **PaddleOCR**.

The service extracts text from document images and is designed to support different document types in the future.

## Tech Stack

* Python 3.13.x
* PaddleOCR
* PaddlePaddle
* OpenCV
* Pillow
* NumPy

## Setup

### 1. Install Python

Download and install **Python 3.13.x** from the official Python website:

[Download Python 3.13.x](https://www.python.org/downloads/?utm_source=chatgpt.com)

During installation on Windows, make sure to enable:

```text
Add Python to PATH
```

Verify the installation:

```powershell
python --version
```

Expected:

```text
Python 3.13.x
```

### 2. Open the OCR Service

From the project root:

```powershell
cd backend/ocr-service
```

### 3. Create a Virtual Environment

```powershell
python -m venv venv
```

A `venv` folder will be created inside the OCR service.

### 4. Activate the Virtual Environment

Windows PowerShell:

```powershell
.\venv\Scripts\Activate.ps1
```

You should see `(venv)` at the beginning of the terminal prompt.

### 5. Install Dependencies

```powershell
pip install -r requirements.txt
```

This installs PaddleOCR and all other required Python dependencies.

### 6. Run the OCR Service

The current implementation is a local OCR test script:

```powershell
python test_ocr.py
```

It processes the sample document image and prints the extracted text, confidence scores, and currently supported structured fields.

## Current Flow

```text
Document Image
      ↓
   PaddleOCR
      ↓
Extracted Text + Confidence
      ↓
Structured Field Extraction
```

## Current Status

* Python environment setup ✅
* PaddleOCR integration ✅
* Text and confidence extraction ✅
* Initial structured field extraction ✅
* Sample document testing ✅

## Next Steps

* Convert the OCR implementation into a REST API
* Accept document uploads
* Add image preprocessing
* Support multiple document types
* Improve extraction and validation
* Integrate with the Spring Boot backend
* Dockerize the service
