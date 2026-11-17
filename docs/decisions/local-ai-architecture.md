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
Concrete defaults below, mapped to the hardware tiers in
[hardware-sizing.md](hardware-sizing.md). Every model here is a one-line `ollama pull` on the
desktop and serves the same weights under vLLM on a server — "pick your tier, pull the model,
done." **Named families/versions rev fast** — treat these as the current pinned defaults, not
a permanent list.

- **Chat / RAG** (interactive Q&A over the doc set) — quality scales with VRAM:
  | Tier (VRAM) | Model | Notes |
  |---|---|---|
  | Entry (16 GB) | **Qwen3 8B** or **Llama 3.1 8B** (Q4/Q5) | Solid instruction-following + citation behavior for RAG. |
  | Recommended (24 GB) | **Qwen3 14B** or **Gemma 3 12B** (Q4/Q5) | Best quality-per-VRAM at the 4090 tier. |
  | Business (48 GB) | **Qwen3 32B** or **Mistral Small 3.x (24B)** | Fits an RTX 6000 Ada; approaches hosted-model quality. |
  | High-perf / server (48–80 GB) | **Llama 3.3 70B** or **Qwen3 72B** (Q4) | The "as good as cloud for most tasks" tier; 48 GB tight, 80 GB comfortable. |
- **Structured extraction:** an instruct model with reliable JSON/function-style output —
  **Qwen3 14B/32B** or **Mistral Small 3.x**. Temperature 0; validate output against the
  schema. Mid-size is enough because the task is bounded (fill known fields), not open-ended;
  don't reach for 70B here.
- **Embeddings (local):** **`nomic-embed-text`** (768-dim; good default) or
  **`mxbai-embed-large`** (1024-dim; better retrieval); **`bge-m3`** for multilingual /
  hybrid. **Gotcha:** these have **different vector dimensions** than the current OpenAI 1536
  — the vector store (Chroma) must be **re-ingested** when the embedder changes; you can't mix
  dimensions in one collection.
- **Pin versions.** The model + quantization are part of the validated pipeline; treat a
  model bump like a code change (re-validate — see below).

**Candidates to evaluate (not yet pinned):**
- **Muse Glimmer 30B** (Meta, Apache 2.0; `huggingface.co/meta-models/Muse-Glimmer-30B`) —
  agent-tuned (tool use, multi-step, failure recovery), **131k context**, built-in speculative
  decoding, and **multimodal (text + image)**. The multimodal angle is the reason to look:
  it reads **page images / screenshots**, so it could complement — or reduce — the OCR step for
  scanned/image documents, which text-only models can't. Sits in the Recommended→Business tier
  (24 GB quantized, 48 GB comfortable). **Verify first:** the HF owner is `meta-models`, not
  Meta's known orgs (`meta-llama` / `facebook`), and it surfaced via a spoofed-looking marketing
  email — confirm the org is genuinely Meta before adopting. **Evaluate, don't pin on the spec
  sheet:** run it on labeled ground truth vs. the Qwen3/Llama defaults, and specifically test the
  image-document path against our OCR pipeline (its differentiator).

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
**Full tiered spec** (workstation & server, entry/recommended/high-performance — CPU/GPU-VRAM/RAM/
storage): [hardware-sizing.md](hardware-sizing.md).

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
- **Private mobile access (opportunity):** make FreeEedUI review + AI **mobile-responsive** as a
  *private viewport* to the on-prem/appliance — phone over LAN/VPN, heavy processing stays
  server-side, **data never leaves.** Answers the market's "mobile AI gap" (lawyers are mobile,
  AI is desktop-bound) the FreeEed way (private, not cloud). Low effort, high signal.
- ~~Pick the default chat model and the default extraction model (+ quantization) per hardware
  tier~~ — done (see **Models** above); **pin the exact hashes** once validated.
- Choose the local embedder and plan the **one-time Chroma re-ingest** at the new dims.
- Decide OCR engine and validate it on representative scans.
- Build the egress **monitor/attestation** format (what the log/certificate looks like).
- Stand up the **precision/recall** validation harness with a labeled sample.
- Confirm desktop-vs-server serving split (Ollama / vLLM) and installer packaging.

