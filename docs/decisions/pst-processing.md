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
  - **Scale-out ("Piranha"):** distribute item processing horizontally on
    cloud/Ubuntu nodes — FreeEed's original Hadoop DNA, modernized. **Preferred
    scheduler: Kafka** (see below), not Spark.
- **Unifying insight:** write a clean, near-stateless `process(item)` core; a
  local pool *or* a Spark map drives it — **same core, two schedulers.**
- **Containerize the Linux engine:** local (Win/Mac via Docker/WSL2) *and* Piranha
  (cloud) run the **same Linux image** → PST is always `libpff` → **JPST dies
  everywhere**; Windows/Mac become the **review/UI client, not the parser**.
  (One decision removes the per-OS tool matrix *and* seeds the distributed engine.)
- **`libpff` runs natively on Windows too** (libyal ships VS build files), so
  "libpff everywhere" can be native on all three OSes — a container is *not*
  required to drop JPST.

## Scaling engine: Kafka (preferred over Spark)
For FreeEed's *embarrassingly-parallel, item-level* work (little cross-item
shuffle), **Kafka fits better than Spark** and is fully **cloud-independent**
(same stack on a laptop, on-prem, or any cloud → matches the private/self-hosted
positioning; K3 can run it in their own VPC).
- **Fan-out:** **Share Groups / Queues (KIP-932)** — per-message ack, many
  consumers per partition. Beats classic consumer groups for eDiscovery's
  *extreme* per-item size variance (a 2 GB PST won't head-of-line-block small
  emails; parallelism decoupled from partition count).
- **Data plane = claim-check:** never put GB payloads in Kafka; messages carry
  *references*; workers fetch bytes from object storage. Use **MinIO**
  (self-hosted, S3-compatible) to keep the whole stack cloud-independent. Kafka =
  control plane, MinIO / shared-FS = data plane.
- **Light stateful work (dedup, families):** **Kafka Streams** — embedded, no
  extra cluster (keeps it lean/portable). Compacted `seen-md5` for dedup;
  `groupBy(family-id)` + family-keyed partitions for family locality. (**Flink**
  is heavier and adds a cluster — only if analytics outgrow this.)
- **Same engine local ↔ cluster:** single-broker KRaft + a few local worker
  containers locally; a cluster + many workers for K3. One code path, config-scaled.

## Three-phase pipeline — and where Bates actually lives
Bates numbering is **production, not ingestion** — so the "global sequential
ordering" problem is *not* a pipeline concern. Split:
1. **Ingest / process** (parallel, high-throughput; Kafka Share-Groups + workers):
   extract text/metadata, **dedup (MD5)**, **preserve family integrity**
   (email↔attachment links from container expansion), index → FreeEedUI.
   **No Bates here.**
2. **Review** (FreeEedUI): tag / label / code (responsive, privilege,
   confidentiality) — the IPRO-Eclipse-style coding panels.
3. **Produce** (FreeEed, triggered by FreeEedUI): over the *reviewed, selected
   subset* apply **Bates** (options: prefix, start, zero-pad width, page- vs
   doc-level, family ranges BegDoc/EndDoc/BegAtt/EndAtt), redactions, TIFF/PDF
   imaging, and `.DAT`/`.OPT` load files. **Low-volume, deterministic, single
   ordered pass — no parallelism/Kafka needed.**

So the parallel pipeline only has to get **dedup + families** right; Bates and
imaging are a separate, ordered production step. Aligns with the
**production-engine-split** (FreeEedUI orchestrates FreeEed production, FreeEedUI#61)
and the **ESI production format spec** (#551).

## Working choices / leanings (revisit before acting)
- **Do NOT buy JPST 2.0.** We'd pay for unused edit/create features and prolong a
  proprietary dep we're on a path to delete.
- **Standardize PST on `libpff` (`pffexport`) via fork/exec**, native per-OS
  (Linux/Mac/Windows) — one code path replacing *both* `readpst` and
  `jreadpst.jar`. Beats java-libpst on OST + robustness; matches IPED.
- **Direction:** containerized Linux processing engine + **Piranha** scale-out on
  **Kafka (Share Groups) + MinIO**. Keep JPST *only if* we consciously choose to
  support native-Windows-**without-a-container** processing as a segment.
- Windows/Mac still matter — as the **review/UI client**, not the parser.

## Open items when resuming
- Build + forensic-validate `pffexport` for **Windows** and **macOS** (Linux is
  apt `readpst`/`pff-tools` today).
- Refactor `PstProcessor` to a single `libpff` fork/exec path (per-OS binary);
  drop the `jreadpst.jar` branch.
- Design the `process(item)` core; get **dedup + family integrity** right in the
  parallel pipeline (Bates is deferred to the production phase, not the pipeline).
- Kafka path: **Share Groups (KIP-932)** for fan-out, **Kafka Streams** for
  dedup/families, **MinIO** claim-check; confirm Share Groups GA status.
- Decide whether local processing **requires a container** (kills native-Windows
  tooling entirely) or must also run natively on the desktop.

## Related
Issue #557 (Windows PST handling). See also the processing-performance and
"no outbound calls during imaging" constraints.
