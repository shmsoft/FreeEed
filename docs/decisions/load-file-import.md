# Load-file import (DAT/OPT productions) — repair & build-out plan

**Status:** proposed (2026-09-03)
**Drives:** #599 (Relativity import robustness), #519 (DAT delimiter), Jesse London (production interop), and a detailed field report of a Concordance production importing as loose files.

## Problem
Importing a standard Concordance/Relativity production (DAT + OPT + natives/images/text under `VOL*/`) does **not** reconstruct documents. Files ingest as **loose documents** (SOLRID_* IDs), Bates/control numbers don't appear or resolve in search, and image pages / OCR text are standalone rather than grouped into logical documents.

## Current state (verified in code)
- **The DAT/OPT application path is not wired in.** `FreeEedProcess.processLoadFiles()` is an **empty stub** — the switch that instantiates `DATProcessor`/`CSVProcessor`/`JSONProcessor` and calls `processLoadFile()` is entirely commented out. It's reached only when `project.getDataSource() == DATA_SOURCE_LOAD_FILE` (+ `LOAD_FILE_FORMAT`), so a normal project import goes straight to loose-file ingest (`MainRunner.run`).
- **`DATProcessor` exists and parses a `.dat`** (row → metadata; `EXTRACTED TEXT` → text file). Delimiter/qualifier is now **auto-detected** (0x14/tab/pipe + 0xFE/quote), BOM-stripped — commit `63a428e3` (#519/#599). But nothing calls it in the live pipeline.
- **No OPT/Opticon parser** — nothing reads the `.opt` to group image pages into documents or link Bates → images.
- **Bates/Control Number** is set as generic metadata by `DATProcessor` but not mapped to a canonical, searchable Solr field.
- **Staging:** `ActionStaging` already has a `DATA_SOURCE_LOAD_FILE` branch that copies the load file without zipping.

## Goal
Point FreeEed at a production root (or the DAT/OPT) and get **Bates-keyed logical documents** — each with its DAT metadata, OPT image pages, and OCR text — searchable by Bates/control number.

## Plan (phased)

### Phase 1 — Re-wire the DAT importer (make load-file mode do something)
- Rewrite `FreeEedProcess.processLoadFiles()` to select the processor by `LOAD_FILE_FORMAT` (DAT/CSV/JSON) and call `processLoadFile()`.
- Verify `DATProcessor` still fits the current standalone pipeline (SolrIndex, ZipFileWriter, staging dir) — it predates the MR→standalone refactor.
- **Acceptance:** a Load-File/DAT project parses the `.dat` → one document per row with its DAT metadata; log shows the `DAT …: field delimiter 0x…` line.

### Phase 2 — Bates/Control Number: canonical + searchable
- Map the DAT's Bates/control field to a canonical searchable field. Handle common aliases (`Control Number`, `BegBates`, `BEGDOC`, `DOCID`, `Bates/Begin`).
- **Acceptance:** searching `DEF0001425` returns that document.

### Phase 3 — OPT/Opticon support (image-page → document reconstruction) — the big one
- Parse Opticon lines: `IMAGEKEY,VOLUME,FULLPATH,DOCBREAK(Y/N),,,PAGES`. Group consecutive pages (new doc on `DOCBREAK=Y`) into a logical document keyed by the first page's image key (= Bates).
- Join OPT (image key) ↔ DAT (Bates/control number) so each document has metadata + pages + text.
- **Acceptance:** a DAT+OPT production reconstructs into Bates-keyed documents with pages + text — not loose files.

### Phase 4 — Path resolution & robustness
- Resolve backslash relative paths (`.\VOL001\IMAGES\...`) relative to the load-file root, on both OSes.
- BOM/CR/CRLF + delimiter auto-detect (done in `DATProcessor`).
- Malformed/partial references → clear errors, not a silent loose-file fallback.
- **Acceptance:** imports the relativitydev sample productions + the reporter's sample; bad inputs surface clear errors.

### Phase 5 — Discoverability & confirmation (UX)
- The field reporter didn't know to enable load-file mode. Auto-detect a DAT/OPT at the import root and offer/enter load-file mode, or add an explicit "Import a production (DAT/OPT)" path.
- Log clearly whether the DAT parsed and the OPT was applied, so users can confirm.
- **Acceptance:** pointing at a production root prompts/auto-selects load-file mode; logs confirm application.

## Test data
- `~/FreeEed-samples/relativity-import-samples/SampleDataSources/` (already local) — multiple DAT dialects + OPT + EDRM natives/images/text.
- The field reporter's sample DAT/OPT lines (requested).
- Commit a tiny DAT+OPT fixture as a regression test.

## Sequencing & effort
- **Phase 1** = the unblock (small-ish; re-wire + verify). **Phase 2** = small, high value. **Phase 3 (OPT)** = the bulk of the work. **4–5** = polish.
- Suggested first sprint: **1 + 2** (load-file projects parse DAT + Bates searchable), then **3** (OPT reconstruction).

## Relationships
- **#519** delimiter — done (`63a428e3`).
- **#599** import robustness — this plan implements it.
- **Produce phase** (#61 / #554 Bates burn-in / #551 / #556, Jesse): import (reconstruct here) and produce (Bates burn-in there) are the two ends of the same load-file competence.
- **#600** per-document PDF export — separate (export side), shares the imaging renderer.
