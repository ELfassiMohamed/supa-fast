from datetime import datetime

from pydantic import BaseModel, Field


class ProductCreate(BaseModel):
    name: str = Field(..., min_length=1)
    description: str = Field(default="")
    category: str = Field(..., min_length=1)
    price: float = Field(..., ge=0)
    currency: str = Field(..., min_length=1, max_length=10)
    availability: str = Field(..., min_length=1, max_length=30)


class ProductUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=1)
    description: str | None = None
    category: str | None = Field(default=None, min_length=1)
    price: float | None = Field(default=None, ge=0)
    currency: str | None = Field(default=None, min_length=1, max_length=10)
    availability: str | None = Field(default=None, min_length=1, max_length=30)


class Product(BaseModel):
    id: int
    name: str
    description: str | None = None
    category: str
    price: float
    currency: str
    availability: str
    created_at: datetime
    updated_at: datetime


class ProductListResponse(BaseModel):
    products: list[Product]
    count: int


class ProductResponse(BaseModel):
    product: Product


class ProductDeleteResponse(BaseModel):
    deleted_id: int

