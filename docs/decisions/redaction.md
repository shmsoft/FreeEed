# Redaction — FOIA-first, human-driven (no AI in v1)

**Status:** **direction accepted (2026-10-02); v1 scope superseded (2026-10-04) by
`foia-redaction-mvp.md`.** Direction decided by Mark: **first audience = FOIA / public
records**; **first implementation does not use AI**. That direction still holds. The *scope* of
"v1" below does not — it was narrowed to **manual redaction only** two days later, and manual-only
is what was built and shipped. Read `foia-redaction-mvp.md` for the scope of record.
**Drives:** the "Redaction" pillar in `docs/strategy/FreeEed-2027-Vision.md`; production
requirements in `docs/specs/esi-production-format.md` §9; public-sector appliance customers
(first: an Ohio school district — FOIA/state public-records + FERPA).

> ## What is actually built — as of 2026-11-08
>
> Redaction in FreeEed is **manual, and in preview** (10.8.8-PREVIEW). **Nothing in this record
> may be described as shipping unless it appears in the "built" list below** — on air, in a
> letter, on a web page, or to a customer.
>
> **Built, verified in the published `-daily-` artifact** (implementation is in the **FreeEedUI**
> repo, not this one):
> - **Manual boxes** drawn in the review viewer, per document, with an exemption code
>   (FreeEed #614, #615)
> - **Configurable jurisdiction profiles** — Federal FOIA, FERPA, and an editable state example,
>   as runtime-editable data (`redaction-exemptions.tsv`)
> - **Burn-in on export** — the page is re-rendered to a raster, boxes are burned into the pixels,
>   and the page is written back **image-only, with no text layer at all** (FreeEed #616)
> - A **redaction-log cover page** in the release PDF
>
> **Not built** (each is roadmap, and several are described below as if they were v1):
> - **Pattern rules** (SSN / phone / email / DOB / address / account numbers, custom regex) —
>   see "Ways to create redactions", item 2
> - **Name / term lists** — item 3
> - **Propose / accept review queue** — item 4; every saved box is `source=manual`,
>   `status=accepted`, hardcoded
> - **Whole-page / whole-document withholding** — item 5
> - **OCR of the redacted image**, so the release is **not text-searchable**
> - **The automatic verification gate** (re-extract text from the output, fail on any text inside
>   a box)
> - **Audit trail** of who added, accepted or removed each redaction (FreeEedUI #81)
> - **AI-assisted detection of what to redact** — nothing anywhere in the product identifies
>   candidate personal data for redaction. The exemption profiles are a **picklist for a human**,
>   not a detector.
>
> On the last point, from the Jason Baron session (2026-10-08): the defensible line is
> **"we find the candidates, the officer makes the call, because legally only they can."** A b(6)
> withholding requires a privacy-against-public-interest balancing test that a records officer
> cannot delegate to software. Today FreeEed does not even find the candidates — a human does
> both halves. That is a smaller claim, and it is true.

## Problem (as stated 2026-10-02)
FreeEed can collect, process, search, review and produce, but **cannot redact**. Public-records
offices must release the non-exempt portions of records and withhold exempt content with a
cited legal basis. Without redaction, FreeEed can't complete a FOIA response, which is the core
job for the school/agency audience.

## Current state at the time of this record (2026-10-02 — historical; see the box above)
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

## Ways to create redactions (as originally scoped for v1, no AI)
**Only item 1 was built.** Items 2–5 were moved out of v1 by `foia-redaction-mvp.md`
(2026-10-04) and remain unbuilt as of 2026-11-08. They are kept here as the design for when
they are picked up — not as a description of the product.

1. **Manual box** — draw a rectangle on the page image in the review viewer, pick a code.
   **[BUILT — #614/#615]**
2. *(not built)* **Pattern rules** — built-ins: SSN, phone, email, date of birth, street address,
   account/card numbers; plus **customer-defined regexes** (e.g. a district's student-ID format).
   Each match on the page text → a *proposed* redaction with the rule's default code.
3. *(not built)* **Name / term lists** — paste a list (student roster, minors, informants); every occurrence
   → proposed redaction with a chosen code. Expected to be the biggest time-saver for schools.
4. *(not built)* **Review queue** — step through proposed redactions; accept/reject singly or in bulk.
   Only accepted redactions are applied.
5. *(not built)* **Withhold page / document** — with a code; produces a placeholder page in the release.

Locating text on a page (for rules 2–3) uses the text layer for born-digital PDFs and Tesseract
word boxes (hOCR/TSV) for scans/images.

## Applying redactions (production)
For each released document with accepted redactions:
1. Render each page to an image (PDFBox) at production DPI.
2. **Burn in** each box (filled, with the exemption code printed in it).
3. Rebuild the page as image-only PDF (no original text layer, no original metadata/XMP,
   no attachments/annotations/forms carried over).
4. *(not built)* **OCR the redacted image** → the released text (spec §9). Because this step is
   missing, **the current release is image-only and not text-searchable.** That is the safe
   direction to be wrong in: a missing text layer is an inconvenience, a leaked one is a
   career-ending FOIA mistake.
5. *(not built)* **Verification gate (automatic):** re-extract text from the output and confirm no
   text lies inside any redaction box; fail the production on violation. Until this exists, the
   copy/paste check is a **manual release-test step and Mark's to run.**
6. *(not built)* Set `REDACTION=Yes`; apply page/Bates numbering.

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

## Phases (revised 2026-11-08 to match what was built)
1. **Shipped MVP (preview):** manual boxes, jurisdiction-configurable codes, burn-in production
   (image-only pages), redaction-log cover page. — *FreeEed #614/#615/#616.*
   Originally this phase also listed pattern rules, name lists, the review queue, OCR text and
   the verification gate; `foia-redaction-mvp.md` moved all of those out on 2026-10-04.
2. **Finish the MVP workflow:** CSV redaction log (#617), one-click FOIA preset (#618),
   verification gate, OCR re-layer so the release is searchable, audit trail (FreeEedUI #81).
3. **Assisted detection, human-approved** — pattern rules, name/term lists, and the
   propose/accept review queue. The data model below already has `source` and `status` for this.
4. **Local-model candidate detection**, human-approved (`docs/decisions/local-ai-architecture.md`).
   A **personal-data span detector that is exemption-agnostic**, not an exemption classifier:
   the same engine serves US public records, EU/UK DSAR and US state privacy requests, and it
   never makes a withhold/release decision.

## Acceptance — as originally written (describes the superseded scope)
- A test PDF with known SSNs and a roster name: rules propose them, reviewer accepts, release
  PDF shows labeled boxes; **copy/paste and text extraction of the release return no redacted
  content**; released text equals OCR of the redacted image; exemption log lists each code/page.
- A scanned (image-only) page with an SSN is found via OCR word boxes and redacted the same way.
- Originals' hashes unchanged after production.

## Acceptance — for what was actually built (manual only)
- A reviewer draws boxes on a document in the review viewer, tags each with a code from the
  case's profile, saves, reloads the document, and the boxes come back.
- Exporting the release set produces a PDF whose redacted pages show the box with its exemption
  code printed on it, and **copy/paste and text extraction of the release return no redacted
  content** — which here is guaranteed structurally, because the released page has no text layer.
- The release carries a redaction-log cover page naming the case, the document count and the
  redaction count.
- Originals are untouched: the boxes live in a separate per-case `redactions.tsv`, and nothing is
  written back to the processed output.
- **Not covered by any automated test.** The copy/paste check above is run by hand at release
  time. See phase 2.

## Decisions (Mark, 2026-10-02)
- **Release format:** default to **one combined PDF** per release (page/Bates numbered);
  per-document PDFs remain an option.
- **State exemption lists:** maintained by the **customer's counsel**. FreeEed ships the
  editable profile mechanism (plus federal FOIA and FERPA lists); counsel supplies/edits the
  state codes. We don't author state legal content.
- **Sequencing:** build on the **current engine** now; don't wait for the processing-engine
  refactor (`docs/decisions/processing-engine.md`). Keep the burn-in/OCR/verification code
  self-contained so it can move with the refactor.
