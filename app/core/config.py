import os
from dataclasses import dataclass

from dotenv import load_dotenv


load_dotenv()


@dataclass(frozen=True)
class Settings:
    supabase_url: str
    supabase_key: str
    supabase_db_url: str | None = None


def get_settings() -> Settings:
    supabase_url = os.getenv("SUPABASE_URL")
    supabase_key = os.getenv("SUPABASE_KEY")
    supabase_db_url = os.getenv("SUPABASE_DB_URL")

    if not supabase_url or not supabase_key:
        raise RuntimeError("Missing SUPABASE_URL or SUPABASE_KEY in environment.")

    return Settings(
        supabase_url=supabase_url,
        supabase_key=supabase_key,
        supabase_db_url=supabase_db_url,
    )

