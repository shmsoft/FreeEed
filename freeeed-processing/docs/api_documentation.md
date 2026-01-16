# AI Advisor API Documentation

Base URL: `http://localhost:8000` (default)

## Endpoints

### Question Answering

#### `GET /question_case/`
Ask AIAdvisor about a single case.
- **Parameters**:
  - `question` (string, required): The question to ask.
  - `case_id` (string, required): The ID of the case to query.
- **Response**: JSON object containing the question, answer, and sources.

#### `GET /question_cases/`
Ask AIAdvisor about multiple cases.
- **Parameters**:
  - `question` (string, required): The question to ask.
  - `case_ids` (list of strings, required): List of case IDs to query.
- **Response**: JSON object containing the question, answer, and combined sources.

### Index Management

#### `POST /clean_case_index/`
Clean up (delete and recreate) an index for a case.
- **Parameters**:
  - `case_id` (form data, required): The ID of the case to clean.
- **Response**: Success message.

#### `GET /describe_index/`
Describe Chroma collections or a specific case collection.
- **Parameters**:
  - `case_id` (string, optional): If provided, describes that specific case. If omitted, lists all collections.
- **Response**: JSON object or list of objects with collection details (name, count).

### Content Ingestion

#### `POST /store_document/`
Store a single uploaded document.
- **Parameters**:
  - `case_id` (form data, required): The ID of the case.
  - `document` (file, required): The file to upload.
  - `document_id` (form data, optional): Custom ID/source name for the document. Defaults to filename.
- **Response**: JSON object with number of chunks indexed.

#### `POST /store_content/`
Store raw text content.
- **Parameters**:
  - `case_id` (form data, required): The ID of the case.
  - `content` (form data, required): The raw text content.
  - `source` (form data, required): Source identifier (e.g., filename).
- **Response**: JSON object with number of chunks indexed.

#### `POST /store_zip_texts/`
Store all text files from a ZIP archive.
- **Requirements**: The ZIP file must contain a `text/` folder with `.txt` files.
- **Parameters**:
  - `case_id` (form data, required): The ID of the case.
  - `zip_file` (file, required): The ZIP file to upload.
- **Response**: JSON object with number of files and chunks indexed.
