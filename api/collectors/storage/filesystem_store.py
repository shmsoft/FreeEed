"""Filesystem storage for collected items."""

from __future__ import annotations

import os
from pathlib import Path

from collectors.base.types import ItemMetadata


class FilesystemStore:
    """Write collected files to a job-specific directory."""

    def __init__(self, base_dir: str):
        self.base_dir = Path(base_dir)
        self.base_dir.mkdir(parents=True, exist_ok=True)

    def write_item(self, metadata: ItemMetadata, content: bytes) -> str:
        safe_name = metadata.name
        target = self.base_dir / safe_name
        if target.exists() and metadata.checksum:
            stem = target.stem
            suffix = target.suffix
            target = self.base_dir / f"{stem}_{metadata.item_id[:8]}{suffix}"

        target.parent.mkdir(parents=True, exist_ok=True)
        temp_path = target.with_suffix(target.suffix + ".tmp")
        with open(temp_path, "wb") as fh:
            fh.write(content)
        os.replace(temp_path, target)
        return str(target)
