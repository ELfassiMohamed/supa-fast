from fastapi import APIRouter, Depends, HTTPException, Path, Query, status
from supabase import Client

from app.crud.products import (
    create_product,
    delete_product,
    get_product_by_id,
    list_products,
    update_product,
)
from app.db.supabase_client import get_supabase_client
from app.schemas.products import (
    ProductCreate,
    ProductDeleteResponse,
    ProductListResponse,
    ProductResponse,
    ProductUpdate,
)


router = APIRouter(prefix="/products", tags=["Products"])


def get_db_client() -> Client:
    return get_supabase_client()


@router.get(
    "",
    response_model=ProductListResponse,
    summary="List products",
    description="Returns products with optional category and availability filters.",
)
def read_products(
    limit: int = Query(default=100, ge=1, le=1000),
    offset: int = Query(default=0, ge=0),
    category: str | None = Query(default=None),
    availability: str | None = Query(default=None),
    client: Client = Depends(get_db_client),
):
    items, total = list_products(
        client,
        limit=limit,
        offset=offset,
        category=category,
        availability=availability,
    )
    return {"products": items, "count": total}


@router.get(
    "/{product_id}",
    response_model=ProductResponse,
    summary="Get one product",
)
def read_product(
    product_id: int = Path(..., ge=1),
    client: Client = Depends(get_db_client),
):
    item = get_product_by_id(client, product_id)
    if not item:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found.")
    return {"product": item}


@router.post(
    "",
    response_model=ProductResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create product",
)
def create_product_endpoint(
    payload: ProductCreate,
    client: Client = Depends(get_db_client),
):
    created = create_product(client, payload.model_dump())
    return {"product": created}


@router.put(
    "/{product_id}",
    response_model=ProductResponse,
    summary="Update product",
)
def update_product_endpoint(
    payload: ProductUpdate,
    product_id: int = Path(..., ge=1),
    client: Client = Depends(get_db_client),
):
    update_payload = payload.model_dump(exclude_none=True)
    if not update_payload:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="At least one field must be provided for update.",
        )

    updated = update_product(client, product_id=product_id, payload=update_payload)
    if not updated:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found.")
    return {"product": updated}


@router.delete(
    "/{product_id}",
    response_model=ProductDeleteResponse,
    summary="Delete product",
)
def delete_product_endpoint(
    product_id: int = Path(..., ge=1),
    client: Client = Depends(get_db_client),
):
    deleted = delete_product(client, product_id)
    if not deleted:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found.")
    return {"deleted_id": product_id}

