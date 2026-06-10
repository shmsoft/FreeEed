"""Mock connector HTTP behavior tests."""

from datetime import datetime
from unittest.mock import MagicMock, patch

import pytest

from collectors.base.auth_context import AuthContext
from collectors.box.box_auth import BoxAuth
from collectors.google_drive.gdrive_auth import GoogleDriveAuth
from collectors.dropbox.dropbox_auth import DropboxAuth


def test_box_token_refresh_mock():
    auth = AuthContext(connector_id="box", refresh_token="r1")
    client = BoxAuth(auth, config={"mock_refresh": True})
    refreshed = client.refresh_token()
    assert refreshed.access_token == "mock-box-token"


@patch("collectors.box.box_auth.requests.post")
def test_box_token_refresh_http(mock_post):
    mock_post.return_value = MagicMock(
        status_code=200,
        json=lambda: {"access_token": "new-token", "expires_in": 3600},
    )
    auth = AuthContext(connector_id="box", refresh_token="r1")
    client = BoxAuth(auth, config={"client_id": "id", "client_secret": "secret"})
    refreshed = client.refresh_token()
    assert refreshed.access_token == "new-token"


def test_gdrive_token_refresh_mock():
    auth = AuthContext(connector_id="google_drive", refresh_token="r1")
    client = GoogleDriveAuth(auth, config={"mock_refresh": True})
    refreshed = client.refresh_token()
    assert refreshed.access_token == "mock-gdrive-token"


def test_dropbox_token_refresh_mock():
    auth = AuthContext(connector_id="dropbox", refresh_token="r1")
    client = DropboxAuth(auth, config={"mock_refresh": True})
    refreshed = client.refresh_token()
    assert refreshed.access_token == "mock-dropbox-token"
