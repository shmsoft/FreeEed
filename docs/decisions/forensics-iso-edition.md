# FreeEed Forensics Edition — all-in-one bootable/installable ISO

**Status:** Proposal / working record. (2026-07) Championed and built by **Remco** (external
contributor). A **distribution channel**, not a code change to FreeEed.

## What it is
A turnkey **"all-in-one forensics"** Linux ISO (bootable + installable) with **FreeEed** plus a
curated set of open-source DFIR / eDiscovery tools preinstalled — so an investigator boots a
ready-to-work environment instead of assembling a toolchain. Base: **MX Linux** (Debian stable),
using Remco's live-system → installable-ISO workflow; ships with **NVIDIA + CUDA** drivers.

## Where it fits
- An **additional distribution option**, alongside the per-OS installers (Phase 1) — **not** a
  replacement. Different audience: the dedicated **forensic workstation / lab**, vs. the lawyer
  who double-clicks an installer on their existing Mac/Windows.
- Aligns with the "**containerize the Linux engine**" direction ([[pst-processing]]) — a Debian
  base is the same Linux environment FreeEed's engine wants; ISO and container are cousins.
- Solves the **dependency-hell** problem of shipping the Python/native tool stack (iLEAPP, libpff,
  Tesseract, etc.) — everything is baked in.

## Appliance — distributing computers ("eDiscovery-in-a-box")
David (Berenthal Law) suggested **distributing pre-built computers** for eDiscovery: ship a
turnkey machine imaged with the Forensics Edition + local AI that a firm plugs in and uses. This
is the **physical embodiment of the local-first / no-outbound** positioning, and it fixes the
**local-AI hardware gap** (local LLMs need a GPU the lawyer's laptop lacks — the appliance ships
with one).

- **Why it fits:** ultimate court-defensibility (a dedicated box the firm owns; data never
  leaves; physical isolation → trivial CJIS/privilege certification); fully turnkey (no IT);
  higher-value + stickier than software-only; and the **ISO is literally the appliance image**.
- **Model — capture the value without becoming a hardware vendor:** start with a **reference
  spec + "we image it"**, partnering with a system integrator / VAR to supply and ship the
  hardware while Scaia provides the image + support (not inventory). Optionally a **managed /
  rental** appliance — recurring revenue, the on-prem version of "we operate it." Avoid tying up
  capital / inventory / RMA up front.
- **Tier by scale:** single-workstation appliance for a small firm; multi-node (Piranha) for
  70 TB-class shops (K3 / Panther).
- **David = design partner / likely first customer.** Pricing & go-to-market = CRM territory;
  technical enablement (this ISO + local-AI-on-GPU + Piranha) is here. See `local-ai-architecture.md`.

## Tool bundle
**Remco's current list:** CyberChef, OpenRefine · iLEAPP/aLEAPP (GUI+CLI) · Apache Tika · Angry IP
Scanner, Sniffnet, EtherApe, Wireshark, Zenmap · ffmpeg, WinFF, HandBrake, Audacious, Audacity,
Praat · Autopsy/Sleuthkit · ClamTK · DB Browser for SQLite, DBeaver, fqlite, BaseX · Evolution
(PST) · Guymager, ddrescue (imaging) · FTK Imager (via Wine) · Maltego · fcrackzip, PDFcrack, John
the Ripper · Quickhash, Grsync · **Ollama + models** · SpeechNote, whisper-ctranslate2 · `ent` and
many CLI tools.

**FreeEed additions — the eDiscovery / legal-review angle (the gap in a DFIR-first list):**
- **ExifTool** — metadata extraction (central to eDiscovery).
- **Tesseract OCR** — searchable text from scans/images.
- **LibreOffice (`soffice`)** — document → PDF imaging / production rendering.
- **poppler-utils / qpdf / pdftk** — PDF text, manipulation, redaction prep.
- **libpff / `pff-tools`** (+ **libpst / `readpst`**) — bulk PST/OST export (FreeEed's PST path;
  scales past Evolution for large mailstores).
- **bulk_extractor** — PII/feature extraction (pairs with legal PII review).
- **hashdeep / md5deep** — recursive/bulk hashing for dedup + verification.

**DFIR staples worth adding:** plaso/log2timeline (+ Timesketch), Volatility 3, RegRipper, YARA,
PhotoRec/foremost (carving).

**Centerpiece:** **FreeEed** + its runtime (Solr; the bundled JRE once Phase 1 lands).

**Synergy to note:** Remco already ships **Ollama + models + CUDA** — that *is* FreeEed's
local-first AI direction (on-box, court-defensible AI; see `local-ai-architecture.md`). The ISO
already carries local AI.

## Build & update strategy
- **Today (Remco):** set up a system, snapshot it to an installable ISO; ship new ISOs regularly;
  installed systems can also update in place (accept some divergence; keep the canonical build).
  Rebuild-from-scratch is preferred over in-place at major Debian jumps (**trixie → forky**, ~mid
  2027 per Remco).
- **Add — version manifest per ISO:** ship a package/version list (or lockfile) with each release.
  For a *forensics* product, "which build/version produced this?" must be answerable
  (defensibility), and it tames in-place divergence.
- **Later — scripted/reproducible build** (live-build / config-as-code) so the ISO is *rebuildable
  from source*, not only clonable from one machine → provenance + kills bus-factor (Remco offers
  to hand it off; a script makes that real). Frame as future, not a correction — his method works.
- **Hosting:** a multi-GB ISO needs a home — `shmsoft.s3` (already used for releases) or a torrent
  for bandwidth.

## Licensing
The ISO is **aggregation/distribution** of independently-licensed tools (many GPL). Aggregation
does **not** infect FreeEed's Apache-2.0 — but the ISO **as a whole** must honor each tool's
license (e.g., GPL source-availability offers). Track per-tool licenses in the manifest.

## Ownership / knowledge transfer
Remco builds & maintains it now and explicitly offers to hand it off. To make that real: capture
his **build steps + "tips & tricks"** as docs, and move toward the scripted build above. Any build
scripts/configs he contributes to our repos fall under the **CLA** (see `CLA.md`).

## Open items
- Confirm final tool list with Remco; add the eDiscovery/DFIR items above.
- Manifest format (package + version + license).
- Reproducible build script (live-build) — later.
- ISO hosting (S3 / torrent).
- Screen share: get FreeEed running on Remco's side **and** walk the ISO together (he needs ~1 day
  for a fresh English install; he's on holiday — schedule loosely, not this week).

Related: [[pst-processing]], [[local-ai-architecture]], refactoring-plan (installers).
