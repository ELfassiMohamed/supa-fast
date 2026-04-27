from __future__ import annotations

import json
import os
from urllib import error, parse, request

from fastapi import FastAPI, HTTPException, Query

# FastAPI application instance.
app = FastAPI()


def _load_dotenv_file(path: str = ".env") -> None:
    # Skip loading when no .env file exists.
    if not os.path.exists(path):
        return

    # Read simple KEY=VALUE lines and inject them into process env.
    with open(path, "r", encoding="utf-8") as env_file:
        for raw_line in env_file:
            line = raw_line.strip()
            # Ignore blank lines, comments, and malformed entries.
            if not line or line.startswith("#") or "=" not in line:
                continue

            key, value = line.split("=", 1)
            key = key.strip()
            value = value.strip().strip("'").strip('"')
            # Preserve already-defined variables (e.g., from system env).
            if key and key not in os.environ:
                os.environ[key] = value


# Load environment variables at startup.
_load_dotenv_file()


@app.get("/")
async def root():
    # Quick pointer to the interactive FastAPI docs.
    return {"message": "Go to /docs"}


@app.get("/products")
def get_products(
    # Pagination controls for Supabase query.
    limit: int = Query(default=100, ge=1, le=1000),
    offset: int = Query(default=0, ge=0),
):
    # Read Supabase credentials from environment.
    supabase_url = os.getenv("SUPABASE_URL")
    supabase_key = os.getenv("SUPABASE_KEY")

    # Fail fast when required credentials are missing.
    if not supabase_url or not supabase_key:
        raise HTTPException(
            status_code=500,
            detail="Missing SUPABASE_URL or SUPABASE_KEY in environment variables.",
        )

    # Build Supabase REST endpoint with query parameters.
    base_url = supabase_url.rstrip("/")
    query_params = parse.urlencode(
        {
            # Limit selected fields to the product schema columns.
            "select": "id,name,description,category,price,currency,availability,created_at,updated_at",
            "limit": limit,
            "offset": offset,
            # Stable ordering for predictable pagination.
            "order": "id.asc",
        }
    )
    endpoint = f"{base_url}/rest/v1/products?{query_params}"

    # Create authenticated HTTP request to Supabase REST API.
    req = request.Request(
        endpoint,
        headers={
            "apikey": supabase_key,
            "Authorization": f"Bearer {supabase_key}",
            "Accept": "application/json",
        },
        method="GET",
    )

    try:
        # Send request and parse JSON payload from response body.
        with request.urlopen(req, timeout=15) as response:
            payload = response.read().decode("utf-8")
            return json.loads(payload)
    except error.HTTPError as exc:
        # Forward Supabase HTTP errors with original response content.
        response_text = exc.read().decode("utf-8", errors="replace")
        raise HTTPException(
            status_code=exc.code,
            detail=f"Supabase HTTP error: {response_text}",
        ) from exc
    except error.URLError as exc:
        # Network/DNS/connectivity failure while contacting Supabase.
        raise HTTPException(
            status_code=502,
            detail=f"Failed to connect to Supabase: {exc.reason}",
        ) from exc
    except json.JSONDecodeError as exc:
        # Supabase returned non-JSON data unexpectedly.
        raise HTTPException(
            status_code=502,
            detail="Received invalid JSON response from Supabase.",
        ) from exc
