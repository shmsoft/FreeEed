# Running a Local LLM Under Strict Security Compliance — a FreeEed Briefing

**Purpose:** what it takes to apply AI (large language models) to criminal-justice,
civil-litigation, and other confidential or regulated records **without** creating a
compliance exposure — and why FreeEed is building toward a **local-first** AI model to
solve exactly this. This note discusses *requirements and architecture only*; it contains
no case data.

## The problem with "just use a cloud AI"
Criminal Justice Information (CJI) — the contents of police, arrest, and supplemental
reports — is governed by the **FBI CJIS Security Policy** (administered at the state level
by each state's CJIS Systems Agency; e.g., in California by the state Department of
Justice). The moment CJI is sent to a third-party AI endpoint, that endpoint and its
operator are pulled **into scope** of that policy. Meeting it through a commercial cloud AI
provider means, at minimum:

- **FedRAMP-authorized** cloud boundary (a government-authorized environment, not a
  standard commercial one).
- A signed **CJIS Security Addendum** binding the vendor and its staff.
- **Personnel screening** — background checks and fingerprint-based record checks for
  anyone with unescorted access to CJI; access by unvetted or out-of-jurisdiction
  personnel is restricted.
- **FIPS-validated encryption** in transit and at rest, defined **data residency**, and
  **advanced/multi-factor authentication**.
- Full **audit logging**, media protection, physical protection, and incident response.

A general-purpose commercial AI API typically does **not** satisfy these out of the box. It
can sometimes be done inside a dedicated government cloud boundary, but that is a heavy,
ongoing audit obligation — and every request still ships the sensitive record to someone
else's infrastructure.

Note the broader direction of travel: even in ordinary civil litigation, courts have begun
**barring** the upload of confidential material into mainstream AI unless the provider is
contractually prohibited from training on or disclosing it. The regulated-data world is
simply further ahead on the same principle.

## Why local-first changes the equation
If the model runs **on infrastructure you already control** and the data **never leaves the
authorized boundary**, most of the third-party burden disappears:

- **No external processor to vet** — no vendor addendum, no provider-side personnel
  screening, no data-residency question about someone else's cloud.
- The compliance surface shrinks to **your own** controls, which you were already
  operating for the case-management system.
- You can **prove** it: the AI component can be monitored and shown to make **zero**
  outbound network calls — a clean artifact for an auditor.

This is the same principle FreeEed already applies to evidence processing, where **no
outbound calls are permitted during imaging/processing** for forensic soundness. Extending
that "nothing leaves the machine" discipline to the AI layer is a continuation of FreeEed's
existing design, not a new departure.

## The same logic applies to civil matters, not just criminal justice
CJIS is the strictest and most explicit regime, which makes it a useful worst case — but
the underlying principle is general. **Any matter involving confidential or privileged
material** carries the same exposure the moment that material is sent to a third-party AI:

- **Civil litigation** — attorney-client-privileged and confidential documents. Courts have
  begun **restricting or barring** the upload of protected material into mainstream AI
  unless the provider is contractually prohibited from training on or disclosing inputs,
  with documentation retained. Disclosing privileged material to an outside AI can also
  raise **waiver** concerns — a risk with no upside.
- **Regulated industries** — health records, financial/PII/PCI data, trade secrets, and
  internal investigations, each under its own confidentiality regime.

In every case the local-first answer is identical: if the data never leaves the boundary,
there is **no third-party disclosure to police, no vendor to contractually bind, and
nothing to waive.** What CJIS *mandates* for criminal-justice data is simply **good
practice for any confidential matter** — and one local-first architecture covers them all.

## FreeEed's approach (what "local model" looks like in practice)
- **Local LLM served behind an OpenAI-compatible API** (Ollama for a workstation/dev
  footprint; a higher-throughput server such as vLLM for production). Because the interface
  is standard, existing extraction code that calls an OpenAI-style client re-points to the
  local endpoint with minimal change — the model runs locally, the calling code barely
  moves.
- **Local OCR / document intelligence.** If the source documents are regulated, the text
  extraction step must be local too (e.g., an on-box OCR engine) — otherwise the raw
  document leaks at the OCR stage even if the LLM is local. Same discipline, earlier in the
  pipeline.
- **Egress control + monitoring.** Network policy blocks outbound traffic from the AI
  process; a monitor records and can attest that no external connections occurred. This is
  the certification evidence.
- **Audit logging** of inputs/outputs within the boundary, encryption at rest, access
  control + MFA — the standard control families, now applied to a system you fully own.

## What you actually need to run a local model compliantly
A practical checklist:

1. **Boundary** — run the model inside the same authorized environment as the case data;
   no traffic to external AI services.
2. **No external calls** — local LLM *and* local OCR; enforce with network egress rules,
   and **monitor** to prove it.
3. **Encryption** — FIPS-validated, in transit and at rest.
4. **Access control** — least-privilege access to the model and its logs, with MFA.
5. **Audit** — log model usage; retain within the boundary.
6. **Personnel** — the same screening standard that already applies to staff touching the
   records applies to those operating the model.
7. **Documentation** — written policy describing the above, ready for audit.
8. **Accuracy validation** — see below; a compliance win is only useful if the extraction
   is still reliable.

## The honest engineering caveats
- **Accuracy.** A local open-weight model will not automatically match the largest hosted
  models on messy real-world documents. Plan a **validation pass**: run the local model and
  the incumbent against a labeled sample and measure **field-level precision and recall**
  before switching anything in production. The compliance case is the *why*; a measured
  accuracy delta is what makes the switch defensible.
- **Hardware.** Production-grade local inference needs appropriate **GPU** capacity; a
  developer's CPU-only box is fine for prototyping but not for throughput. Size this
  deliberately.
- **Model choice.** Structured fact-extraction is a good fit for mid-size local models;
  pick one, pin the version, and treat it as part of the validated pipeline.

## Bottom line
For regulated records, the cheapest path to "strict security compliance" is usually to
**stop sending the data out at all.** A local model, local OCR, enforced no-egress, and a
monitor that can attest to it turns a hard third-party-vendor compliance problem into a set
of controls you already run on your own infrastructure. FreeEed is building along exactly
this line — local-first AI as the default, with the private/no-outbound posture as the
selling point rather than an afterthought.

*(Requirements summarized here reflect the general shape of the CJIS Security Policy, which
is revised periodically; verify the current version and any state-specific obligations with
your CJIS Systems Officer.)*
