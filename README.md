<p align="center">
  <img src="docs/assets/FreeEed-Detective.png" alt="FreeEed — eDiscovery processing + AI review" width="900" />
</p>

# FreeEed

> **Mission:** Make professional-grade eDiscovery and AI over sensitive documents *affordable, private, and self-hosted* — open-source software so any lawyer, investigator, or small firm can process, search, review, and question their own data on their own machine, without sending it to anyone.

## Quickest Start
- [Buy support](https://freeeed.org/support/) and [request](https://freeeed.org) a ready-to-run VM
- [Download the latest release as zip](https://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-10.8.4.1.zip)
- [Download Windows installer release](https://shmsoft.s3.us-east-1.amazonaws.com/releases/FreeEed-10.8.4.1-Windows.exe)
- [Download Linux installer release](https://shmsoft.s3.us-east-1.amazonaws.com/releases/FreeEed-10.8.4.1-Linux.run)
- [Daily build](https://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-latest.zip) *(unstable)*
- [Daily build of Windows Installer](https://shmsoft.s3.us-east-1.amazonaws.com/releases/FreeEed-latest-Windows.exe) *(unstable)*
- [Daily build of Linux Installer](https://shmsoft.s3.us-east-1.amazonaws.com/releases/FreeEed-latest-Linux.run) *(unstable)*

## Quick Start
1. Follow the [installation instructions](https://github.com/shmsoft/FreeEed/wiki/FreeEed-Installation)
2. Run FreeEed and register on first launch — we’ll email you a free activation key right away. Tell us a little about yourself; we’d love to hear from you. ([mark@scaia.ai](mailto:mark@scaia.ai))

## Capabilities
- Works on Windows, macOS, Linux, VirtualBox, and Amazon AWS cloud
- Ability to process 1,400+ file types, including MS Office and PST files (see [Apache Tika formats](https://tika.apache.org/))
- OCR
- AI (talk to your eDiscovery documents)
- AI analysis (responsive, privileged, smoking gun — under development)
- Video and audio transcription
- Document review
- “Imaging” (conversion of documents to PDF)

## Documentation
Extensive documentation is available on the [wiki](https://github.com/markkerzner/FreeEed/wiki), including how to [run FreeEed over RDP on Ubuntu](https://github.com/shmsoft/FreeEed/wiki/Run-FreeEed-over-RDP-on-Ubuntu).

## How it works
FreeEed is built with AI/ChatGPT, Tika, Lucene, and Solr and embodies best practices in Big Data.

## To build the project
If you are a developer, you can build the project from source.

Check out the documentation on the [For Developers](for_developers_only.md) page.

## For assistance
- Phone: 713-568-9753  
- Email: [mark@scaia.ai](mailto:mark@scaia.ai)

## Git Workflow
1. `git checkout dev && git pull`
2. `git checkout -b feature/<short-description>`
3. Work, commit, push → `git push -u origin feature/<name>`
4. Open PR: `feature/* → dev`
5. When `dev` is stable, PR: `dev → main`
6. Tag from `main`: `git tag vX.Y.Z && git push origin vX.Y.Z`
