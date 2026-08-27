# FreeEed Forensics Edition — User Guide

**Edition:** Forensics Edition v1 (ISO build 20260722)  
**Base OS:** MX Linux (Debian stable)  
**Default login:** `fit` / `fit`  
**Contributor:** Remco Siderius

---

## What is FreeEed Forensics Edition?

FreeEed Forensics Edition is a **bootable live Linux ISO** that bundles FreeEed with a curated
set of open-source digital forensics and eDiscovery tools. Boot it on any x86-64 PC or
workstation and get a ready-to-work environment — no installation required, no toolchain to
assemble.

**Target users:** forensic investigators, eDiscovery practitioners, litigation support
specialists, and law firms running an in-house forensics lab.

**Key properties:**
- All processing is **local** — nothing is sent to external servers.
- Includes **NVIDIA + CUDA** drivers for GPU-accelerated AI inference (Ollama).
- Ships with FreeEed's full runtime (Solr, Tika, bundled tools).
- GPL and other open-source tools are aggregated (not merged with FreeEed's Apache-2.0
  code) — see [Attribution & Licensing](forensics-edition-attributions.md) for details.

---

## Getting Started

### System requirements
| | Minimum | Recommended |
|---|---|---|
| CPU | 64-bit, 4 cores | 8+ cores |
| RAM | 8 GB | 16–32 GB |
| Storage (live / USB) | 64 GB USB 3.0+ | 128 GB USB 3.0 / SSD |
| Storage (installed) | 120 GB | 256 GB SSD |
| GPU | — | NVIDIA (CUDA, 8 GB+ VRAM for local AI) |

### Booting the ISO
1. Write the ISO to a USB drive:
   ```
   # Linux/macOS
   sudo dd if=FreeEed-20260722.iso of=/dev/sdX bs=4M status=progress
   # or use Balena Etcher (Windows/macOS/Linux GUI)
   ```
2. Boot the target machine from the USB drive (set the BIOS/UEFI boot order or use the
   one-time boot menu, usually F12 or Del).
3. At the login prompt use: **username** `fit`, **password** `fit`.
4. The desktop loads directly into the ready-to-use environment.

### Verifying the download
Always verify the ISO before booting, especially on forensic hardware:
```
md5sum FreeEed-20260722.iso
# Expected: 82bf413fc2b2d27539c5e5d1eda64974
```

### Installing to disk (optional)
The ISO is a live system and can also be installed permanently using the **MX Installer**
shortcut on the desktop. A permanent install enables full-disk encryption and faster I/O.

---

## Running FreeEed

FreeEed is pre-installed and pre-configured.

1. Open a terminal and start the required services:
   ```
   cd ~/freeeed
   ./start_all.sh
   ```
2. Launch the FreeEed desktop UI from the application menu or:
   ```
   ./start_freeeed.sh
   ```
3. The FreeEed web review interface (FreeEedUI) is available at **http://localhost:8090**
   once services are running.

For a full walkthrough see `docs/FreeEed-demo.md` inside FreeEed, or the FreeEed manual at
`docs/FreeEedManual.pdf`.

---

## Installed Tool Inventory

Software is catalogued in `/home/fit/Temp` (split into debs, snaps, flatpaks, and
user-installed apps). The table below lists the main tools by category.

### eDiscovery & Legal Review
| Tool | Purpose |
|------|---------|
| **FreeEed** | End-to-end eDiscovery processing, review, and production |
| **Apache Solr** | Full-text search and index (FreeEed runtime) |
| **Apache Tika** | Document text/metadata extraction (FreeEed runtime) + standalone GUI |
| **LibreOffice** | Document → PDF imaging / production rendering |
| **ExifTool** | Metadata extraction from any file type |
| **Tesseract OCR** | OCR — searchable text from scanned images |
| **poppler-utils / qpdf / pdftk** | PDF text extraction, manipulation, redaction prep |

### Digital Forensics & Imaging
| Tool | Purpose |
|------|---------|
| **Autopsy / Sleuthkit** | Full-featured disk forensics |
| **Guymager** | Forensic disk imaging (E01/AFF/DD) |
| **ddrescue** | Disk recovery / imaging of failing media |
| **FTK Imager** (via Wine) | Forensic imaging, compatible with existing FTK workflows |
| **bulk_extractor** | PII and feature extraction from raw disk images |
| **PhotoRec / foremost** | File carving from disk images |
| **Volatility 3** | Memory forensics |

### Email & Communication Analysis
| Tool | Purpose |
|------|---------|
| **libpff / pff-tools** | PST/OST bulk export (large mailstores) |
| **libpst / readpst** | PST → mbox/EML conversion |
| **Evolution** | PST import and email client |

