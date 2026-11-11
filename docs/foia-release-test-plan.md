# FOIA release test plan — 10.8.8

**Purpose:** confirm that a records officer can take a set of records from ingest to a
defensible redacted release, on an installed build, before 10.8.8 goes to the stable
download channel.

**Scope note.** Redaction in 10.8.8 is **manual**. There is no automated detection of what
to redact — a human finds the content and draws the box. That is the design (see
`docs/decisions/redaction.md`), and this plan tests that path only.

**Why a written plan.** The burn-in is the one thing in this release that cannot be wrong:
a release that looks redacted but still carries the text is a career-ending FOIA mistake for
the customer. The mechanical half of that is now covered by automated harnesses (Part A);
everything the harnesses cannot reach is in Part C, and it needs a person.

---

## Part A — already machine-verified, do not re-test by hand

Two standalone harnesses drive the real shipped export path
(`CaseFileService.mergeRedactedPdfs`) against a synthetic case. **18 checks, all passing**
as of 2026-11-10 against FreeEedUI `c93d34a`.

**Burn-in (content removal) — 10 checks**
- The source PDF has a real text layer before redaction (so the test is meaningful).
- The redacted SSN, name and phone number are **not** recoverable from the release.
- The released document pages carry **no text layer at all** — 0 characters extracted.
- Negative control: the *unredacted* text is gone too, proving the page is a raster and not
  a silently-blank page that would pass every other assertion.
- The release is a real document (log cover page + content page), the cover page is the
  redaction log, names the case, and reports the redaction count.

**Placement — 8 checks**
- The boxed region is **99.0%** opaque fill: the box covers what it was drawn over.
- The box contains white label pixels — it is a *labeled* box carrying its exemption code,
  never a plain black box.
- The *unboxed* line is **10.9%** dark: still ink-on-paper, not blacked out. This is the
  check that catches a y-axis flip, which would otherwise hide the SSN and black out an
  innocent paragraph while passing every content-removal test.

**What this means:** the server-side burn-in is correct. Content is removed, boxes land on
the coordinates they are given, labels are legible, nothing is over-redacted.

**What this does NOT cover:** whether the *browser viewer* sends the right coordinates. The
harnesses feed coordinates directly to the server. If `search.js` measured y from the wrong
origin or against the wrong element, the whole chain would be wrong while the server stays
provably right. **That is Part C step 9, and only a person in a browser can close it.**

---

## Part B — GA gating items (must pass before the stable channel is armed)

`Version.java` reads `10.8.8` — a clean semver, which arms the **`-latest-`** channel. The
next publishing release run therefore **overwrites the public download links**. Do not run it
until B1–B3 and Part C pass.

### B1 — Desktop review credentials (#68)
1. Install the 10.8.8 desktop build. Start it. Click through to open Review.
2. **Expect:** a "Review sign-in" dialog showing the url, username `admin`, and a **real
   generated password** (not the literal word "password", not blank).
3. Sign in to Review with that password. **Expect:** it works.
4. Sign out. Try `admin` / `admin`. **Expect:** rejected.
5. Change the password inside Review, then delete the file the dialog named
   (`work/initial-admin-password.txt`, under the Tomcat directory — the dialog prints the
   absolute path).
6. Open Review from the desktop again. **Expect:** no dialog this time, and the new password
   still signs in.

### B2 — Appliance review credentials (#68)
1. First boot of the appliance image. **Expect:** `/opt/freeeed/review-admin-credentials.txt`
   exists, mode **600**, and the URL inside it carries the appliance's **actual IP**, not
   `localhost` or a placeholder.
2. Sign in to Review from another machine using those credentials. **Expect:** it works.
3. Reboot. **Expect:** the file is **not** regenerated and the password still works.

### B3 — First-run dialogs land in front (#621 item B)
1. Fresh install, first launch. **Expect:** the Edition chooser and the registration dialog
   appear **in front of** the Control Panel, not hidden behind it.
2. Dismiss them, restart. **Expect:** they do not reappear.

---

## Part C — FOIA end-to-end, on an installed build

Do this on the installed build, not from source. A records officer's path.

### 1. Create the case
Create a project, point it at a small record set (a dozen documents is plenty — include at
least one scanned/image-only page and one multi-page PDF).

