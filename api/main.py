from fastapi import FastAPI, File, UploadFile, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import jpype
import jpype.imports
import os
from typing import Optional, List
import tempfile
import logging
import sys
import subprocess
from pathlib import Path

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

app = FastAPI(
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
    cmd = ["java", "-jar", "/app/freeeed-processing.jar", "-param_file", param_file_path]
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        logger.info(f"Process output: {result.stdout}")
        return True
    except subprocess.CalledProcessError as e:
        logger.error(f"Process error: {e.stderr}")
        return False

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

@app.get("/health",
    summary="Check API health status",
    description="Returns the health status of the API and its components.")
async def health_check():
    """Check if the service is running"""
    return {
        "status": "healthy",
        "message": "Service is running",
        "components": {
            "api": "healthy",
            "java": "running" if jpype.isJVMStarted() else "not running"
        }
    }