### Network Forensics & Analysis
| Tool | Purpose |
|------|---------|
| **Wireshark** | Network packet capture and analysis |
| **Angry IP Scanner** | IP/port discovery |
| **Sniffnet** | Network traffic monitoring |
| **EtherApe** | Graphical network activity |
| **Zenmap** | Nmap GUI, network mapping |
| **Maltego** | Link analysis and open-source intelligence |

### Data Analysis & Databases
| Tool | Purpose |
|------|---------|
| **CyberChef** | Data transformation, encoding/decoding, analysis |
| **OpenRefine** | Data cleaning and transformation |
| **DB Browser for SQLite** | SQLite inspection and queries |
| **DBeaver** | Multi-database client |
| **fqlite** | Forensic SQLite recovery / analysis |
| **BaseX** | XML database and XQuery |

### Mobile & App Data
| Tool | Purpose |
|------|---------|
| **iLEAPP / aLEAPP** | iOS and Android logical artifact extraction (GUI + CLI) |

### Password & Encryption
| Tool | Purpose |
|------|---------|
| **fcrackzip** | ZIP password recovery |
| **PDFcrack** | PDF password recovery |
| **John the Ripper** | Password hash cracking |

### Hashing & Integrity
| Tool | Purpose |
|------|---------|
| **hashdeep / md5deep** | Recursive/bulk hashing for dedup and chain-of-custody verification |
| **Quickhash** | GUI file/directory hashing |
| **Grsync** | Verified file synchronization |

### Audio & Video
| Tool | Purpose |
|------|---------|
| **ffmpeg** | Media conversion, extraction, analysis |
| **WinFF / HandBrake** | Video conversion GUIs |
| **Audacity / Audacious** | Audio editing and playback |
| **Praat** | Phonetic/voice analysis |
| **SpeechNote** | Speech-to-text transcription |
| **whisper-ctranslate2** | Local AI speech transcription (Whisper, runs offline) |

### Malware & Threat Analysis
| Tool | Purpose |
|------|---------|
| **ClamTK** | Malware scanning |
| **YARA** | Pattern matching / malware classification |
| **binwalk** | Firmware/binary analysis and extraction |

### Statistical & Analytical Utilities
| Tool | Purpose |
|------|---------|
| **benfordcheck** | Benford's law analysis of numeric datasets (fraud indicator) |
| **RegRipper** | Windows registry forensics |
| **plaso / log2timeline** | Timeline generation from artifacts |
| **ent** | Entropy analysis |

### Local AI (on-box, no internet required)
| Tool | Purpose |
|------|---------|
| **Ollama** | Local LLM server (OpenAI-compatible API; runs models offline) |
| Bundled models | See `/home/fit/.ollama/models` for the installed model list |

> **Privacy note:** Ollama runs entirely on the local machine. With the GPU drivers bundled,
> inference runs on the NVIDIA GPU. No data is sent anywhere. This is the same local-AI
> foundation that FreeEed's AI advisor (Phase 3) uses — the ISO already carries it.

---

## Versioning

| Field | Value |
|-------|-------|
| Edition name | FreeEed Forensics Edition |
| ISO build date | 20260722 |
| Base OS | MX Linux (Debian stable) |
| FreeEed version | 10.8.5-SNAPSHOT (see **Help → About** in the FreeEed UI for the exact build SHA) |

**Version scheme:** ISO builds are identified by build date (`YYYYMMDD`). Official releases
will carry a semantic version (e.g., `v1.0.0`) and a published checksum alongside the download.

---

## Download & Checksum

| | |
|---|---|
| File | `FreeEed-20260722.iso` (note: the initial pre-release build shipped with a three-e typo in the filename; the MD5 listed here applies to that original file) |
| Size | ~58 GB |
| MD5 | `82bf413fc2b2d27539c5e5d1eda64974` |
| Hosted | Contact Mark for the current download location (S3/torrent TBD for public release) |

Always verify the checksum before use.

---

## Licensing

FreeEed itself is licensed under the **Apache License, Version 2.0**. The ISO bundles
independently-licensed open-source tools (GPL, LGPL, MIT, and others). Bundling by
aggregation does not change FreeEed's license, but the ISO as a whole must respect each
tool's terms. A full per-tool attribution and license table is in
[`docs/forensics-edition-attributions.md`](forensics-edition-attributions.md).

---

## Known Gaps / Planned Additions

The following are recommended for a future build (see
`docs/decisions/forensics-iso-edition.md`):

- **plaso / log2timeline + Timesketch** — full artifact timeline and visualization (plaso is
  present; Timesketch server integration is not yet configured)
- Reproducible build script (live-build / config-as-code) for provenance and bus-factor
  reduction
- Version manifest shipped with each ISO (package + version + license lockfile)

---

## Feedback & Contributions

Suggestions, bug reports, and additions: open an issue at
https://github.com/shmsoft/FreeEed/issues or contact Remco Siderius (the edition
maintainer) through the FreeEed project.

Build scripts and configuration contributed to the FreeEed repo fall under the
Contributor License Agreement (`CLA.md`).
