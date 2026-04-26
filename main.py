from fastapi import FastAPI

from app.api import api_router


app = FastAPI(
    title="Products API",
    description="FastAPI + Supabase service for product CRUD operations.",
    version="2.0.0",
)
app.include_router(api_router)


@app.get("/", tags=["Health"])
def healthcheck():
    return {"status": "ok", "service": "products-api"}
