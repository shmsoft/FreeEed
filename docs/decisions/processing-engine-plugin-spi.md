# Processing engine — executor SPI & pluggable schedulers

**Status:** proposed (2026-09-15). Companion to `processing-engine.md` (the engine direction);
this records the **code-boundary** decision that follows from its "one core, two schedulers" insight.

## Decision
Keep the open core **scheduler-agnostic** by defining a small **executor SPI**, and provide the
scale-out scheduler as a **separate pluggable implementation** rather than baking it into this
repository.

- **Open core (this repo):** a near-stateless **`process(item)`** and an **executor interface**
  (submit work, await results, apply backpressure, report status). The core never imports Kafka,
  MinIO, or any cluster dependency.
- **Local executor (this repo):** the default single-node implementation — a warm, pooled,
  out-of-process worker set (soffice / tesseract / libpff), parallel + crash-isolated. Ships and
  runs with open-source FreeEed, no extra infrastructure.
- **Distributed executor ("Piranha") — separate component, not in this repo:** implements the same
  SPI over the scale-out stack (Kafka Share-Groups control plane + MinIO/object-store claim-check
  data plane; Kafka Streams for dedup/families). Loaded as a plugin at runtime when present;
  absent, the engine uses the local executor.

## Why a clean SPI (not #ifdefs or a fork)
- **Dependency hygiene:** the base build stays light — no cluster libraries pulled into a laptop
  install. Kafka/MinIO live only with the distributed plugin.
- **One core, two schedulers:** the exact same `process(item)` runs under the local pool or the
  distributed executor — write/validate the core once (dedup + family integrity are the only
  parts that must be correct under parallelism; Bates/imaging are the separate production step).
- **Deployment picks the executor:** laptop/appliance → local; cloud/cluster → distributed. No
  code change in the core, just which executor is loaded.
- **Testability:** the local executor is the reference implementation the SPI is tested against.

## SPI shape (to firm up when we build it)
- `process(item) -> result` — pure-ish, no scheduler assumptions.
- `Executor`: `submit(items)`, capacity/backpressure hints (bound in-flight per worker so a 2 GB
  PST can't swamp a node), `status()` (submit + async job-status), a DLQ/max-attempts contract so
  a poison item can't stall a case.
- Dedup + family-integrity hooks that a distributed executor can partition by hash / family-id.

## Boundary
- The **distributed executor is maintained in a separate repository** and integrated as a plugin;
  it is **out of scope for this open-source repo**. This record fixes only the *seam* (the SPI and
  the local implementation), so the core stays open and dependency-light regardless of how the
  scale-out engine is distributed.

## Relates to
`processing-engine.md` (Piranha / Kafka / MinIO direction, three-phase pipeline, Nuix scaling
learnings), and the production-engine split (Bates/imaging are a separate ordered step, not part
of the parallel executor).
