# Hardware sizing for FreeEed + local AI

Target hardware for running FreeEed — the **processing + review engine** and the **on-prem local AI**
— in two configurations (workstation and server), each in three tiers. These are **spec targets, not
part numbers**: any vendor's equivalent works (workstation towers / rack servers from any OEM, or a
custom build).

- **OS:** Ubuntu Linux (the engine's native platform; document review is browser-based, so any OS can
  view it).
- **GPU:** NVIDIA / CUDA (only needed for the local-AI layer; processing/review don't require a GPU).

## Workstation — single expert / small firm / solo attorney

| Tier | CPU | GPU (VRAM) | RAM | Storage |
|---|---|---|---|---|
| **Entry** | 8-core | RTX 4000-class, **16 GB** | 32 GB | 1 TB NVMe |
| **Recommended** | 16-core | **24 GB** (RTX 4090) or **48 GB** (RTX 6000 Ada) | 64 GB | 2 TB NVMe |
| **High-performance** | 24–32 core | **48 GB** (RTX 6000 Ada) | 128 GB | 4 TB NVMe |

The local AI model runs on the box, so **GPU VRAM sets the tier** — larger VRAM runs larger models,
for faster and higher-quality analysis. Suggested chat/RAG models per tier: **Qwen3 8B / Llama 3.1
8B** (16 GB) → **Qwen3 14B / Gemma 3 12B** (24 GB) → **Qwen3 32B / Mistral Small 3.x** (48 GB), with
**Llama 3.3 70B / Qwen3 72B** at the top server tier. Full model guidance (extraction + embeddings) in
[local-ai-architecture.md](local-ai-architecture.md#models).

## Server — enterprise / multi-user review / scale-out

| Tier | CPU | GPU (VRAM) | RAM | Storage |
|---|---|---|---|---|
| **Entry** | 32-core | 1× **48 GB** | 128 GB | 4–8 TB NVMe |
| **Recommended** | 64-core | 1–2× **48 GB** (dedicated AI node) | 256 GB | 16 TB+ NVMe |
| **High-performance** | 96–128 core (dual-socket) | 2× 48 GB or **80 GB** (A100/H100) | 512 GB+ | NVMe + scale-out |

For multi-user / high-volume, the GPU sits on a **dedicated AI node** so document processing and AI
serving scale independently.

## Sizing notes

- **Storage:** NVMe required. Plan **~3–5× the matter's source-data size** (natives + extracted text +
  search index + imaged PDFs + working space), plus tens of GB for the local-model store.
- **GPU:** NVIDIA / CUDA. Consumer **RTX 4090 (24 GB)** is an excellent entry/solo tier; professional
  **RTX 6000 Ada (48 GB, ECC)** for business/enterprise; **A100/H100 (80 GB)** for the top server tier.
  VRAM sizes to model + context. (Local-model serving: Ollama on the desktop, vLLM on a server — see
  [local-ai-architecture.md](local-ai-architecture.md).)
- **CPU / OCR:** processing (text extraction, OCR, imaging, indexing) is CPU-parallel per document and
  benefits from strong per-core throughput + fast NVMe.

## Phasing (certify what's ready first)

- **eDiscovery processing + review engine — validated & shipping.** The CPU / RAM / storage / OS
  configs are ready **now**.
- **Local-model AI layer (the GPU tiers) — being finalized.** The GPU/VRAM targets are sized for the
  on-prem local AI; they firm up as that capability lands (see the [Local AI umbrella, #597](https://github.com/shmsoft/FreeEed/issues/597)).
- **Multi-node scale-out (top server tiers) — the growth path.** The **single-box** workstation and
  server configs are the v1 targets; multi-node is planned, not required today.