## Packaging and provider-disclosure posture (PROPOSED 2026-11-16 — not yet decided)

**Status: proposed.** Recorded here so the reasoning survives; needs Mark's confirmation
before anything is built. Two changes, both moving toward *openness* rather than concealment.

### 1. Stop shipping the AI layer as a compiled executable only

Today the Python AI layer ships compiled, for one reason: to make the source not-too-easy to
lift. The proposal is to stop relying on that, and to protect the layer by **entitlement**
instead of by **obfuscation**.

**The argument is not convenience, it is that obfuscation contradicts the product claim.**
This document's own design principle is "the data never leaves the authorized boundary, **and
we can attest to it**." An opaque binary that opens network sockets is the worst possible
artifact to hand an auditor, a CISO, or an opposing expert: non-egress cannot be demonstrated
about a black box, only asserted. The buyers this architecture is aimed at — forensic
examiners, law-enforcement units under CJIS, arbitration practices — audit their tools as a
matter of professional method. For them **auditability is a feature, and concealment is the
risk**, which inverts the original reasoning.

Two secondary points:

- The deterrent was weaker than it looked. Standard Python bundlers are unpacked with a single
  well-known command, so little is actually being given up.
- A compiled layer is materially harder to diagnose on a customer's machine, and that cost is
  paid in support hours at exactly the moment a customer is already unhappy.

**What protects the layer instead:** the offline activation key already designed for the
commercial tier (entitlement, not secrecy); the integration surface into the processing
pipeline, which is where the real work is; and the support relationship. A retrieval wrapper
around an OpenAI-shaped API is not the scarce asset — the pipeline it is wired into is.

**Open:** the specific replacement mechanism is **Mark's to state** — key-gating and
source-level obfuscation point in opposite directions, and only the first is compatible with
the audit argument above.

### 2. Do not conceal the external-model path — label it

External/cloud models (OpenAI and equivalents) are **bring-your-own-key and commodity**: any
user can wire an API key to a document set today, without us. Treating that path as something
to hide costs credibility and buys nothing.

So the posture is: **cloud AI is named plainly, documented, and neither promoted nor
apologized for.** This is *not* a promotion of the external path. The local model remains the
flagship and the differentiator, for the reasons in this document and in
`docs/local-ai-cjis-briefing.md`. What changes is that cloud AI stops being an embarrassment
and becomes clearly-labeled table stakes.

### 3. The consequence that is not cosmetic: embeddings

BYOK is an honest posture for an interactive chat box — the user supplied the key and accepted
the data flow. **It is not an honest posture for ingest-time egress.**

As built, embeddings are produced through `common/embeddings.py` against a hosted embedding
function, and there is no `base_url` seam in the AI layer at all. So document text leaves the
machine **when a case is ingested**, before the user has chosen to ask any cloud question at
all. No label on a chat screen discloses that, and no provider dropdown as currently wired
would change it.

Therefore the proposal carries three requirements, without which "plain labeling" is cosmetic:

1. **The provider setting must govern embeddings as well as chat.** One switch, both paths.
2. **Ingest should default to local embeddings**, so the default install does not export case
   text.
3. **The settings UI must distinguish the service address from the model endpoint.** The
   current Program Settings fields invite exactly the opposite reading: "AI Advisor Endpoint"
   is the address of *our* service, not the model's, while the provider list offers one hosted
   option — so a user who wants a local model cannot express it, and reasonably concludes they
   have, which is worse than being told no.

Item 3 has already been observed in the field, not predicted.

### Pricing consequence (summary; detail is in the private operations records)

If the external path is commodity, then **the paid line cannot be "access to AI."** It is
local AI plus updates and support — which is what the activation-key model already assumes.
The two are consistent; this only removes an option nobody should have been selling.

## Related
`docs/local-ai-cjis-briefing.md` (compliance framing), `refactoring-plan.md` (Phase 3),
`processing-engine.md` (the processing engine this AI sits alongside).
