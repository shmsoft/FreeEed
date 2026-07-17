# Local AI — Architecture & Design (Phase 3)

**Status:** Design / working record. (2026-07)

Companion to `docs/local-ai-cjis-briefing.md` (the *why* / compliance framing) and the
Phase 3 section of `refactoring-plan.md`. This doc is the *how*: how FreeEed runs AI as a
**local-first** model — no data leaves the machine, and it can be **proven** — while keeping
the option of an external model as a secondary path. Design principle throughout: **the data
never leaves the authorized boundary, and we can attest to it.**

## Where AI lives today (the seams to swap)
Two narrow chokepoints, which is what makes "go local" a config change, not a rewrite:

- **Chat / RAG (`ai_advisor`):** `common/llm.py` → `get_llm()` returns
  `ChatOpenAI(model=LLM_MODEL)`. Embeddings go through a single `embedding_function`
  (`common/embeddings.py`, `OpenAIEmbeddingFunction`). Config lives in `~/.freeeed/.env`
  (`LLM_MODEL`, `CHROMA_EMBED_MODEL`, `OPENAI_API_KEY`).
- **Structured extraction (external document pipelines):** call an OpenAI-style client
  directly (e.g. an `AzureOpenAI` client for field extraction). Same shape — a client
  pointed at an endpoint.

**Key fact:** every path speaks the **OpenAI API shape**. So "local" means *re-point the
`base_url`* at a local server and change the model name — the calling code barely moves.

## Design principle: OpenAI-compatible local serving
Run an open-weight model behind an **OpenAI-compatible HTTP endpoint** on infrastructure we
control. Callers keep using the same SDKs; only `base_url` + model name change (and a dummy
API key). This preserves a clean **toggle** between local and external without forking code.

## Serving layer — Ollama vs vLLM
- **Ollama** — easiest footprint; runs a quantized model on a workstation (CPU or a modest
  GPU), exposes `/v1` OpenAI-compatible + an embeddings endpoint. **Use for:** dev,
  single-user desktop (the FreeEed installer scenario), low volume.
- **vLLM** (or TGI) — high-throughput GPU server, OpenAI-compatible. **Use for:** production
  batch/extraction workloads and multi-user. Same API, so nothing else changes.
- **One code path, config-scaled:** desktop → Ollama; server → vLLM. The app doesn't know
  the difference.

## Models
- **Chat / RAG:** a mid-size instruct model (e.g. Llama / Qwen / Mistral family). Quality
  scales with size; balance against hardware.
- **Structured extraction:** a good instruct model with reliable JSON/function-style output;
  temperature 0; validate output against the schema. Mid-size is usually enough because the
  task is bounded (fill known fields), not open-ended.
- **Embeddings (local):** e.g. `nomic-embed-text` / `mxbai-embed-large` via the local
  server. **Gotcha:** local embedders have **different vector dimensions** than the current
  OpenAI 1536 — the vector store (Chroma) must be **re-ingested** when the embedder changes;
  you can't mix dimensions in one collection.
- **Pin versions.** The model + quantization are part of the validated pipeline; treat a
  model bump like a code change (re-validate — see below).

## Local OCR (document pipelines only)
If the source is confidential and text is extracted by OCR, the **OCR must be local too**
(e.g. Tesseract / PaddleOCR) — otherwise the raw document leaks at the OCR stage even with a
local LLM. Same "nothing leaves the boundary" rule, one step earlier in the pipeline.

## Egress control + the certification monitor
This is the artifact that makes the compliance claim real (see the CJIS briefing):

- **Enforce:** block outbound network from the AI process (firewall / network policy /
  isolated subnet); the local model + OCR need no internet.
- **Observe & attest:** monitor the AI process's network activity and record that **zero**
  external connections occurred over a run — a log an auditor (or a court) can rely on.
- **Belt-and-suspenders demo:** the whole thing runs with the network physically off.
- Build order: start with observability (prove current behavior), then enforce, then
  package the attestation as a first-class output.

