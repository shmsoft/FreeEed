# Redaction — FOIA-first, rule-based (no AI in v1)

**Status:** accepted (2026-10-02). Direction decided by Mark: **first audience = FOIA / public
records**; **first implementation does not use AI**.
**Drives:** the "Redaction" pillar in `docs/strategy/FreeEed-2027-Vision.md`; production
requirements in `docs/specs/esi-production-format.md` §9; public-sector appliance customers
(first: an Ohio school district — FOIA/state public-records + FERPA).

## Problem
FreeEed can collect, process, search, review and produce, but **cannot redact**. Public-records
offices must release the non-exempt portions of records and withhold exempt content with a
cited legal basis. Without redaction, FreeEed can't complete a FOIA response, which is the core
job for the school/agency audience.

## Current state (verified in code, 2026-10-02)
- **No redaction code** in this repo or in FreeEedUI.
- `docs/specs/esi-production-format.md` §9 already sets the output rules: **labeled** boxes
  stating the basis (no plain black boxes); **OCR text of the redacted image**, never the native
  text; **"Withheld" placeholder** for wholly withheld documents; `REDACTION` Yes/No field.
- Building blocks already present: **PDFBox** and **iText** (`freeeed-processing/pom.xml`),
  **Tesseract OCR** (`org.freeeed.ocr.OCRProcessor`), doc→PDF imaging, and FreeEedUI's
  **PII Report** (pattern detection — reusable as a rule source, but **not** its AI path).

## Principles (non-negotiable)
1. **Destroy, don't cover.** A redaction removes the information from every layer of the
   released file. Overlay-only boxes on top of selectable text are a known, public failure mode.
2. **Originals are never modified** (forensic soundness). Redactions are a separate, per-case
   annotation layer; they are applied only when producing a release.
3. **Every redaction cites a basis.** The exemption code is printed on the box and recorded in
   the log.
4. **Nothing is applied without a human decision.** Rules *propose*; a reviewer accepts.
5. **No AI and no outbound network calls** in v1. Every redaction is either drawn by a person
   or produced by a rule the records officer can explain.
6. **Auditable.** Who added/accepted/removed each redaction, and when.

## Data model (annotation layer)
One record per redaction, stored with the case (FreeEedUI side):
`case, docId, page, box (x, y, w, h in page coordinates), exemptionCode, source
(manual | pattern:<rule> | list:<name>), status (proposed | accepted | rejected),
createdBy/at, decidedBy/at`.
Whole-page and whole-document withholding are records with no box (`scope = page | document`).

## Exemption codes — configurable per jurisdiction
- A case selects a **jurisdiction profile**; the profile supplies the code list shown when
  redacting and printed on boxes.
- Ship profiles for: **Federal FOIA** (5 U.S.C. §552(b)(1)–(b)(9)), **FERPA**
  (20 U.S.C. §1232g), and an **editable state profile** (e.g. Ohio Public Records Act,
  ORC 149.43). Codes are **data, not code** — records officers/counsel can edit them.
  *(State exemption lists are supplied and maintained by the customer's counsel; we don't
  hard-code legal content.)*

## Ways to create redactions (v1, no AI)
1. **Manual box** — draw a rectangle on the page image in the review viewer, pick a code.
2. **Pattern rules** — built-ins: SSN, phone, email, date of birth, street address,
   account/card numbers; plus **customer-defined regexes** (e.g. a district's student-ID format).
   Each match on the page text → a *proposed* redaction with the rule's default code.
3. **Name / term lists** — paste a list (student roster, minors, informants); every occurrence
   → proposed redaction with a chosen code. Expected to be the biggest time-saver for schools.
4. **Review queue** — step through proposed redactions; accept/reject singly or in bulk.
   Only accepted redactions are applied.
5. **Withhold page / document** — with a code; produces a placeholder page in the release.

Locating text on a page (for rules 2–3) uses the text layer for born-digital PDFs and Tesseract
word boxes (hOCR/TSV) for scans/images.

## Applying redactions (production)
For each released document with accepted redactions:
1. Render each page to an image (PDFBox) at production DPI.
2. **Burn in** each box (filled, with the exemption code printed in it).
3. Rebuild the page as image-only PDF (no original text layer, no original metadata/XMP,
   no attachments/annotations/forms carried over).
4. **OCR the redacted image** → the released text (spec §9).
5. **Verification gate (automatic):** re-extract text from the output and confirm no text lies
   inside any redaction box; fail the production on violation.
6. Set `REDACTION=Yes`; apply page/Bates numbering.

Unredacted documents in the same release follow the normal production path.

## Release package
- **One combined release PDF** (default), page/Bates numbered; per-document PDFs as an option.
- **Exemption log** (CSV + PDF): document, page(s), exemption code, count, withheld/partial —
  supports the response letter and appeals.
- Audit log export for the case.

## Out of scope for v1 (produce as images or defer)
- **Spreadsheets** — produce as images (spec §9 allows it); cell-level native redaction later.
- **Email header / metadata fields** (To/From/CC names) — redact on the rendered image in v1;
  field-level redaction later.
- Audio/video.
- **AI-suggested redactions** — later phase, local model only (`docs/decisions/local-ai-architecture.md`),
  still human-approved.

## Where it lives
- **FreeEedUI (browser review):** viewer with box drawing, rule/list setup, review queue,
  annotation storage, jurisdiction profiles.
- **freeeed-processing (engine):** rule matching over page text/OCR boxes, burn-in, OCR,
  verification gate, release + log writing (alongside existing imaging/production).
- **Operator console:** production/release run, as today.

## Phases
1. **MVP:** manual boxes + pattern rules + name lists, jurisdiction-configurable codes, review
   queue, burn-in production with OCR text + verification gate, exemption log.
2. Field-level email/metadata redaction; spreadsheet handling; more release-packaging options;
   redaction carry-over across duplicates.
3. AI-suggested redactions (local-only), human-approved.

## Acceptance (MVP)
- A test PDF with known SSNs and a roster name: rules propose them, reviewer accepts, release
  PDF shows labeled boxes; **copy/paste and text extraction of the release return no redacted
  content**; released text equals OCR of the redacted image; exemption log lists each code/page.
- A scanned (image-only) page with an SSN is found via OCR word boxes and redacted the same way.
- Originals' hashes unchanged after production.

## Decisions (Mark, 2026-10-02)
- **Release format:** default to **one combined PDF** per release (page/Bates numbered);
  per-document PDFs remain an option.
- **State exemption lists:** maintained by the **customer's counsel**. FreeEed ships the
  editable profile mechanism (plus federal FOIA and FERPA lists); counsel supplies/edits the
  state codes. We don't author state legal content.
- **Sequencing:** build on the **current engine** now; don't wait for the processing-engine
  refactor (`docs/decisions/processing-engine.md`). Keep the burn-in/OCR/verification code
  self-contained so it can move with the refactor.