### 2. Turn on PDF imaging — THE TRAP
Open the **Imaging** tab and tick **Create PDF images**.

> **This is the one step that silently ruins the run.** Without it there are no per-document
> PDF renditions, so the Redact tab has nothing to draw on and the FOIA workflow fails at the
> last step with no warning earlier. The one-click FOIA preset that would set this
> automatically is **not built** (#618). Expect to do it by hand, and expect customers to
> forget.

### 3. Process
Run processing. **Expect:** per-document PDFs appear under the project output's `images/`
folder, named `<uniqueId>_<name>.pdf`.

### 4. Start Review and sign in
Review serves on **port 8090**. Sign in with the generated credentials from B1/B2.

### 5. Open a document and redact
Select the case, search, open a document, open the **Redact** tab.
- Draw a box over something sensitive. **Expect:** an exemption-code picker offering
  **Federal FOIA** (b)(1)–(b)(9), **FERPA**, and the **State (Ohio ORC 149.43)** example.
- Pick a code. Draw a second box on a **different page** of a multi-page document.
- Move one box; delete another. **Expect:** all three gestures work.
- Save.

### 6. Persistence
Navigate away, then reopen the same document. **Expect:** the boxes come back, on the right
pages, with their codes. (They are stored in a per-case `redactions.tsv` beside the case
files — originals are never touched.)

### 7. Exemption list is editable by counsel
Edit `redaction-exemptions.tsv` in the working directory — add a code for your own
jurisdiction. Reload the Redact tab. **Expect:** the new code appears without a redeploy. An
operator copy in the working directory wins over the bundled one.

### 8. Export the release
Use **Export redacted (selected)** on two checked documents, then **Export redacted (all)**.
**Expect:** a PDF whose first page is the redaction log and whose document pages show the
boxes with the exemption code printed on them.

### 9. Verify the release — the step that matters
**9a. Text extraction.** From a terminal:
```
pdftotext release.pdf - | less
```
**Expect:** the log cover page's text, and **nothing at all** from the document pages. If any
redacted content appears, **stop — do not release**, and file it as a blocker.

**9b. Copy/paste.** Open the release in a PDF viewer, select across a redacted area, copy,
paste into a text editor. **Expect:** nothing.

**9c. Placement — this is the part the harnesses cannot do.** Look at each redacted page and
confirm the black boxes are **where you drew them**, over the content you meant, and that
nothing you did not box has been covered. This closes the viewer-coordinate gap described at
the end of Part A. Check at least one box on page 2 or later of a multi-page document —
per-page coordinates are where an off-by-one would show up.

**9d. Log.** Read the cover page. **Expect:** the case name, the document count, the
redaction count, and a line per document listing its codes.

---

## Known limits — expected behavior in 10.8.8, not bugs

Do not file these; they are recorded and scoped.

| Limit | Issue |
|---|---|
| The release is **image-only, so not text-searchable.** Re-OCR of the redacted raster is follow-on work. A missing text layer is an inconvenience; a leaked one is career-ending, so this is the safe direction to be wrong in. | — |
| **No CSV redaction log.** The PDF cover page is a single LETTER page and truncates past roughly 40 entries with "... and N more (full list omitted for space)" — so a large release ships an **incomplete index**, and the index is what the response letter and any appeal are built on. | #617 |
| **No one-click FOIA preset.** See Part C step 2. | #618 |
| **No automatic verification gate.** Nothing re-extracts text from the output and fails the production on a violation. Step 9a is that gate, run by hand. | — |
| **No audit trail** of who added, accepted or removed each box. | FreeEedUI#81 |
| **No propose/accept review queue.** Every saved box is `source=manual`, `status=accepted`. | — |
| **No detection of what to redact.** The exemption profiles are a picklist for a human, not a detector. | roadmap |
| Whole-page / whole-document withholding is not implemented. | — |

---

## Sign-off

| Item | Result | Date | By |
|---|---|---|---|
| A — automated harnesses (18 checks) | PASS | 2026-11-10 | automated |
| B1 — desktop credentials | | | |
| B2 — appliance credentials | | | |
| B3 — first-run dialogs | | | |
| C1–C8 — FOIA workflow | | | |
| C9 — release verification | | | |

**Release gate:** every row above must read PASS before the publishing release run, because
10.8.8 is a clean semver and that run overwrites the public `-latest-` download links.