## Reproducibility bundle — "defensible case snapshot" (Phase-3 capstone)
An export capturing everything needed to **re-run a review later and get the same
determinations** — the strongest form of a defensible *process*. **Local-only:** you can't pin a
cloud model (it isn't yours and is silently replaced), so this is a durable differentiator. Pairs
with the certification monitor (that proves *nothing left the machine*; this proves *you can
re-run it*).

- **Reproducibility manifest** (JSON at bundle root) = literally the *written documentation*
  Morgan v. V2X requires: `{model(name,sha256,quant), runtime(engine,version),
  decoding(temp=0,seed), prompts (#572), embeddings(model,chunking), software(build SHA+time —
  already stamped), corpus(hash,count), ground_truth(recall/elusion/precision)}`.
- **Document set**, **run logs** (#574: every query + retrieved context + output),
  **rationales/citations** (#575), **ground-truth validation** (blind sample + recall/elusion/precision).
- **The model** — pinned by hash; weights held once in the **content-addressed model store**
  (see `processing-engine.md`); for **external handoff** (expert / opposing counsel) the weights
  are **materialized into the bundle** so it's self-contained.

**Determinism (the hard part — don't overclaim):** pin **model hash + runtime engine/version +
greedy (temp 0)**, capture **exact prompt + retrieved context per query**, and pin **retrieval**
(embedding model/version, chunking, frozen index or captured retrieved-sets). GPU inference is
non-deterministic (parallel float reduction) → for **bit-exact** replay, run the archival/verify
pass on a **deterministic backend (CPU / fixed config)**. **Practical defensible bar = same
*determinations*** (greedy makes this rock-solid); offer bit-exact as a mode. *(The reproducibility
pitch itself must not overclaim — that would be a bad look for a defensibility product.)*

**The verifier:** `freeeed verify-snapshot <bundle>` → load pinned model+runtime → replay each
logged query with its captured context → diff vs. archived outputs → **certify match.** The
"hand it to opposing counsel, they re-run it" proof.

**Roadmap:** (1) capture config + logs + prompts + rationales (#574/#572/#575) during Phase-3 local
AI; (2) **snapshot export** (manifest + corpus + logs + model ref/weights + validation) — ships the
documentation first; (3) **re-run verifier** — the proof/demo. Idea from 2026-07 webinar prep (CRM).
Mic-drop line: *"you cannot put ChatGPT in a case file."*

## Hardware / sizing
- **Dev / desktop:** CPU-only works for small models (3–8B) at modest latency; a consumer
  GPU makes it comfortable. Fine for prototyping and single-doc/interactive use.
- **Production extraction:** needs a real **GPU** (VRAM sized to model + context) running
  vLLM for throughput. Size deliberately against target docs/hour; this is the main cost
  variable.

## Accuracy validation (don't switch on faith)
A local model won't automatically match the largest hosted models on messy real documents.
Before swapping anything in production:

1. Assemble a **labeled ground-truth sample**.
2. Run incumbent vs local; measure **field-level precision / recall** (and for retrieval,
   recall/elusion).
3. Only switch where the local model is within an acceptable delta; iterate model/prompt
   where it isn't.

The compliance case is the *why*; a **measured** accuracy delta is what makes the switch
defensible.

## Rollout
- **Toggle, not rip-out:** a `LOCAL_MODE` (or `LLM_BASE_URL`) switch selects local vs
  external at config time; keep the external path as the documented **secondary** option.
- Wire the **auto-start UX** (Phase 3 of the refactoring plan) so licensed users get the
  local model started in the background — no manual step.
- Ship the local model + embedder with the installer (or a first-run download), so
  "download, double-click, done" still holds.

## Open items
- Pick the default chat model and the default extraction model (+ quantization) per hardware
  tier; pin them.
- Choose the local embedder and plan the **one-time Chroma re-ingest** at the new dims.
- Decide OCR engine and validate it on representative scans.
- Build the egress **monitor/attestation** format (what the log/certificate looks like).
- Stand up the **precision/recall** validation harness with a labeled sample.
- Confirm desktop-vs-server serving split (Ollama / vLLM) and installer packaging.

## Related
`docs/local-ai-cjis-briefing.md` (compliance framing), `refactoring-plan.md` (Phase 3),
`processing-engine.md` (the processing engine this AI sits alongside).
