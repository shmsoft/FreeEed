<p align="center">
  <img src="docs/assets/FreeEed-Detective.png" alt="FreeEed — eDiscovery processing + AI review" width="900" />
</p>

# FreeEed

FreeEed is an open-source eDiscovery platform to **ingest, process, OCR, and review** large document collections — with optional AI assistance for investigation and analysis.

- Website: https://freeeed.org
- Support: https://freeeed.org/support/
- Docs (Wiki): https://github.com/shmsoft/FreeEed/wiki

---

## Download

### Fastest option (recommended for teams)
- [Buy support](https://freeeed.org/support/) and request a ready-to-run VM

### Latest stable release
- Complete Pack (stable): https://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-10.8.0.zip

### Nightly builds (unstable)
Nightly builds are produced automatically and may be broken.

- Complete Pack (nightly): https://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-10.8.1-SNAPSHOT.zip
- Windows Installer (nightly): https://shmsoft.s3.us-east-1.amazonaws.com/releases/FreeEed-10.8.1-SNAPSHOT-Windows.exe
- Linux Installer (nightly): https://shmsoft.s3.us-east-1.amazonaws.com/releases/FreeEed-10.8.1-SNAPSHOT-Linux.run

---

## Quick start

1. Follow the installation guide: https://github.com/shmsoft/FreeEed/wiki/FreeEed-Installation
2. **Complete Pack unzip key (if needed):** email mark@scaia.ai and tell us a little about your use case.

---

## Capabilities

- Runs on Windows, macOS, Linux, VirtualBox, and AWS
- Processes 1,400+ file types including MS Office and PST (via Apache Tika): https://tika.apache.org/
- OCR
- AI-assisted investigation (“talk to your eDiscovery documents”)
- AI analysis (responsive, privileged, “smoking gun”) — under development
- Video and audio transcription
- Document review
- “Imaging” (conversion of documents to PDF)

---

## How it works

At a high level:

FreeEed is built with AI/LLMs, Apache Tika, Lucene/Solr, and best practices from large-scale document processing.

---

## Documentation

Extensive documentation is available on the wiki:
https://github.com/shmsoft/FreeEed/wiki

---

## Developer guide

If you are a developer, you can build FreeEed from source.

- Developer notes: [for_developers_only.md](for_developers_only.md)

---

## Support / contact

- Phone: 713-568-9753  
- Email: mark@scaia.ai

---

## Git workflow

1. `git checkout dev && git pull`
2. `git checkout -b feature/<short-description>`
3. Work, commit, push → `git push -u origin feature/<name>`
4. Open PR: `feature/* → dev`
5. When `dev` is stable, PR: `dev → main`
6. Tag from `main`: `git tag vX.Y.Z && git push origin vX.Y.Z`
