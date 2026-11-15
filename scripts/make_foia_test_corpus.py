#!/usr/bin/env python3
"""Generate the fixed FOIA test corpus used by docs/foia-release-test-plan.md.

Why this exists: the Windows FOIA path is the least-verified surface in the
product and the one the release is judged on. Testing it must not depend on
whichever documents somebody happens to have lying around -- the result has to
mean the same thing every time, on every machine, run by anyone. So the corpus
is generated from this script, and every file in it exists to exercise one
named failure mode with a stated expected result (see the README it writes).

The sensitive strings are identical in every file and sit at known positions, so
"did the box land in the right place?" is a look, not a judgement call.

Usage:  python3 scripts/make_foia_test_corpus.py [outdir]
Default outdir: freeeed-processing/test-data/11-foia-corpus, which is packaged
into the installer -- so a tester on a fresh Windows box already has the corpus
and nobody has to remember to send it. That is the point: the test asset travels
with the build. Regenerate and commit the result when a file changes; the
generator, not the binaries, is the source of truth.

Layout:
  11-foia-corpus/README.md      per-file checklist with expected results
  11-foia-corpus/documents/     the 8 documents -- point a project here
  11-foia-corpus/zip-input/     evidence-bundle.zip alone, for the #589 run

Requires: reportlab, python-docx, Pillow, and soffice on PATH for the
.doc/.odt/.xlsx conversions. Missing soffice degrades gracefully: the PDFs and
.docx are still produced and the README says which files are absent.
"""
import os
import shutil
import subprocess
import sys
import zipfile

from reportlab.lib.pagesizes import LETTER
from reportlab.lib.utils import ImageReader
from reportlab.pdfgen import canvas
from PIL import Image, ImageDraw, ImageFont
import docx

# The strings under test. Identical everywhere on purpose.
SSN = "SSN: 123-45-6789"
NAME = "Student: Jane Q. Roe"
PHONE = "Cell: (614) 555-0142"
CONTROL = "Agency response letter -- this line is NOT to be redacted."

W, H = LETTER


def _line(c, y_from_top, text, size=14):
    c.setFont("Helvetica", size)
    c.drawString(72, H - y_from_top, text)


def response_letter(path):
    """Single page, real text layer. The primary redaction target.

    Sensitive lines are at 110/155/200 pt from the top, i.e. 13.9% / 19.6% /
    25.3% of page height -- so a box that lands more than ~2% off is visible to
    the naked eye against the line it was drawn over.
    """
    c = canvas.Canvas(path, pagesize=LETTER)
    _line(c, 72, "CITY OF EXAMPLE -- PUBLIC RECORDS RESPONSE", 16)
    _line(c, 110, SSN)
    _line(c, 155, NAME)
    _line(c, 200, PHONE)
    _line(c, 300, CONTROL)
    _line(c, 340, "Released in part under Exemption (b)(6).", 11)
    c.showPage()
    c.save()


def multipage_memo(path):
    """Three pages, sensitive content on pages 2 and 3 only.

    Page 1 is deliberately clean. Per-page coordinates are where an off-by-one
    in page indexing shows up, and a single-page test cannot see it.
    """
    c = canvas.Canvas(path, pagesize=LETTER)
    _line(c, 72, "INTERNAL MEMORANDUM -- PAGE 1 OF 3", 16)
    _line(c, 120, "This page contains nothing exempt. Nothing here should be covered.")
    c.showPage()
    _line(c, 72, "INTERNAL MEMORANDUM -- PAGE 2 OF 3", 16)
    _line(c, 110, SSN)
    _line(c, 155, NAME)
    _line(c, 300, CONTROL)
    c.showPage()
    _line(c, 72, "INTERNAL MEMORANDUM -- PAGE 3 OF 3", 16)
    _line(c, 110, PHONE)
    _line(c, 300, CONTROL)
    c.showPage()
    c.save()


