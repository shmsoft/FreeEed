# FreeEed Forensics Edition — Attributions & Licenses

This document lists every third-party tool bundled in the FreeEed Forensics Edition ISO,
together with its author/maintainer, license, and (where relevant) its source or homepage.

FreeEed itself is licensed under the **Apache License, Version 2.0**
(https://www.apache.org/licenses/LICENSE-2.0). The ISO is an **aggregation** of
independently-licensed software — aggregation does not change FreeEed's license — but the
ISO as a whole must honour each tool's terms. GPL tools require that source code be available
on request; this is satisfied by each tool's upstream project.

---

## Summary of license families present

| License | Tools (count, representative) |
|---------|-------------------------------|
| Apache 2.0 | FreeEed, Apache Solr, Apache Tika, OpenRefine |
| GPL v2 | hashdeep/md5deep, John the Ripper, Sleuthkit, ddrescue |
| GPL v2+ | binwalk, ClamAV/ClamTK, fcrackzip, Guymager, Zenmap/Nmap, Wireshark |
| GPL v3 / v3+ | FTK Imager (Wine), Praat, RegRipper, Quickhash |
| LGPL v2.1+ | libpff/pff-tools, libpst/readpst, ffmpeg (LGPL components) |
| MIT | CyberChef, fqlite, whisper-ctranslate2, Ollama |
| CDDL + GPL | DBeaver (Eclipse dual-license) |
| BSD variants | Autopsy (Apache 2.0/CDDL), bulk_extractor, ExifTool (Perl Artistic/GPL), YARA (BSD-3-Clause) |
| Volatility Software License | Volatility 3 (open-source, non-commercial free) |
| Other open-source | benfordcheck (see note below), Ollama (MIT), YARA (BSD-3-Clause) |

---

## Per-tool attribution table

### eDiscovery & Legal Review

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **FreeEed** | SHMsoft, Inc. / Mark Kerzner | Apache 2.0 | https://github.com/shmsoft/FreeEed |
| **Apache Solr** | Apache Software Foundation | Apache 2.0 | https://solr.apache.org |
| **Apache Tika** | Apache Software Foundation | Apache 2.0 | https://tika.apache.org |
| **LibreOffice** | The Document Foundation | MPL 2.0 | https://www.libreoffice.org |
| **ExifTool** | Phil Harvey | Perl Artistic License / GPL | https://exiftool.org |
| **Tesseract OCR** | Google / Tesseract team | Apache 2.0 | https://github.com/tesseract-ocr/tesseract |
| **poppler-utils** | poppler developers | GPL v2+ | https://poppler.freedesktop.org |
| **qpdf** | Jay Berkenbilt | Apache 2.0 | https://qpdf.sourceforge.io |
| **pdftk** | PDF Labs | GPL v2 | https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit |

### Digital Forensics & Imaging

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **Autopsy** | Basis Technology | Apache 2.0 / CDDL | https://www.autopsy.com |
| **Sleuth Kit** | Brian Carrier / Basis Technology | CPL 1.0 + GPL | https://www.sleuthkit.org |
| **Guymager** | Guy Voncken | GPL v2+ | https://guymager.sourceforge.io |
| **ddrescue** | Antonio Diaz Diaz | GPL v2+ | https://www.gnu.org/software/ddrescue |
| **FTK Imager** (via Wine) | Exterro, Inc. | Proprietary freeware | https://www.exterro.com/ftk-imager — wine wrapper only; FTK Imager itself is proprietary freeware; redistribution subject to Exterro's end-user terms |
| **bulk_extractor** | Simson Garfinkel / NIST | Public Domain / BSD | https://github.com/simsong/bulk_extractor |
| **PhotoRec / TestDisk** | Christophe Grenier | GPL v2+ | https://www.cgsecurity.org |
| **foremost** | Jesse Kornblum / Nick Mikus | Public Domain | https://foremost.sourceforge.net |
| **Volatility 3** | Volatility Foundation | Volatility Software License (open-source, non-commercial free) | https://github.com/volatilityfoundation/volatility3 |
| **binwalk** | ReFirmLabs | MIT | https://github.com/ReFirmLabs/binwalk |

### Email & Communication Analysis

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **libpff / pff-tools** | Joachim Metz | LGPL v2.1+ | https://github.com/libyal/libpff |
| **libpst / readpst** | Carl Byington et al. | GPL v2+ | https://www.five-ten-sg.com/libpst |
| **Evolution** | GNOME project | GPL v2+ | https://wiki.gnome.org/Apps/Evolution |

### Network Forensics & Analysis

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **Wireshark** | Wireshark Foundation | GPL v2+ | https://www.wireshark.org |
| **Angry IP Scanner** | Anton Keks | GPL v2 | https://angryip.org |
| **Sniffnet** | Giuliano Bellini | MIT / Apache 2.0 | https://github.com/GyulyVGC/sniffnet |
| **EtherApe** | Juan Toledo / EtherApe team | GPL v2+ | https://etherape.sourceforge.io |
| **Zenmap (Nmap)** | Gordon Lyon (Fyodor) | Nmap Public Source License (GPL-based) | https://nmap.org |
| **Maltego** | Maltego Technologies | Proprietary (community edition free) | https://www.maltego.com — redistribution subject to Maltego's terms |

### Data Analysis & Databases

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **CyberChef** | GCHQ | Apache 2.0 | https://github.com/gchq/CyberChef |
| **OpenRefine** | OpenRefine contributors | BSD 3-Clause | https://openrefine.org |
| **DB Browser for SQLite** | DB Browser team | GPL v2+ / MPL | https://sqlitebrowser.org |
| **DBeaver** | DBeaver Corp | Apache 2.0 | https://dbeaver.io |
| **fqlite** | Dirk Pawlaszczyk | MIT | https://github.com/dpawlasz/fqlite |
| **BaseX** | BaseX team | BSD 3-Clause | https://basex.org |

### Mobile & App Data

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **iLEAPP** | Alexis Brignoni | MIT | https://github.com/abrignoni/iLEAPP |
| **aLEAPP** | Alexis Brignoni | MIT | https://github.com/abrignoni/aLEAPP |

### Password & Encryption

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **fcrackzip** | Marc Lehmann | GPL v2 | http://oldhome.schmorp.de/marc/fcrackzip.html |
| **PDFcrack** | Henning Norén | GPL v2+ | https://pdfcrack.sourceforge.net |
| **John the Ripper** | Openwall / Solar Designer | GPL v2 | https://www.openwall.com/john |

### Hashing & Integrity

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **hashdeep / md5deep** | Jesse Kornblum | GPL v2 | https://github.com/jessek/hashdeep |
| **Quickhash** | Ted Smith | GPL v3+ | https://www.quickhash-gui.org |
| **Grsync** | Piero Orsoni | GPL v2 | https://www.opbyte.it/grsync |

### Audio & Video

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **ffmpeg** | FFmpeg team | LGPL v2.1+ (GPL v2+ for certain components) | https://ffmpeg.org |
| **WinFF** | WinFF project | GPL v3 | https://www.biggmatt.com/p/winff.html |
| **HandBrake** | HandBrake team | GPL v2 | https://handbrake.fr |
| **Audacity** | Audacity team | GPL v2+ | https://www.audacityteam.org |
| **Audacious** | Audacious project | BSD 2-Clause | https://audacious-media-player.org |
| **Praat** | Paul Boersma / David Weenink | GPL v3+ | https://www.fon.hum.uva.nl/praat |
| **SpeechNote** | Michal Szczepanski | GPL v3+ | https://github.com/mkiol/DiskJockey (SpeechNote) |
| **whisper-ctranslate2** | Guillermo Cámbara | MIT | https://github.com/Softcatala/whisper-ctranslate2 |

### Malware & Threat Analysis

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **ClamAV / ClamTK** | Cisco Talos / ClamAV team | GPL v2+ | https://www.clamav.net |
| **YARA** | VirusTotal / Victor Alvarez | BSD 3-Clause | https://virustotal.github.io/yara |
| **binwalk** | *(listed above)* | | |

### Statistical & Analytical Utilities

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **benfordcheck** | Folkert van Heusden | See note | https://vanheusden.com |
| **RegRipper** | Harlan Carvey | GPL v3 | https://github.com/keydet89/RegRipper3.0 |
| **plaso / log2timeline** | Kristinn Guðjónsson / plaso team | Apache 2.0 | https://github.com/log2timeline/plaso |
| **ent** | John Walker | Public Domain | https://www.fourmilab.ch/random |

### Local AI

| Tool | Author / Maintainer | License | Source / Homepage |
|------|---------------------|---------|-------------------|
| **Ollama** | Ollama, Inc. | MIT | https://github.com/ollama/ollama |
| Bundled LLM weights | Various (see model cards) | Varies per model (Llama, Mistral, Qwen, etc.) | See `/home/fit/.ollama/models` |

---

## Special attribution — benfordcheck

> **benfordcheck v0.2**  
> © 2009 Folkert van Heusden <folkert@vanheusden.com>  
> Homepage: https://vanheusden.com  
>
> benfordcheck analyses numeric data to detect anomalies using Benford's law — a
> first-digit distribution test widely used in forensic accounting and fraud detection.
> The tool is no longer hosted on GitHub; credit and contact via
> [vanheusden.com](https://vanheusden.com). License terms: check the binary's `--help`
> or `LICENSE` file; if source is unavailable, treat as "all rights reserved" and do not
> redistribute modified versions without permission.

---

## Notes on redistribution

1. **GPL source availability:** GPL-licensed tools (GPL v2, v2+, v3) require that
   machine-readable source code be made available to anyone who receives the binary. This
   is satisfied by pointing recipients to each tool's upstream project (links in the table
   above). No additional source-availability obligation falls on this ISO's distributor
   for unmodified tools, provided the upstream links remain valid.

2. **Proprietary tools (FTK Imager, Maltego community):** these are bundled as-is under
   their own end-user terms. Their presence in the ISO does not grant additional
   redistribution rights; users must agree to each vendor's EULA independently. Review
   and confirm redistribution is permitted before publishing the ISO publicly.

3. **Aggregation vs. linking:** this ISO is an **aggregation** — the tools execute
   independently and are not dynamically linked into FreeEed. Aggregation does not trigger
   GPL copyleft obligations on FreeEed's Apache-2.0 code.

4. **LLM model weights:** bundled model weights carry their own licenses (e.g., Llama
   Community License, Mistral Apache 2.0, Qwen license). Review each model's license card
   before redistribution. Most permit free use and redistribution; some restrict commercial
   use or require attribution.

5. **Manifest:** each ISO release should ship a `MANIFEST.txt` (package name, version,
   license) generated at build time so the per-build tool list is auditable.

---

## Maintaining this file

When a tool is added or removed from the ISO, update the table above. For each new entry
record: tool name, author/maintainer, license (SPDX identifier preferred), and upstream
URL. Flag any tool whose license is unclear or whose redistribution terms are ambiguous
before including it in a public release.
