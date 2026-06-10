"""Ingestion pipeline components."""

__all__ = ["IngestionPipeline", "MetadataNormalizer", "StreamingEngine"]


def __getattr__(name: str):
    if name == "IngestionPipeline":
        from .ingestion_pipeline import IngestionPipeline
        return IngestionPipeline
    if name == "MetadataNormalizer":
        from .metadata_normalizer import MetadataNormalizer
        return MetadataNormalizer
    if name == "StreamingEngine":
        from .streaming_engine import StreamingEngine
        return StreamingEngine
    raise AttributeError(f"module {__name__!r} has no attribute {name!r}")