def scanned_notice(path, tmpdir):
    """Image-only PDF: no text layer at all, like a real scan.

    Exercises the OCR path, and confirms the Redact tab works on a document
    that never had selectable text. Also a control for the burn-in claim: this
    one starts with no text layer, so "no text in the release" proves nothing
    here -- placement is the only thing that can be checked.
    """
    img = Image.new("RGB", (1700, 2200), "white")
    d = ImageDraw.Draw(img)
    try:
        font = ImageFont.truetype(
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 40)
        big = ImageFont.truetype(
            "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 52)
    except OSError:
        font = big = ImageFont.load_default()
    d.text((150, 150), "SCANNED NOTICE (no text layer)", fill="black", font=big)
    d.text((150, 300), SSN, fill="black", font=font)
    d.text((150, 380), NAME, fill="black", font=font)
    d.text((150, 700), CONTROL, fill="black", font=font)
    png = os.path.join(tmpdir, "_scan.png")
    img.save(png)
    c = canvas.Canvas(path, pagesize=LETTER)
    c.drawImage(ImageReader(png), 0, 0, width=W, height=H)
    c.showPage()
    c.save()
    os.remove(png)


def personnel_docx(path):
    """Word document -- the one that matters for FreeEed #579.

    On Windows, imaging has been observed to silently produce no output for
    Word/Office formats: a native/ entry in the log and never an images/ entry,
    no error. No rendition means the Redact tab has nothing to draw on, so the
    FOIA workflow dead-ends on this file with no message.
    """
    doc = docx.Document()
    doc.add_heading("PERSONNEL RECORD -- EXAMPLE COUNTY", level=1)
    for t in (SSN, NAME, PHONE):
        doc.add_paragraph(t)
    doc.add_paragraph(CONTROL)
    doc.save(path)


def soffice_convert(src, outdir, target):
    """Convert via LibreOffice. Returns the produced path, or None."""
    try:
        subprocess.run(
            ["soffice", "--headless", "--convert-to", target,
             "--outdir", outdir, src],
            check=True, capture_output=True, timeout=180)
    except (OSError, subprocess.SubprocessError):
        return None
    ext = target.split(":")[0]
    produced = os.path.join(
        outdir, os.path.splitext(os.path.basename(src))[0] + "." + ext)
    return produced if os.path.exists(produced) else None


README = """# FOIA Windows test corpus -- generated, do not edit by hand

Regenerate with `python3 scripts/make_foia_test_corpus.py` in the FreeEed repo.
Generated, but committed on purpose: this folder ships inside the installer, so a
tester on a fresh machine already has it. Edit the generator, never these files.

Use with Part C of `docs/foia-release-test-plan.md`. Point a project's input at
`documents/`, tick **Create PDF images** in the Imaging tab (step 2 -- THE TRAP),
and process.

Every file is here to answer one question. Record a result for each row.

| File | What it exercises | Expected | Fails -> |
|---|---|---|---|
{rows}

## The strings, in every file

- `{ssn}`
- `{name}`
- `{phone}`
- `{control}` -- **never redact this one.** It is the control: if it is covered,
  something is over-redacting, and if it survives into the released text layer
  then the burn-in did not rasterize the page.

## Positions, so placement is a look and not a judgement

In `01-response-letter.pdf` the three sensitive lines sit at 110, 155 and 200
points from the top of a 792pt page -- **13.9%, 19.6% and 25.3% of page
height**. A box more than about 2% off is visible against the line it was drawn
over. `{control}` sits at 300pt (37.9%) and must stay clear.

## The zip is an expected failure, not a bug to file

`evidence-bundle.zip` exists to confirm FreeEed **#589** on the build under
test: a `.zip` added directly as a project input produces **zero documents,
silently** on Windows -- no error, "Processing done" logged as success. This is
the exact shape of input a records officer or an instructing solicitor sends.
Add it as an input on its own, process, and confirm whether the case comes back
empty. If it does, #589 is still live and the workaround (unzip first, add the
folder) has to be in the release notes.
"""

ROWS = [
    ("01-response-letter.pdf", "Primary redaction target: real text layer, "
     "sensitive lines at known positions",
     "Renders in Redact tab; boxes land on the lines you drew over; released "
     "PDF has no recoverable text", "placement or burn-in regression"),
    ("02-multipage-memo.pdf", "Per-page coordinates -- page 1 clean, "
     "sensitive content on pages 2 and 3",
     "Boxes on p2/p3 land on the right page and the right line", "off-by-one "
     "in page indexing (invisible in any single-page test)"),
    ("03-scanned-notice.pdf", "Image-only input, no text layer; OCR path",
     "Renders; redactable; text appears under Text/OCR", "OCR or rendition "
     "handling for scans"),
    ("04-personnel-record.docx", "Word imaging on Windows",
     "A PDF rendition exists under images/ and the Redact tab renders it",
     "FreeEed #579 -- silent, no rendition, no error"),
    ("05-legacy-memo.doc", "Legacy binary Word filter", "Same as .docx",
     "#579 for the legacy filter specifically"),
    ("06-field-notes.odt", "ODF filter", "Same as .docx", "#579 for ODF"),
    ("07-roster.xlsx", "Spreadsheet imaging (non-text-flow layout)",
     "Renders, or says clearly that it cannot", "silent no-rendition on "
     "spreadsheets"),
    ("08-plain-note.txt", "Baseline -- the simplest possible input",
     "Renders. If this one fails, imaging is broken outright, not "
     "format-specific", "imaging wholesale"),
    ("evidence-bundle.zip", "Zip as a direct project input",
     "KNOWN FAILURE: expect zero documents, silently (see below)",
     "FreeEed #589 -- confirm whether still live"),
]


def main():
    here = os.path.dirname(os.path.abspath(__file__))
    default = os.path.normpath(os.path.join(
        here, "..", "freeeed-processing", "test-data", "11-foia-corpus"))
    root = os.path.abspath(sys.argv[1]) if len(sys.argv) > 1 else default
    # Documents and the zip live in separate folders deliberately. The zip is a
    # known-failure case (#589); a run that mixes it in with the loose files
    # cannot distinguish "the zip yielded nothing" from "the zip was never read".
    out = os.path.join(root, "documents")
    zip_dir = os.path.join(root, "zip-input")
    for d in (out, zip_dir):
        os.makedirs(d, exist_ok=True)
    print("writing corpus to", root)

    response_letter(os.path.join(out, "01-response-letter.pdf"))
    multipage_memo(os.path.join(out, "02-multipage-memo.pdf"))
    scanned_notice(os.path.join(out, "03-scanned-notice.pdf"), out)
    personnel_docx(os.path.join(out, "04-personnel-record.docx"))

    with open(os.path.join(out, "08-plain-note.txt"), "w") as f:
        f.write("PLAIN TEXT NOTE -- baseline imaging check\n\n")
        for t in (SSN, NAME, PHONE, CONTROL):
            f.write(t + "\n")

    # .doc / .odt from the .docx, and .xlsx from a CSV, via LibreOffice.
    missing = []
    src_docx = os.path.join(out, "04-personnel-record.docx")
    for target, final in (("doc", "05-legacy-memo.doc"),
                          ("odt", "06-field-notes.odt")):
        produced = soffice_convert(src_docx, out, target)
        if produced:
            os.replace(produced, os.path.join(out, final))
        else:
            missing.append(final)

    csv_path = os.path.join(out, "_roster.csv")
    with open(csv_path, "w") as f:
        f.write("Name,SSN,Phone,Note\n")
        f.write('"Jane Q. Roe","123-45-6789","(614) 555-0142","withhold b6"\n')
        f.write('"Public Record","n/a","n/a","release in full"\n')
    produced = soffice_convert(csv_path, out, "xlsx")
    if produced:
        os.replace(produced, os.path.join(out, "07-roster.xlsx"))
    else:
        missing.append("07-roster.xlsx")
    os.remove(csv_path)

    # The zip, for #589. Built from copies so the loose files stay usable too.
    bundle = os.path.join(zip_dir, "evidence-bundle.zip")
    with zipfile.ZipFile(bundle, "w", zipfile.ZIP_DEFLATED) as z:
        for n in ("01-response-letter.pdf", "04-personnel-record.docx",
                  "08-plain-note.txt"):
            p = os.path.join(out, n)
            if os.path.exists(p):
                z.write(p, os.path.join("solicitor-bundle", n))

    rows = "\n".join(
        "| `%s` | %s | %s | %s |" % r for r in ROWS)
    body = README.format(rows=rows, ssn=SSN, name=NAME, phone=PHONE,
                         control=CONTROL)
    if missing:
        body += ("\n## Not produced on this machine\n\nLibreOffice conversion "
                 "was unavailable, so these are absent and their rows cannot "
                 "be tested: " + ", ".join("`%s`" % m for m in missing) + "\n")
    with open(os.path.join(root, "README.md"), "w") as f:
        f.write(body)

    print("\nfiles:")
    for d in (out, zip_dir):
        for n in sorted(os.listdir(d)):
            full = os.path.join(d, n)
            print("  %-36s %8d bytes"
                  % (os.path.relpath(full, root), os.path.getsize(full)))
    if missing:
        print("\nWARNING: not produced:", ", ".join(missing))


if __name__ == "__main__":
    main()
