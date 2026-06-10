from contextlib import asynccontextmanager
from fastapi import FastAPI, File, UploadFile, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field
import jpype
import jpype.imports
import os
from typing import Any, Dict, Optional, List
import tempfile
import logging
import sys
import subprocess
from pathlib import Path

# Register collectors on startup
from collectors.base.types import BrowseItemType, SelectedItem
from collectors.core.browse_service import (
    BrowseService,
    ConnectorAuthError,
    CredentialsNotConfiguredError,
)
from collectors.core.oauth_manager import OAuthManager
from collectors.core.oauth_routes import create_oauth_router
from collectors.core.orchestrator import Orchestrator
from collectors.core.registry import list_connectors
from collectors.core.selection_store import SelectionStore
from collectors.core.state_manager import StateManager
from collectors.core.token_store import TokenStore
from collectors.core.trigger_manager import TriggerManager

import collectors.box.box_collector  # noqa: F401
import collectors.google_drive.gdrive_collector  # noqa: F401
import collectors.dropbox.dropbox_collector  # noqa: F401

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

_collector_state = StateManager()
_collector_token_store = TokenStore(db_path=_collector_state.db_path)
_collector_selections = SelectionStore(db_path=_collector_state.db_path)
_collector_browse = BrowseService(token_store=_collector_token_store)
_collector_oauth = OAuthManager(token_store=_collector_token_store)
_collector_orchestrator = Orchestrator(state_manager=_collector_state)
_collector_trigger = TriggerManager(
    _collector_state,
    run_callback=_collector_orchestrator.run_collection,
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        _collector_trigger.load_schedules_from_db()
    except Exception as exc:
        logger.warning("Failed to load collector schedules: %s", exc)
    yield
    _collector_trigger.shutdown()


app = FastAPI(
    lifespan=lifespan,
    title="FreeEed Processing API",
    description="""
    API for processing documents using FreeEed. This API provides endpoints to:
    - Process documents using parameter files
    - Process documents using direct configuration
    - Monitor processing status

    ## Key Features
    * Upload parameter files for processing
    * Configure processing directly via JSON
    * Monitor system health
    * Background processing support

    ## Data Locations
    * Input files should be placed in: /data/input
    * Output files will be available in: /data/output
    """,
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

app.include_router(create_oauth_router(_collector_oauth))

_collectors_static = Path(__file__).resolve().parent / "static" / "collectors"
if _collectors_static.is_dir():
    app.mount("/collectors", StaticFiles(directory=str(_collectors_static), html=True), name="collectors-ui")

class ProjectConfig(BaseModel):
    """
    Configuration for document processing
    """
    project_name: str = "default"
    input_path: str = "/data/input"
    output_path: str = "/data/output"
    process_where: str = "local"
    stage: bool = True
    custodians: List[str] = []

    class Config:
        schema_extra = {
            "example": {
                "project_name": "test-project",
                "input_path": "/data/input",
                "output_path": "/data/output",
                "process_where": "local",
                "stage": True,
                "custodians": ["user1", "user2"]
            }
        }

def run_freeeed_process(param_file_path: str):
    """Run FreeEed processing as a subprocess"""
    jar_path = os.getenv('FREEEED_JAR', '/app/freeeed-processing.jar')
    cmd = ["java", "-jar", jar_path, "-param_file", param_file_path]
    try:
        logger.info(f"Running command: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        logger.info(f"Process output: {result.stdout}")
        return True
    except subprocess.CalledProcessError as e:
        logger.error(f"Process error: {e.stderr}")
        raise Exception(f"Processing failed: {e.stderr}")

@app.post("/process/file",
    summary="Process documents using a parameter file",
    description="""
    Upload a parameter file to process documents. The file should be in the format:
    ```
    project-name=test
    input-dir=/data/input
    output-path=/data/output
    process-where=local
    stage=true
    ```
    """)
async def process_with_parameter_file(
    file: UploadFile = File(..., description="Parameter file in the correct format"),
    background_tasks: BackgroundTasks = BackgroundTasks()
):
    """Process documents using a parameter file"""
    logger.info(f"Processing file: {file.filename}")
    try:
        # Create data directories if they don't exist
        os.makedirs("/data/input", exist_ok=True)
        os.makedirs("/data/output", exist_ok=True)

        # Save uploaded file
        param_file_path = f"/data/input/{file.filename}"
        with open(param_file_path, "wb") as f:
            content = await file.read()
            f.write(content)

        # Start processing in background
        background_tasks.add_task(run_freeeed_process, param_file_path)

        return {
            "status": "processing",
            "message": "Processing started in background",
            "file_name": file.filename,
            "output_location": "/data/output"
        }

    except Exception as e:
        logger.error(f"Error during processing: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/process/config",
    summary="Process documents using JSON configuration",
    description="Process documents using a direct JSON configuration instead of a parameter file.")
async def process_with_config(
    config: ProjectConfig,
    background_tasks: BackgroundTasks
):
    """Process documents using a project configuration"""
    logger.info(f"Processing project: {config.project_name}")
    try:
        # Create a parameter file from the config
        param_file_path = f"/data/input/{config.project_name}.project"

        # Write config to parameter file
        with open(param_file_path, "w") as f:
            f.write(f"project-name={config.project_name}\n")
            f.write(f"input-dir={config.input_path}\n")
            f.write(f"output-dir={config.output_path}\n")
            f.write(f"process-where={config.process_where}\n")
            f.write(f"stage={str(config.stage).lower()}\n")
            if config.custodians:
                f.write(f"custodians={','.join(config.custodians)}\n")

        # Start processing in background
        background_tasks.add_task(run_freeeed_process, param_file_path)

        return {
            "status": "processing",
            "message": "Processing started in background",
            "project_name": config.project_name,
            "output_location": config.output_path
        }

    except Exception as e:
        logger.error(f"Error during processing: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/process/project-file",
    summary="Process using the default project file",
    description="Process using the 1.project file from freeeed-processing/test-data/09-projects/1.project")
async def process_project_file(
    background_tasks: BackgroundTasks,
    custom_path: Optional[str] = None
):
    """Process using the default project file or a custom project file path"""
    try:
        # Get repository root (two levels up from api directory)
        repo_root = Path(__file__).resolve().parents[1]

        # Use default project file path or custom path if provided
        default_project_path = Path("/app/test-data/09-projects/1.project")
        project_file_path = Path(custom_path) if custom_path else default_project_path

        if not project_file_path.is_absolute():
            project_file_path = repo_root / project_file_path

        logger.info(f"Using project file: {project_file_path}")

        if not project_file_path.exists():
            raise HTTPException(
                status_code=404,
                detail=f"Project file not found: {project_file_path}"
            )

        # Start processing in background
        background_tasks.add_task(run_freeeed_process, str(project_file_path))

        return {
            "status": "processing",
            "message": "Processing started in background",
            "project_file": str(project_file_path),
            "api_docs": "/docs"  # Link to API documentation
        }

    except Exception as e:
        logger.error(f"Error processing project file: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

class CollectTriggerRequest(BaseModel):
    connector_id: str
    auto_chain: bool = False
    config: Dict[str, Any] = Field(default_factory=dict)


class CollectScheduleRequest(BaseModel):
    schedule_id: str
    connector_id: str
    cron_expression: str
    auto_chain: bool = False
    config: Dict[str, Any] = Field(default_factory=dict)


class ConnectRequest(BaseModel):
    access_token: Optional[str] = None
    refresh_token: Optional[str] = None
    config: Dict[str, Any] = Field(default_factory=dict)


class SelectionItemRequest(BaseModel):
    id: str
    type: str = "file"
    name: str = ""
    path: str = ""


class SaveSelectionsRequest(BaseModel):
    connector_id: str
    items: List[SelectionItemRequest]


class ProjectCollectTriggerRequest(BaseModel):
    connector_id: str
    auto_chain: bool = False
    config: Dict[str, Any] = Field(default_factory=dict)


@app.post("/collect/trigger", summary="Trigger a manual collection job")
async def collect_trigger(request: CollectTriggerRequest):
    """Start a collection job for the given connector."""
    try:
        job = _collector_trigger.trigger_manual(
            connector_id=request.connector_id,
            auto_chain=request.auto_chain,
            config=request.config,
        )
        return {
            "job_id": job.job_id,
            "connector_id": job.connector_id,
            "status": job.status.value,
        }
    except KeyError:
        raise HTTPException(status_code=404, detail=f"Unknown connector: {request.connector_id}")
    except Exception as e:
        logger.error("Collect trigger failed: %s", e)
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/collect/jobs/{job_id}", summary="Get collection job status")
async def collect_job_status(job_id: str):
    job = _collector_state.get_job(job_id)
    if not job:
        raise HTTPException(status_code=404, detail=f"Job not found: {job_id}")
    return {
        "job_id": job.job_id,
        "connector_id": job.connector_id,
        "status": job.status.value,
        "created_at": job.created_at.isoformat() if job.created_at else None,
        "started_at": job.started_at.isoformat() if job.started_at else None,
        "completed_at": job.completed_at.isoformat() if job.completed_at else None,
        "output_dir": job.output_dir,
        "items_collected": job.items_collected,
        "items_failed": job.items_failed,
        "error_message": job.error_message,
        "auto_chain": job.auto_chain,
    }


@app.get("/collect/connectors", summary="List registered connectors")
async def collect_list_connectors():
    return {"connectors": list_connectors()}


def _connector_or_404(connector_id: str) -> None:
    if connector_id not in list_connectors():
        raise HTTPException(status_code=404, detail=f"Unknown connector: {connector_id}")


def _token_override_from_request(request: Optional[ConnectRequest]) -> Optional[Dict[str, Any]]:
    if not request:
        return None
    override: Dict[str, Any] = {}
    if request.access_token:
        override["access_token"] = request.access_token
    if request.refresh_token:
        override["refresh_token"] = request.refresh_token
    return override or None


@app.post(
    "/collect/projects/{project_id}/connect/{connector_id}",
    summary="Initiate or verify connector connection for a project",
)
async def collect_project_connect(
    project_id: str,
    connector_id: str,
    request: Optional[ConnectRequest] = None,
):
    """Verify OAuth/token credentials for a project-scoped connector session."""
    _connector_or_404(connector_id)
    try:
        result = _collector_browse.connect(
            connector_id=connector_id,
            project_id=project_id,
            token_override=_token_override_from_request(request),
            config=request.config if request else None,
        )
        return {"project_id": project_id, **result}
    except CredentialsNotConfiguredError as exc:
        raise HTTPException(status_code=503, detail=str(exc))
    except ConnectorAuthError as exc:
        raise HTTPException(status_code=401, detail=str(exc))
    except KeyError:
        raise HTTPException(status_code=404, detail=f"Unknown connector: {connector_id}")
    except Exception as e:
        logger.error("Project connect failed: %s", e)
        raise HTTPException(status_code=500, detail=str(e))


@app.get(
    "/collect/projects/{project_id}/browse/{connector_id}",
    summary="Browse remote folder children for a project",
)
async def collect_project_browse(
    project_id: str,
    connector_id: str,
    parent_id: Optional[str] = None,
    access_token: Optional[str] = None,
    refresh_token: Optional[str] = None,
):
    """List folders and files at the remote parent (root when parent_id omitted)."""
    _connector_or_404(connector_id)
    token_override = None
    if access_token or refresh_token:
        token_override = {
            k: v
            for k, v in {"access_token": access_token, "refresh_token": refresh_token}.items()
            if v
        }
    try:
        items = _collector_browse.browse(
            connector_id=connector_id,
            parent_id=parent_id,
            project_id=project_id,
            token_override=token_override,
        )
        return {
            "project_id": project_id,
            "connector_id": connector_id,
            "parent_id": parent_id,
            "items": [_collector_browse.serialize_browse_item(item) for item in items],
        }
    except CredentialsNotConfiguredError as exc:
        raise HTTPException(status_code=503, detail=str(exc))
    except ConnectorAuthError as exc:
        raise HTTPException(status_code=401, detail=str(exc))
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    except Exception as e:
        logger.error("Project browse failed: %s", e)
        raise HTTPException(status_code=500, detail=str(e))


@app.post(
    "/collect/projects/{project_id}/selections",
    summary="Save selected remote items for a project",
)
async def collect_project_save_selections(project_id: str, request: SaveSelectionsRequest):
    _connector_or_404(request.connector_id)
    items = [
        SelectedItem(
            id=item.id,
            type=BrowseItemType(item.type),
            name=item.name,
            path=item.path,
        )
        for item in request.items
    ]
    saved = _collector_selections.save_selections(project_id, request.connector_id, items)
    return saved


@app.get(
    "/collect/projects/{project_id}/selections",
    summary="Retrieve saved remote selections for a project",
)
async def collect_project_get_selections(
    project_id: str,
    connector_id: Optional[str] = None,
):
    if connector_id:
        _connector_or_404(connector_id)
    return {
        "project_id": project_id,
        "selections": _collector_selections.get_selections(project_id, connector_id),
    }


@app.post(
    "/collect/projects/{project_id}/trigger",
    summary="Trigger collection for saved project selections only",
)
async def collect_project_trigger(project_id: str, request: ProjectCollectTriggerRequest):
    """Run collection limited to items saved via the selections API."""
    _connector_or_404(request.connector_id)
    selected = _collector_selections.get_selected_items(project_id, request.connector_id)
    if not selected:
        raise HTTPException(
            status_code=400,
            detail=(
                f"No selections saved for project '{project_id}' "
                f"and connector '{request.connector_id}'"
            ),
        )

    config = dict(request.config)
    config["project_id"] = project_id
    config["selected_items"] = [
        {
            "id": item.id,
            "type": item.type.value,
            "name": item.name,
            "path": item.path,
        }
        for item in selected
    ]
    processing = config.setdefault("processing", {})
    processing.setdefault("project_code", project_id)
    if "project_name" not in processing:
        processing["project_name"] = f"collection-{project_id}"

    try:
        job = _collector_trigger.trigger_manual(
            connector_id=request.connector_id,
            auto_chain=request.auto_chain,
            config=config,
        )
        return {
            "project_id": project_id,
            "job_id": job.job_id,
            "connector_id": job.connector_id,
            "status": job.status.value,
            "selection_count": len(selected),
        }
    except KeyError:
        raise HTTPException(status_code=404, detail=f"Unknown connector: {request.connector_id}")
    except Exception as e:
        logger.error("Project collect trigger failed: %s", e)
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/collect/connectors/{connector_id}/health", summary="Connector health check")
async def collect_connector_health(connector_id: str):
    if connector_id not in list_connectors():
        raise HTTPException(status_code=404, detail=f"Unknown connector: {connector_id}")
    return _collector_orchestrator.get_connector_health(connector_id)


@app.post("/collect/schedules", summary="Register a cron collection schedule")
async def collect_create_schedule(request: CollectScheduleRequest):
    try:
        _collector_trigger.add_schedule(
            schedule_id=request.schedule_id,
            connector_id=request.connector_id,
            cron_expression=request.cron_expression,
            auto_chain=request.auto_chain,
            config=request.config,
        )
        return {
            "schedule_id": request.schedule_id,
            "connector_id": request.connector_id,
            "cron_expression": request.cron_expression,
            "status": "registered",
        }
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except KeyError:
        raise HTTPException(status_code=404, detail=f"Unknown connector: {request.connector_id}")


@app.get("/health",
    summary="Check API health status",
    description="Returns the health status of the API and its components.")
async def health_check():
    """Check if the service is running"""
    collector_status = _collector_orchestrator.subsystem_status()
    overall = "healthy"
    if collector_status.get("status") != "healthy":
        overall = "degraded"

    return {
        "status": overall,
        "message": "Service is running",
        "components": {
            "api": "healthy",
            "java": "running" if jpype.isJVMStarted() else "not running",
            "collectors": collector_status,
        }
    }
