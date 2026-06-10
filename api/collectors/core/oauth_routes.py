"""FastAPI routes for collector OAuth flows."""

from __future__ import annotations

from typing import Optional

from fastapi import APIRouter, HTTPException, Request
from fastapi.responses import HTMLResponse, JSONResponse, RedirectResponse

from collectors.core.oauth_manager import OAuthError, OAuthManager
from collectors.core.registry import list_connectors


def _wants_json(request: Request, format_param: Optional[str]) -> bool:
    if format_param == "json":
        return True
    accept = request.headers.get("accept", "")
    return "application/json" in accept and "text/html" not in accept


def _success_html(connector_id: str, project_id: str) -> str:
    label = connector_id.replace("_", " ").title()
    return f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Connected to {label}</title>
  <style>
    body {{ font-family: system-ui, sans-serif; max-width: 32rem; margin: 3rem auto; padding: 0 1rem; }}
    h1 {{ font-size: 1.25rem; }}
  </style>
</head>
<body>
  <h1>Connected to {label}</h1>
  <p>OAuth completed for project <strong>{project_id}</strong>. You can close this window.</p>
  <p>Next: <code>POST /collect/projects/{project_id}/connect/{connector_id}</code> or browse the remote tree.</p>
</body>
</html>"""


def _error_html(message: str) -> str:
    return f"""<!DOCTYPE html>
<html lang="en">
<head><meta charset="utf-8"><title>OAuth failed</title></head>
<body><h1>OAuth failed</h1><p>{message}</p></body>
</html>"""


def create_oauth_router(oauth_manager: OAuthManager) -> APIRouter:
    router = APIRouter(tags=["collect-oauth"])

    @router.get("/collect/oauth/{connector_id}/authorize")
    async def oauth_authorize(
        connector_id: str,
        project_id: str,
        redirect_after: Optional[str] = None,
    ):
        if connector_id not in list_connectors():
            raise HTTPException(status_code=404, detail=f"Unknown connector: {connector_id}")
        if not oauth_manager.supports_connector(connector_id):
            raise HTTPException(
                status_code=400,
                detail=f"OAuth not configured for connector: {connector_id}",
            )
        try:
            url = oauth_manager.build_authorize_url(
                connector_id=connector_id,
                project_id=project_id,
                redirect_after=redirect_after,
            )
        except OAuthError as exc:
            raise HTTPException(status_code=503, detail=str(exc)) from exc
        return RedirectResponse(url, status_code=302)

    @router.get("/collect/oauth/{connector_id}/callback")
    async def oauth_callback(
        connector_id: str,
        request: Request,
        code: Optional[str] = None,
        state: Optional[str] = None,
        error: Optional[str] = None,
        error_description: Optional[str] = None,
        format: Optional[str] = None,
    ):
        if connector_id not in list_connectors():
            raise HTTPException(status_code=404, detail=f"Unknown connector: {connector_id}")

        if error:
            message = error_description or error
            if _wants_json(request, format):
                raise HTTPException(status_code=400, detail=message)
            return HTMLResponse(_error_html(message), status_code=400)

        try:
            auth, project_id, redirect_after = oauth_manager.handle_callback(
                connector_id=connector_id,
                code=code or "",
                state=state or "",
            )
        except OAuthError as exc:
            if _wants_json(request, format):
                raise HTTPException(status_code=400, detail=str(exc)) from exc
            return HTMLResponse(_error_html(str(exc)), status_code=400)

        if redirect_after:
            return RedirectResponse(redirect_after, status_code=302)

        payload = {
            "status": "connected",
            "project_id": project_id,
            "connector_id": connector_id,
            "has_refresh_token": bool(auth.refresh_token),
            "scopes": auth.scopes,
        }
        if _wants_json(request, format):
            return JSONResponse(payload)
        return HTMLResponse(_success_html(connector_id, project_id))

    return router
