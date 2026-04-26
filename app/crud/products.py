from typing import Any

from supabase import Client


TABLE_NAME = "products"
PRODUCT_COLUMNS = (
    "id, name, description, category, price, currency, availability, created_at, updated_at"
)


def list_products(
    client: Client,
    *,
    limit: int,
    offset: int,
    category: str | None = None,
    availability: str | None = None,
) -> tuple[list[dict[str, Any]], int]:
    query = (
        client.table(TABLE_NAME)
        .select(PRODUCT_COLUMNS, count="exact")
        .order("id", desc=False)
        .range(offset, offset + limit - 1)
    )

    if category:
        query = query.eq("category", category)
    if availability:
        query = query.eq("availability", availability)

    response = query.execute()
    items = response.data or []
    total = response.count or 0
    return items, total


def get_product_by_id(client: Client, product_id: int) -> dict[str, Any] | None:
    response = (
        client.table(TABLE_NAME)
        .select(PRODUCT_COLUMNS)
        .eq("id", product_id)
        .limit(1)
        .execute()
    )
    items = response.data or []
    if not items:
        return None
    return items[0]


def create_product(client: Client, payload: dict[str, Any]) -> dict[str, Any]:
    response = client.table(TABLE_NAME).insert(payload).execute()
    created_items = response.data or []
    return created_items[0]


def update_product(
    client: Client,
    *,
    product_id: int,
    payload: dict[str, Any],
) -> dict[str, Any] | None:
    response = (
        client.table(TABLE_NAME)
        .update(payload)
        .eq("id", product_id)
        .select(PRODUCT_COLUMNS)
        .execute()
    )
    items = response.data or []
    if not items:
        return None
    return items[0]


def delete_product(client: Client, product_id: int) -> bool:
    response = (
        client.table(TABLE_NAME)
        .delete()
        .eq("id", product_id)
        .select("id")
        .execute()
    )
    return bool(response.data)

