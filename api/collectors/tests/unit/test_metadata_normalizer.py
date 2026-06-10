"""Tests for MetadataNormalizer."""

from datetime import datetime

from collectors.ingestion.metadata_normalizer import MetadataNormalizer


def test_normalize_sanitizes_filename():
    normalizer = MetadataNormalizer()
    meta = normalizer.normalize({"id": "1", "name": 'bad<file>.txt'}, "box")
    assert meta.name == "bad_file_.txt"
    assert meta.item_id == "1"
    assert meta.extra["connector_id"] == "box"


def test_content_checksum_stable():
    normalizer = MetadataNormalizer()
    assert normalizer.content_checksum(b"hello") == normalizer.content_checksum(b"hello")
    assert normalizer.content_checksum(b"hello") != normalizer.content_checksum(b"world")


def test_to_storage_record():
    normalizer = MetadataNormalizer()
    meta = normalizer.normalize(
        {"id": "1", "name": "doc.pdf", "modifiedTime": "2024-01-15T10:00:00Z"},
        "google_drive",
    )
    record = normalizer.to_storage_record(meta, "/data/input/collections/job/doc.pdf")
    assert record["local_path"].endswith("doc.pdf")
    assert record["connector_id"] == "google_drive"
