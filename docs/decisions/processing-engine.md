# Processing Engine — Architecture & Direction

**Status:** Exploratory / working decision record — to revisit. (2026-07)

**Scope:** this started as a PST/OST library decision (below) but has grown into the record for
the **processing engine as a whole** — PST/OST libraries, scale-up vs scale-out (Piranha), the
Kafka/MinIO engine, the three-phase pipeline, incremental/rolling collections, and
**containerizing the Linux engine** to eliminate Windows-native code. *PST was the trigger, not
the boundary.* (Renamed from `pst-processing.md`.)

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

## Incremental / rolling collections + cross-batch dedup
eDiscovery data arrives **piecemeal** — a batch today, another in three days, and so on.
So **"add data to the case"** must be a **first-class** operation, not reprocess-from-scratch.
(Discussed 2026-07; decisions below. Current-code behavior TBD — see Open items.)

- **A case is a long-lived entity**, not a one-shot project. It accumulates three things:
  the **index**, a **dedup store** (every content hash seen so far), and **provenance**
  (batch/collection/custodian/load-date per item). "Add data" = process the new batch and
  **merge into the existing case.**
- **Cross-batch dedup falls out of the persistent, case-scoped hash store** — the *same*
  dedup the parallel pipeline already needs, just with a store that **survives across runs**.
  Batch N is deduped against everything in batches 1..N-1, not only within itself. No separate
  "incremental" code path required.
- **Don't drop dupes — record occurrences.** Keep one master doc; append each duplicate's
  **custodian / path / batch** (defensibility; drives the custodian/dedup fields at production).
- **Global vs custodian dedup** is a **policy/setting**, not a hardcode.
- **Indexing is naturally incremental** — Solr/Lucene *appends*; adding a batch never means a
  full reindex.
- **Provenance stamping** (collection/batch id + load date + custodian) enables filtering
  "what came in batch 3" and cutting **rolling productions** (produce only the new responsives).
- **Dedup correctness under parallelism:** partition the dedup stage **by hash** (same hash →
  one decider), or enforce a DB unique constraint on `(case, hash)`. Combined with stable item
  ids, this makes **re-running a batch idempotent** (re-collection after a partial failure
  doesn't double-count).

## Container-first — eliminate the Windows-native engine (direction)
The Windows-native code paths are the main source of bugs and test/support cost — JPST's
extensionless-file handling (#580), soffice detection (#579), console-window sprawl (#583),
path/filesystem differences (native-preview / `work/c.dat` lookups). **Direction: stop writing
Windows-native *engine* code; run the Linux engine everywhere.**

- **Container for those who can:** on Windows/Mac, ship the Linux engine in a container
  (WSL2 / Docker) → PST is always libpff, paths are Linux, no batch launchers. The whole class
  of Windows-native bugs disappears and the test matrix collapses to one platform.
- **Appliance for those who can't:** locked-down / CISO shops (e.g. Panther) often **forbid**
  WSL2 / Hyper-V / Docker on endpoints — so a hard virtualization prerequisite would block the
  privacy-first customers we most want. For them the **"FreeEed Certified Hardware" appliance**
  (a pure Linux box — see `forensics-iso-edition.md`) *is* "no Windows-native, ever." Cleanest
  expression of this direction.
- **Review UI is already platform-free** — a web app (browser → localhost Tomcat), no native
  port needed regardless.
- **Drop** the fragile Windows-native engine (JPST, batch scripts, path handling); **keep** a
  thin Windows *host* + the browser review. Same Linux image runs on the laptop (container),
  the appliance, and Piranha cloud nodes — **one core, one image, everywhere.**
- **Container-*first*, not container-*only*:** do NOT make Docker/WSL a hard requirement for
  *every* Windows user — the appliance covers the shops that can't virtualize.
- **Timing:** this is the architectural "real fix" that makes the current Windows-native bugs
  moot, but it's a **Phase-1++** move — today's individual bugs still get patched in place.

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
- **(Deferred code check — when we start)** How does current FreeEed handle it *today*: does
  dedup persist **across** processing runs or only within one run? Is there a real
  **add-to-existing-case/index** path, or does it reprocess a project? Answers scope how much
  the incremental/rolling-collection model needs building vs. already exists.

## Related
Issue #557 (Windows PST handling). See also the processing-performance and
"no outbound calls during imaging" constraints.
