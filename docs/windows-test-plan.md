# Windows Test Plan — every `dev` build

A repeatable smoke/acceptance test for FreeEed on Windows. Run it against each new build from
`dev` (the daily/unstable build tracks `dev`). File anything that fails as a GitHub issue
using the template at the bottom. Goal: catch Windows regressions early so they can be fixed
before a release.

## What to test against
- **Daily Windows installer (tracks `dev`):**
  https://shmsoft.s3.us-east-1.amazonaws.com/releases/FreeEed-latest-Windows.exe
- Always **record the exact build** you tested: open **Help → About** (or run FreeEed with
  `-version`) and copy the **version + git commit SHA + build time**. Every bug report must
  include this — builds are identified by SHA, not just the version number.

## Before you start (record your environment)
- Windows version/edition (e.g., Windows 11 23H2).
- Whether it's a clean machine/VM or an upgrade over a previous FreeEed install.
- RAM, and whether you're testing large files.

## Test cases
For each: note **Pass / Fail**, and on fail capture steps, screenshot, and the log location
(see bottom).

1. **Install** — run the installer; it completes without errors; FreeEed launches.
2. **First-launch registration** — register on first launch; the free activation key arrives
   by email and activates. (Note if anything about licensing/activation misbehaves.)
3. **Create a project & load data** — create a new project; add a small mixed sample
   (a few PDFs, a Word/Excel doc, an email or two).
4. **Process / ingest** — run processing to completion; no crash; progress/among counts look
   right; output lands in the **writable output folder** (should be under your user profile,
   e.g. `FreeEed-output`), not a locked/system path.
5. **Verify extraction** — extracted text and metadata are present; native + text views open.
6. **Imaging (doc → PDF)** — run imaging on a document; a PDF is produced and opens.
7. **OCR** — process a scanned/image-only PDF; text is extracted via OCR.
8. **PST/email** — process a small `.pst` (or `.eml`/`.msg`); messages and attachments expand;
   family relationships preserved.
9. **Review web app** — open review; it loads on **port 8090** (not 8080); documents list,
   search returns hits, tagging/coding works.
10. **AI (only if the build/license enables it)** — the AI Advisor starts; you can ask a
    question about a processed document and get an answer. Note any licensing/`402` errors.
11. **Transcription (optional)** — process a short audio/video file; a transcript is produced.
12. **Restart & reopen** — close and reopen FreeEed and the existing project; state is intact.
13. **Uninstall** — uninstall cleanly (note anything left behind).

## What makes a bug report useful
- The **build version + SHA + build time** (from About).
- **Exact steps** to reproduce, in order.
- **Expected** vs **actual** result.
- A **screenshot** of the error/state.
- **Logs** — attach or paste the relevant tail. On Windows the logs are typically under the
  FreeEed install/working directory (`logs/`); include the console window text if FreeEed was
  started from a terminal.
- **Severity** — blocker (can't proceed) / major (feature broken) / minor (cosmetic).

## Bug report template (copy into a GitHub issue)
```
Title: [Windows] <short summary>

Build:        <version> / <git SHA> / <build time>   (from Help → About)
Windows:      <version/edition>  |  Clean install? <yes/no>
Severity:     <blocker | major | minor>

Steps to reproduce:
1.
2.
3.

Expected:
Actual:

Screenshots: <attach>
Logs:        <attach/paste relevant lines; note log path>
Notes:
```

## Workflow
- File each issue on GitHub; label it `windows` and `qa` if labels are available.
- One issue per distinct problem (don't batch unrelated bugs).
- Note which build first showed the bug; if a later `dev` build fixes it, comment and close.
