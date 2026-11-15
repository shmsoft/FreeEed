# FOIA Windows test corpus -- generated, do not edit by hand

Regenerate with `python3 scripts/make_foia_test_corpus.py` in the FreeEed repo.
Generated, but committed on purpose: this folder ships inside the installer, so a
tester on a fresh machine already has it. Edit the generator, never these files.

Use with Part C of `docs/foia-release-test-plan.md`. Point a project's input at
`documents/`, tick **Create PDF images** in the Imaging tab (step 2 -- THE TRAP),
and process.

Every file is here to answer one question. Record a result for each row.

| File | What it exercises | Expected | Fails -> |
|---|---|---|---|
| `01-response-letter.pdf` | Primary redaction target: real text layer, sensitive lines at known positions | Renders in Redact tab; boxes land on the lines you drew over; released PDF has no recoverable text | placement or burn-in regression |
| `02-multipage-memo.pdf` | Per-page coordinates -- page 1 clean, sensitive content on pages 2 and 3 | Boxes on p2/p3 land on the right page and the right line | off-by-one in page indexing (invisible in any single-page test) |
| `03-scanned-notice.pdf` | Image-only input, no text layer; OCR path | Renders; redactable; text appears under Text/OCR | OCR or rendition handling for scans |
| `04-personnel-record.docx` | Word imaging on Windows | A PDF rendition exists under images/ and the Redact tab renders it | FreeEed #579 -- silent, no rendition, no error |
| `05-legacy-memo.doc` | Legacy binary Word filter | Same as .docx | #579 for the legacy filter specifically |
| `06-field-notes.odt` | ODF filter | Same as .docx | #579 for ODF |
| `07-roster.xlsx` | Spreadsheet imaging (non-text-flow layout) | Renders, or says clearly that it cannot | silent no-rendition on spreadsheets |
| `08-plain-note.txt` | Baseline -- the simplest possible input | Renders. If this one fails, imaging is broken outright, not format-specific | imaging wholesale |
| `evidence-bundle.zip` | Zip as a direct project input | KNOWN FAILURE: expect zero documents, silently (see below) | FreeEed #589 -- confirm whether still live |

## The strings, in every file

- `SSN: 123-45-6789`
- `Student: Jane Q. Roe`
- `Cell: (614) 555-0142`
- `Agency response letter -- this line is NOT to be redacted.` -- **never redact this one.** It is the control: if it is covered,
  something is over-redacting, and if it survives into the released text layer
  then the burn-in did not rasterize the page.

## Positions, so placement is a look and not a judgement

In `01-response-letter.pdf` the three sensitive lines sit at 110, 155 and 200
points from the top of a 792pt page -- **13.9%, 19.6% and 25.3% of page
height**. A box more than about 2% off is visible against the line it was drawn
over. `Agency response letter -- this line is NOT to be redacted.` sits at 300pt (37.9%) and must stay clear.

## The zip is an expected failure, not a bug to file

`evidence-bundle.zip` exists to confirm FreeEed **#589** on the build under
test: a `.zip` added directly as a project input produces **zero documents,
silently** on Windows -- no error, "Processing done" logged as success. This is
the exact shape of input a records officer or an instructing solicitor sends.
Add it as an input on its own, process, and confirm whether the case comes back
empty. If it does, #589 is still live and the workaround (unzip first, add the
folder) has to be in the release notes.
