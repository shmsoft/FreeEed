# FOIA Manual-Redaction MVP

**Status:** proposed (scope record) — 2026-10-04
**Owner:** Mark
**Context:** FOIA is the lead go-to-market vertical for 10.8.x (self-hosted eDiscovery/FOIA).
The ingest -> per-document-PDF -> review -> produce pipeline already works; the missing piece
for FOIA is **redaction of exemptions**. This record scopes the smallest sellable version:
**manual** redaction. AI-assisted redaction (FreeEed #601/#554) is the premium upsell, out of scope here.

## Why this, why now
- Most differentiated lane: FOIA incumbents (GovQA/Granicus, NextRequest, FOIAXpress) are
  request-intake portals, cloud SaaS — they do not do process-records-and-redact for small
  self-hosted agencies. That gap is validated by the MITRE FOIA Assistant intel and by Don Howell.
- The redaction/markup work doubles as the shared **FreeEed Viewer** (also needed by the plaintiff
  "Production Viewer").
- Self-hosted / "nothing leaves your network" is a decisive buy-reason for government.

## User story (end to end, on the self-hosted appliance or desktop)
1. Create a FOIA case -> ingest records (Google Vault mbox, loose files, PST) -> process with a
   one-click **"FOIA" preset** (enables per-document PDF).
2. Review in the browser; on each responsive doc, draw redaction boxes and tag each with an
   exemption code (b(6), b(7)(C), ...).
3. Export the **release set**: per-document **redacted PDFs** + a **redaction log**.

## In scope (the new build — 5 pieces)
1. **In-viewer redaction overlay** — open a doc's PDF, drag/move/delete rectangles; persist per doc.
   (This is the shared FreeEed Viewer work.)
2. **Exemption codes** — configurable label list (FOIA b(1)-b(9) + room for state exemptions);
   each box carries a code, shown on the redaction.
3. **Burn-in on export** — produce a redacted PDF where boxed content is TRULY removed
   (rasterize/flatten the page so no text hides under the box). Server-side, reusing the PDF-imaging
   pipeline.
4. **Redaction log** — CSV/PDF: doc, page, exemption code, count.
5. **"FOIA Request" preset** — one button that sets the per-doc-PDF processing profile.

## Out of scope (v2 / premium)
- AI auto-redaction (detect PII/names/SSNs) -> premium upsell (#601/#554).
- Redact-by-search across all docs; OCR re-layer for searchable redacted PDFs; full Vaughn index
  with written justifications; word-level (vs area) redaction; any public request-intake portal
  (that is the incumbents' turf; FreeEed is the process+redact+release tool).

## Non-negotiable correctness requirement
Redaction MUST delete the content (flatten to image / remove the text layer), never a black box over
live text. A selectable-text-under-black-box leak is a career-ending FOIA mistake and would destroy
product credibility. This is the single thing the MVP must get provably right.

## Effort (rough)
- In-viewer redaction overlay (PDF.js + draw/persist): biggest piece, medium front-end.
- Burn-in redacted-PDF export (server): medium, and must be correct.
- Exemption list / redaction log / FOIA preset: small each.
- Ingest, per-doc PDF, review, export already exist. Ballpark: a focused few-week MVP; it also
  bootstraps the FreeEed Viewer.

## The sell
"Process your records, redact exemptions, and release - on your own server, for a fraction of a FOIA
SaaS, with nothing leaving your network." Manual redaction v1 -> AI-assisted redaction as the paid upgrade.

## Tracking
GitHub epic + 5 sub-issues (see the FOIA manual-redaction epic in shmsoft/FreeEed).
