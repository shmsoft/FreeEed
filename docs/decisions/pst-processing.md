# PST/OST Processing — Options and Direction

**Status:** Exploratory / working decision record — to revisit. (2026-07)

## Context
FreeEed **reads/extracts** PST and OST for eDiscovery (never edits/creates them).
Today the handling is split and both paths fork/exec a subprocess:

- **Windows:** `java -jar proprietary_drivers/jreadpst.jar` — **Independentsoft JPST** (commercial).
- **\*nix:** `readpst` — **libpst** (open source).

Trigger for this discussion: Independentsoft shipped **JPST 2.0** (edit-in-place,
create-from-scratch, `.msg`→PST, Outlook 2013+ OST, >2 GB streaming). Buy the
upgrade, or move to open source?

## Key finding: we only READ PST
JPST 2.0's headline features (edit / create / convert) are **irrelevant** to
FreeEed. Only its *read-side* gains matter: modern **OST (2013+)**, **>2 GB**
streaming, robustness. So the upgrade is worth far less to us than the release
notes suggest.

## Options
1. **java-libpst** (pure Java, in-process) — free, portable, no subprocess.
   *But:* **no OST 2013+ support** (Apache Tika's `OutlookPSTParser` uses it and
   has this known gap), lightly maintained, weaker on large/corrupt files.
2. **libpff / `pffexport`** (native C, fork/exec) — free, open, **forensic-grade
   (the lib IPED uses)**, handles OST incl. 2013+, **cross-platform including
   Windows** (compile `pffexport.exe`). A GPL CLI invoked as a subprocess is
   arm's-length → does **not** infect Apache-2.0 FreeEed (same as `readpst`
   today). Cost: build + validate per-OS binaries.
3. **Independentsoft JPST** (commercial Java, in-process) — robust, OST, a
   **drop-in `.jar` (zero build)**, no GPL. Cost: license + a proprietary dep we
   are otherwise trying to delete.

## Reference point: IPED (github.com/sepinf-inc/IPED)
- Processes via Apache Tika (→ java-libpst by default), **but wrote its own
  `libpff` fork/exec parser because java-libpst wasn't good enough**, and builds
  custom HTML mail views. Confirms: pure-Java java-libpst is insufficient for
  forensic PST; **native `libpff` is the proven open path**.
- Hits ~**400 GB/hr** via Sleuthkit + heavy multithreading + **out-of-process
  parsing** (parallel *and* crash-isolated) + Lucene. (FreeEed is ~10 GB/hr.)

## Architecture context that drives the PST choice
- Two scaling axes:
  - **Scale-up (IPED-style):** local worker pool + **pooled, warm** out-of-process
    tools (soffice / tesseract / libpff) — parallelism + crash isolation.
  - **Scale-out ("Piranha"):** distribute item processing horizontally with
    **Spark** (or a queue+workers / Ray) on cloud/Ubuntu nodes. This is FreeEed's
    original Hadoop DNA, modernized.
- **Unifying insight:** write a clean, near-stateless `process(item)` core; a
  local pool *or* a Spark map drives it — **same core, two schedulers.**
- **Containerize the Linux engine:** local (Win/Mac via Docker/WSL2) *and* Piranha
  (cloud) run the **same Linux image** → PST is always `libpff` → **JPST dies
  everywhere**; Windows/Mac become the **review/UI client, not the parser**.
  (One decision removes the per-OS tool matrix *and* seeds the distributed engine.)
- **`libpff` runs natively on Windows too** (libyal ships VS build files), so
  "libpff everywhere" can be native on all three OSes — a container is *not*
  required to drop JPST.

## Forensic hard part (applies to any parallel/distributed path)
Bates numbering, **family grouping**, and **MD5 dedup** must be global,
sequential, and **reproducible**. Parallel/distributed workers finish out of
order. Pattern: keep **all numbering out of the parallel map**; process in
parallel emitting results with stable sort keys, then a **single deterministic
finalization pass** assigns Bates / groups families / resolves dedup. Getting
this wrong = wrong productions.

## Working choices / leanings (revisit before acting)
- **Do NOT buy JPST 2.0.** We'd pay for unused edit/create features and prolong a
  proprietary dep we're on a path to delete.
- **Standardize PST on `libpff` (`pffexport`) via fork/exec**, native per-OS
  (Linux/Mac/Windows) — one code path replacing *both* `readpst` and
  `jreadpst.jar`. Beats java-libpst on OST + robustness; matches IPED.
- **Direction:** containerized Linux processing engine + optional **Piranha**
  (Spark) scale-out. Keep JPST *only if* we consciously choose to support
  native-Windows-**without-a-container** processing as a segment.
- Windows/Mac still matter — as the **review/UI client**, not the parser.

## Open items when resuming
- Build + forensic-validate `pffexport` for **Windows** and **macOS** (Linux is
  apt `readpst`/`pff-tools` today).
- Refactor `PstProcessor` to a single `libpff` fork/exec path (per-OS binary);
  drop the `jreadpst.jar` branch.
- Design the `process(item)` core + the **deterministic Bates/family/dedup
  finalization** — the shared prerequisite for both scale-up and Piranha.
- Pick the Piranha scheduler: **Spark** vs queue+workers (Kafka/K8s) vs Ray.
- Decide whether local processing **requires a container** (kills native-Windows
  tooling entirely) or must also run natively on the desktop.

## Related
Issue #557 (Windows PST handling). See also the processing-performance and
"no outbound calls during imaging" constraints.
