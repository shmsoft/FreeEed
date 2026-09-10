# Windows code signing — options & recommendation

**Status:** proposed (2026-09-10) — unblocks #581 and the signed GA. Decision for Mark.
**Context:** the daily/GA `.exe` is unsigned → SmartScreen "Unknown publisher," which is
fatal for the trust-first school/agency FOIA audience. We build the `.exe` with `makensis`
on the **Ubuntu** box, so signing must work **from Linux/CI**, not a plugged-in USB token.

## The 2023 rule (why the old way is gone)
Since June 2023, publicly-trusted code-signing **private keys must live on FIPS-140-2
hardware** — a USB token or a **cloud HSM**. You can no longer download a `.pfx` and sign
with a bare file. A physical USB token can't be used from a headless Linux build box, so
**cloud-HSM signing is the only sane path for our pipeline.**

## Options (all cloud-HSM, sign from Linux)

| Option | Cost (approx) | Trust | Linux signing | Notes |
|---|---|---|---|---|
| **Azure Trusted Signing** | **~$10/mo** | Fast SmartScreen reputation (MS-run) | **Yes** (`AzureSignTool`, .NET, runs on Linux) | Cheapest; **we already have Azure**; needs org identity verification. **Recommended.** |
| DigiCert KeyLocker | ~$400–800/yr | OV or **EV** (EV = instant SmartScreen) | Yes (their client + CI) | Established; pricier. |
| SSL.com eSigner | ~$300–600/yr | OV/EV | Yes (eSigner CKA / cloud API) | Mid-cost alternative. |
| Sectigo/DigiCert OV/EV on **USB token** | ~$200–600/yr | OV/EV | **No** (physical token) | ✗ can't sign headless on Linux — avoid. |

**EV vs OV:** EV clears SmartScreen *immediately*; OV builds reputation over downloads/time.
Azure Trusted Signing isn't "EV" but gains Microsoft-backed reputation quickly — good enough
for launch, at a fraction of EV cost.

## Recommendation: **Azure Trusted Signing**
- **Cheapest by far (~$10/mo)** and you **already run Azure** — no new vendor relationship.
- **Signs from the Ubuntu build box** via `AzureSignTool` (or `jsign`/`osslsigncode` with the
  Trusted Signing endpoint), so it drops straight into the existing `SIGN_WIN` hook (`c6cd9a6a`).
- Identity is Scaia, Inc. (same org already used for the Mac Developer ID).
- Requirement: complete **Azure Trusted Signing account + identity validation** (a few days of
  org verification). Start that now — it's the long pole, and only Mark can do it.

Fallback if Trusted Signing eligibility is a problem: **SSL.com eSigner** (mid-cost, Linux-friendly).

## How it wires into the build (SIGN_WIN hook)
The `SIGN_WIN` hook (already inert-by-default in `release_freeeed_complete.sh`, `c6cd9a6a`)
runs, after `makensis` produces `FreeEed-$VERSION-Windows.exe`:
```
AzureSignTool sign \
  -kvu <trusted-signing-endpoint> -kvc <cert-profile> \
  -tr http://timestamp.acs.microsoft.com -td sha256 -fd sha256 \
  FreeEed-$VERSION-Windows.exe
```
(secrets via env/Azure creds — never committed). Then `signtool verify` / `osslsigncode verify`
confirms the signature + timestamp before publish.

## Mark's next actions (the only #1 work only you can do)
1. **Start Azure Trusted Signing enrollment** (org identity validation) — the long pole.
2. Once active: give the build box the Trusted Signing endpoint + a service-principal cred
   (env vars), and Claude wires `AzureSignTool` into the `SIGN_WIN` hook.
3. Build a **signed** `.exe`, install-test on the **AWS Windows VM**, confirm **no SmartScreen
   "unknown publisher."** That gates the signed GA and the FOIA marketing push.
