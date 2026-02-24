<h1>FreeEed</h1>

## Quickest Start using one of the ways below
* [Buy support](https://freeeed.org/support/) and [request](https://freeeed.org) a ready-to-run VM.
* [Download the latest release](https://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-10.8.0.zip).  
* [Daily build](https://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-10.8.1-SNAPSHOT.zip).
* [Daily build of Windows Installer](https://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-10.8.1-SNAPSHOT-Windows.exe).
* [Daily build of Linux Installer](https://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-10.8.1-SNAPSHOT-Linux.run).

## Quick Start

1. [Installation instructions](https://github.com/shmsoft/FreeEed/wiki/FreeEed-Installation)
2. Request an unzip key by writing to mark@scaia.ai. Tell us a little about yourself. We would love to hear from you.

## Capabilities

* Works in Windows, Mac, Linux, VirtualBox, and Amazon AWS cloud
* Ability to process over 1,400 file types, including MS Office and PST files ([Tika formats](https://tika.apache.org/) and more)
* OCR
* AI (talk to your eDiscovery documents)
* AI (responsive, privileged, smoking gun - under development)
* Video and audio transcription
* Document review
* "Imaging" - that is, conversion of documents to PDF

## Documentation

Extensive documentation on the [wiki here](https://github.com/markkerzner/FreeEed/wiki)

## How it works

FreeEed is built with AI, ChatGPT, Tika, Lucene, and Solr and embodies best practices in Big Data.

## To build the project

If you are a developer, you can build the project from the source.

Check out the documentation on the [For Developer](for_developers_only.md) page.

## For assistance

713-568-9753  
mark@scaia.ai

## Git Workflow

1. git checkout dev && git pull
1. git checkout -b feature/<short-description>
1. Work, commit, push → git push -u origin feature/<name>
1. Open PR: feature/* → dev
1. When dev is stable, PR: dev → main
1. Tag from main: git tag vX.Y.Z && git push origin vX.Y.Z
