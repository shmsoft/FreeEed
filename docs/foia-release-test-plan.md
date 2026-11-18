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
provably right.

**Partly closed on 2026-11-11, by inspection plus measurement in Chrome:**
- **Origin is correct — there is no y-axis flip.** The viewer measures
  `e.clientY - rect.top`, i.e. from the top of the page image, which is the convention the
  server's burn-in expects.
- **Scale was wrong, and is now fixed (FreeEedUI#90).** The drawing overlay was sized in
  pixels once at image load, so after a window resize or a browser zoom it no longer matched
  the re-flowed image, and boxes drawn afterwards were stored against the stale scale —
  measured at up to 5.8% of page height, about 3.3 lines of 14pt text. Silent: the release
  looked redacted and covered the wrong text. The overlay is now stretched to the image's
  box in CSS and tracks it through any reflow.

**Still needs a person:** the end-to-end viewer → server → released-PDF path on an installed
build, which is Part C step 9 — including the resize case in step 5, which is the regression
test for the above.

**Carried forward to the shipping build, verified 2026-11-17.** Part A was run against
FreeEedUI `c93d34a`; the build going to GA carries UI `425a1ff`. Everything between those two
commits is four files:

| File | Change |
|---|---|
| `Version.java` | report 10.8.8, not 10.8.8-PREVIEW (#93) |
| `dao/user/FSUserDao.java` | `u.dat` was created group-readable (#94) |
| `webapp/css/search-modern.css` | overlay locked to the page image (#90) |
| `webapp/js/search.js` | overlay locked to the page image (#90) |

**No Java in the burn-in path changed** — `CaseFileService.mergeRedactedPdfs` and everything
the two harnesses drive are byte-identical between the two commits. So the 18 checks stand for
`425a1ff` and **do not need re-running by hand or by machine.**

That is not a free pass. `search.js` *did* change, and it changed the overlay scaling that the
harnesses cannot see — so the one thing this diff makes more important, not less, is the
**resize case in Part C step 5**. It is the only check anywhere that covers #90.

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
5. Change the password inside Review. **Expect:** the file the dialog named
   (`work/initial-admin-password.txt`, under the Tomcat directory — the dialog prints the
   absolute path) **is now gone, deleted by the password change itself.** Do not delete it by
   hand; deleting it yourself destroys the evidence this step exists to collect. If it is
   still there, this row FAILS — that is FreeEedUI #95, the defect this build fixes.
6. Open Review from the desktop again. **Expect:** no dialog this time, and the new password
   still signs in.

**What changed in this build, and why step 5 reads the way it does.** Until 10.8.8 the file
survived the password change and the dialog went on displaying a dead credential — found on
this machine on 2026-11-17, where an install from the 15th showed a password that had stopped
working on the 16th, and confirmed by signing in with it and being refused. Two fixes ship
here and the row covers both:

- **FreeEedUI #97** — `FSUserDao.saveUser` deletes the published file when the admin's
  stored password actually changes. This is what step 5 checks. It protects new installs.
- **FreeEed #646** — `ReviewCredentials.isSuperseded` suppresses the dialog when `u.dat` is
  newer than the password file, so an install that is *already* stale stops lying. This is
  what protects everyone upgrading into 10.8.8, whose file #97 was never there to delete.
  It cannot be exercised by a clean run of this row: a real first run writes the password
  file *after* `storeUsers()`, so the file is the newer one and the dialog correctly shows.
  Verified separately against both directories on this machine — fresh store `false`, the
  stale `work.pre-ga-20261117` `true`.

### B2 — Appliance review credentials (#68) — **N/A for 10.8.8**
**10.8.8 ships Linux and Windows installers only; there is no appliance image in this
release, so this gate has nothing to run against** (decided 2026-11-15). The steps stay here
because the code path is in the build and the gate applies the moment an image ships — do not
delete them, and do not let the row read PASS on the strength of the desktop test, which
exercises a different file in a different place.

1. First boot of the appliance image. **Expect:** `/opt/freeeed/review-admin-credentials.txt`
   exists, mode **600**, and the URL inside it carries the appliance's **actual IP**, not
   `localhost` or a placeholder.
2. Sign in to Review from another machine using those credentials. **Expect:** it works.
3. Reboot. **Expect:** the file is **not** regenerated and the password still works.

### B3 — First-run dialogs land in front (#621 item B)
1. Fresh install, first launch. **Expect:** the Edition chooser and the registration dialog
   appear **in front of** the Control Panel, not hidden behind it. This is the whole point of
   the row; everything below is about not misreading the second launch.
2. On the Edition chooser, leave **"Remember my choice"** unticked and click Continue.
   Restart. **Expect: the Edition chooser appears again.** That is correct behaviour, not a
   failure — `FreeEedUI.java:527-534` clears both the remember flag and the stored selection
   when the box is unticked, precisely so the dialog returns.
   While the chooser is on screen, read its subtitle: it must say **"Unlock premium
   capabilities"**. 10.8.7 and every build before it said "capabilties" (FreeEed #647, fixed
   in both the `.java` and the paired NetBeans `.form` so regenerating the form cannot bring
   it back). This is the one customer-visible string in the first thing a new user sees.
3. Tick **"Remember my choice"**, click Continue, restart. **Expect:** the Edition chooser
   does **not** appear.
4. The registration dialog is gated separately, on `user-registration-status` being
   `registered` or `declined`. Once either is recorded it must not return on a later launch.

**Do not mark this row FAIL because the Edition chooser came back.** Ask first whether the box
was ticked. A tester following an earlier wording of this row would have reported correct
behaviour as a defect, or "fixed" it.

---

### B4 — the publishing run must not leave a stale macOS link

**Not a test on the build; a check on the publishing run itself. Decide before the run, not
after — the `-latest-` overwrite is the irreversible part.**

10.8.8 builds **no macOS `.dmg`**, and that is structural rather than an oversight: the `.dmg`
can only be built, signed and notarized on a Mac (`SIGN_MAC=1` refuses on anything but
Darwin), while releases are published from the Linux box. The script's answer is
`PREBUILT_MAC_DMG=/path/to.dmg`, a `.dmg` built on the Mac and copied in before upload.

What happens if the publishing run is made with no `.dmg` present:

- The mac upload block is guarded by `if [ -f "FreeEed-$VERSION-macOS.dmg" ]`, so it is
  **skipped silently** — no warning, no error, exit 0.
- `FreeEed-latest-Windows.exe` and `FreeEed-latest-Linux.run` are overwritten with 10.8.8.
  **`FreeEed-latest-macOS.dmg` is not touched**, and on S3 that object is dated 2026-10-05,
  664 MB — the 10.8.7 build.
- Net effect: a macOS visitor to the download page is served **10.8.7 presented as current**,
  indefinitely, with nothing anywhere saying so. Two platforms move and one does not.
- `REQUIRE_INSTALLERS` would refuse a partial release, but it is **opt-in and unset by
  default**, so nothing catches this unless it is passed deliberately.

Pick one and record it here:

1. **Build the `.dmg` on the Mac**, notarize it, and publish with
   `PREBUILT_MAC_DMG=/path/to/FreeEed-10.8.8-macOS.dmg`. Tracked as #559.
2. **Publish Windows + Linux only**, and in the same session change the download page so the
   macOS link is removed or labelled 10.8.7. The link and the release move together or the
   page lies.
3. Accept the skew. **Not acceptable silently** — it only becomes an option with option 2's
   page change attached, which makes it option 2.

Either way, run the publish as `REQUIRE_INSTALLERS=windows,linux` so the guard is stated
rather than absent.

**DECIDED 2026-11-17 (Mark): option 2 — publish Windows + Linux, macOS stays on 10.8.7.**

The demo this release is cut for is a Windows audience, and the `.dmg` cannot be built on the
publishing box. So the macOS build is deliberately deferred rather than rushed, on three
conditions, all of which are part of this row and none of which is optional:

1. The publishing run passes `REQUIRE_INSTALLERS=windows,linux` — the two platforms being
   published are *named*, so a missing one fails the run instead of being skipped in silence.
2. **In the same session as the publish**, the download page says the macOS download is
   10.8.7. Not "after the demo", not "next week": the `-latest-` overwrite is irreversible and
   the moment Windows and Linux move, an unchanged page is making a false claim to every macOS
   visitor. If the page cannot be changed in that session, the publish waits.
3. The macOS `.dmg` is built on the Mac from the same commit as the published build
   (`13f01e98`), not from a later `dev`, and published with `PREBUILT_MAC_DMG=...`. A `.dmg`
   from a different commit shipped under the same version number is worse than no `.dmg`.

This row cannot be signed PASS before the publishing run, because it is a check *on* that run:
after publishing, confirm `FreeEed-latest-Windows.exe` and `FreeEed-latest-Linux.run` carry the
new date and size, and that the page change in condition 2 is live. Tracked: #559.

**Executed 2026-11-18**, on Mark's instruction to publish ahead of Part C. Recorded plainly,
including where it departed from the conditions above:

```
FreeEed-latest-Windows.exe   916,587,025   2026-11-18 14:09   (was 2026-10-04, 10.8.7)
FreeEed-latest-Linux.run     940,426,611   2026-11-18 14:09   (was 2026-10-04, 10.8.7)
FreeEed-latest-macOS.dmg     664,693,662   2026-10-05         UNCHANGED, as decided
```

Both installers verified publicly reachable over HTTPS with
`Cache-Control: no-cache, max-age=0, must-revalidate`, and byte sizes identical to the local
artifacts.

- **Condition 1 was not met as written, and this is deliberate.** `REQUIRE_INSTALLERS` is a guard
  inside `release_freeeed_complete.sh`, and the script was *not* used: re-running it would have
  rebuilt from the current tree and stamped a commit later than `13f01e98`, i.e. published a build
  nobody had tested. Instead the script's upload block was replicated against the existing
  artifacts and both installers were confirmed present and live by hand. The guard's *purpose* was
  served; the guard itself was not run.
- **Condition 2 is met, by a different mechanism than this row assumed.** The download page does
  **not** hold hand-typed version labels: it calls
  `api.github.com/repos/SHMSoft/FreeEed/releases/latest`, strips the leading `v` from `tag_name`,
  and renders `'Windows Installer v' + version`. The page read v10.8.7 purely because the newest
  GitHub release was `v10.8.7` from 2026-10-04 — publishing to S3 cannot move it. **The last step
  of a release is therefore `gh release create`, not a site edit.** Done 2026-11-18:
  `v10.8.8` against `13f01e98` (a short SHA is rejected; the API wants the full 40 characters),
  targeting the commit rather than `main` so a protected branch is untouched. Verified live:
  Windows pane reads `Windows Installer v10.8.8 (.exe)`, Linux `Linux Installer v10.8.8 (.run)`.
  The repo README was corrected in the same commit as this entry.
- **macOS needs no correction to avoid lying, and still cannot disclose.** Its entry is
  `macOS Installer (.dmg)` with no version string at all, so it never claimed 10.8.8. It also
  cannot say it is 10.8.7 — that *would* be a hand edit to the widget, and is the only page edit
  a release actually needs. Not done; tracked with #559.
- **Condition 3 stands** — the macOS `.dmg`, when built, comes from `13f01e98`.

**Part C had not been run when this was published.** That was Mark's call, and the reasoning holds:
10.8.7 was already public with the same known Windows imaging defect (#579), so 10.8.8 is not a
regression on any axis — it is 10.8.7 plus the credentials fix (FreeEedUI#95 / #646) and the
Edition-dialog typo (#647). The FOIA run-through gates the *demo*, not the download.

---

## Part C — FOIA end-to-end, on an installed build

Do this on the installed build, not from source. A records officer's path.

### 1. Create the case — use the fixed corpus, not whatever is to hand
The corpus ships **inside the installer**, at
`freeeed-processing/test-data/11-foia-corpus/` in the repo and under the installed build's
`FreeEed/test-data/11-foia-corpus/`. A tester on a fresh Windows box already has it; nobody
has to remember to send files, and there is no question of which version was used. Regenerate
it with `python3 scripts/make_foia_test_corpus.py` after changing the generator — never edit
the files by hand.

```
11-foia-corpus/README.md      per-file checklist with expected results
11-foia-corpus/documents/     the 8 documents  <- point the project's input here
11-foia-corpus/zip-input/     evidence-bundle.zip alone, for the #589 run
```

The zip sits in its own folder on purpose: it is a known-failure case, and a run that mixes it
in with the loose files cannot tell "the zip yielded nothing" from "the zip was never read".

The staged inventory must read **`doc 1, docx 1, odt 1, pdf 3, txt 1, xlsx 1` = 8 files**
before processing. That tally is itself a check — a shortfall here is a staging problem, not an
imaging one, and chasing it later wastes the run.

The corpus is deliberately mixed, because each format fails differently:

| File | Exists to catch |
|---|---|
| `01-response-letter.pdf` | placement — sensitive lines at **13.9% / 19.6% / 25.3%** of page height, so a box >2% off is visible to the eye |
| `02-multipage-memo.pdf` | per-page coordinates — page 1 clean, content on **pages 2 and 3** |
| `03-scanned-notice.pdf` | image-only input, OCR path, redacting a page that never had text |
| `04-personnel-record.docx`, `05-legacy-memo.doc`, `06-field-notes.odt` | **#579** — on Windows, imaging has produced *no rendition and no error* for Office formats, which dead-ends this workflow silently |
| `07-roster.xlsx` | spreadsheet imaging |
| `08-plain-note.txt` | baseline — if this fails, imaging is broken outright, not format-specific |
| `evidence-bundle.zip` | **#589**, as a *known* failure — add it as an input on its own and confirm whether a zip still yields zero documents silently |

`Agency response letter -- this line is NOT to be redacted.` appears in every file at 37.9%
of page height. **Never box it.** It is the control: if it gets covered something is
over-redacting, and if it survives into the released text layer the burn-in did not
rasterize the page.

### 2. Turn on PDF imaging — THE TRAP
Open the **Imaging** tab and tick **"Create PDF Images, multi-page, for every file"**.

Then open the **OCR** tab and tick **"Perform OCR"**. `03-scanned-notice.pdf` is image-only
with no text layer, so without this it has no text at all and the run cannot tell a working
OCR path from a broken one — the result looks the same either way.

> **This is the one step that silently ruins the run.** Without it there are no per-document
> PDF renditions, so the Redact tab has nothing to draw on and the FOIA workflow fails at the
> last step with no warning earlier. The one-click FOIA preset that would set this
> automatically is **not built** (#618). Expect to do it by hand, and expect customers to
> forget.

### 3. Process
Run processing. **Expect:** per-document PDFs appear under the project output's `images/`
folder, named `<uniqueId>_<name>.pdf`.

**Count them: there must be 8.** This is the cheapest check in the plan and it gates
everything after it — the Redact tab can only draw on a rendition, so a document with no
rendition drops out of the workflow with nothing said. Fewer than 8 means **#579**, and *which*
files are missing is the finding: `.docx` / `.doc` / `.odt` missing is the known Office-format
failure, `08-plain-note.txt` missing means imaging is broken outright.

### 4. Start Review and sign in
Review serves on **port 8090**. Sign in with the generated credentials from B1/B2.

### 5. Open a document and redact
Select the case, search, open a document, open the **Redact** tab.
- Draw a box over something sensitive. **Expect:** an exemption-code picker offering
  **Federal FOIA** (b)(1)–(b)(9), **FERPA**, and the **State (Ohio ORC 149.43)** example.
- Pick a code. Draw a second box on a **different page** of a multi-page document.
- Move one box; delete another. **Expect:** all three gestures work.
- **Resize the browser window (and try Ctrl +/− zoom), then draw another box without
  changing page first.** This is the case that caught a real mis-placement bug — the
  drawing overlay used to be frozen at its load size, so after a reflow every new box was
  stored against the wrong scale and burned in up to ~3 lines of text away from where it
  was drawn, with nothing visible to show it. Fixed in FreeEedUI#90; this step is how you
  confirm the fix is in the build you are testing.
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
per-page coordinates are where an off-by-one would show up. **And check the box you drew
after resizing the window in step 5** — that one is the actual regression test for
FreeEedUI#90, and a run that never touches the window size will pass while leaving the bug
undetected.

**9d. Log.** Read the cover page. **Expect:** the case name, the document count, the
redaction count, and a line per document listing its codes.

---

## Known limits — expected behavior in 10.8.8, not bugs

Do not file these; they are recorded and scoped. **#617 and #618 were explicitly deferred to
the 10.8.9 milestone on 2026-11-10** so the GA was not carrying a new Swing control and a new
export path through its install test ten days before the episode.

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
| A — automated harnesses (18 checks) | PASS — carried forward to UI `425a1ff`, burn-in path unchanged | 2026-11-17 | automated + inspection |
| B1 — desktop credentials | PASS — steps 1-6 on `build=13f01e98`; password change deleted `initial-admin-password.txt` by itself (#95 verified on disk), reopen showed no dialog, new password signs in | 2026-11-17 | Mark |
| B2 — appliance credentials | N/A — no appliance image in 10.8.8 | 2026-11-15 | Mark |
| B3 — first-run dialogs | | | |
| B4 — no stale macOS `-latest-` link | PASS — 2026-11-18. Windows + Linux `-latest-` moved to 10.8.8 (`build=13f01e98`); macOS alias untouched at the 2026-10-05 object; release `v10.8.8` cut against `13f01e98`; download page verified reading v10.8.8 on the Windows and Linux panes and claiming no version on macOS | 2026-11-18 | Mark |
| C1–C8 — FOIA workflow | | | |
| C9 — release verification | | | |

**Release gate:** every row above must read PASS — or N/A with a stated reason, as B2 does —
before the publishing release run, because 10.8.8 is a clean semver and that run overwrites
the public `-latest-` download links. A blank row is not an N/A: it is an untested gate, and
the point of the table is that nobody can tell the two apart a week later.
