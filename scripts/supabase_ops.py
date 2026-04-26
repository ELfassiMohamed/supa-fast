import argparse
import csv
import sys
from pathlib import Path

import psycopg
from psycopg import Connection

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from app.core.config import get_settings


DEFAULT_CSV_PATH = "products-100000.csv"
VALID_AVAILABILITY = ("in_stock", "out_of_stock", "discontinued")


CREATE_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    currency TEXT NOT NULL,
    availability TEXT NOT NULL CHECK (availability IN ('in_stock', 'out_of_stock', 'discontinued')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_products_category ON products (category);
CREATE INDEX IF NOT EXISTS idx_products_availability ON products (availability);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_products_updated_at ON products;
CREATE TRIGGER trg_products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
"""


def get_db_connection() -> Connection:
    settings = get_settings()
    if not settings.supabase_db_url:
        raise RuntimeError(
            "Missing SUPABASE_DB_URL in environment. "
            "Use the Supabase Postgres connection string for table creation/import."
        )
    return psycopg.connect(settings.supabase_db_url)


def create_products_table(conn: Connection) -> None:
    with conn.cursor() as cursor:
        cursor.execute(CREATE_TABLE_SQL)
    conn.commit()


def import_products_csv(
    conn: Connection,
    csv_path: Path,
    *,
    batch_size: int = 2000,
    truncate_first: bool = False,
) -> int:
    if not csv_path.exists():
        raise FileNotFoundError(f"CSV file not found: {csv_path}")

    inserted = 0
    with conn.cursor() as cursor:
        if truncate_first:
            cursor.execute("TRUNCATE TABLE products RESTART IDENTITY;")

        with csv_path.open("r", encoding="utf-8", newline="") as csv_file:
            reader = csv.DictReader(csv_file)
            pending_rows: list[tuple[str, str, str, float, str, str]] = []

            for row in reader:
                availability = (row.get("Availability") or "").strip().lower()
                if availability not in VALID_AVAILABILITY:
                    continue

                try:
                    price = float((row.get("Price") or "0").strip())
                except ValueError:
                    continue

                pending_rows.append(
                    (
                        (row.get("Name") or "").strip(),
                        (row.get("Description") or "").strip(),
                        (row.get("Category") or "").strip(),
                        price,
                        (row.get("Currency") or "").strip().upper(),
                        availability,
                    )
                )

                if len(pending_rows) >= batch_size:
                    cursor.executemany(
                        """
                        INSERT INTO products (name, description, category, price, currency, availability)
                        VALUES (%s, %s, %s, %s, %s, %s);
                        """,
                        pending_rows,
                    )
                    inserted += len(pending_rows)
                    pending_rows.clear()

            if pending_rows:
                cursor.executemany(
                    """
                    INSERT INTO products (name, description, category, price, currency, availability)
                    VALUES (%s, %s, %s, %s, %s, %s);
                    """,
                    pending_rows,
                )
                inserted += len(pending_rows)

    conn.commit()
    return inserted


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Supabase setup and product CSV import utility.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    subparsers.add_parser("create-table", help="Create products table and indexes.")

    import_parser = subparsers.add_parser("import-csv", help="Import products CSV into database.")
    import_parser.add_argument("--csv-path", default=DEFAULT_CSV_PATH, help="Path to products CSV file.")
    import_parser.add_argument("--batch-size", type=int, default=2000, help="Rows per insert batch.")
    import_parser.add_argument(
        "--truncate",
        action="store_true",
        help="Truncate products table before import.",
    )

    setup_parser = subparsers.add_parser("setup", help="Create table then import CSV.")
    setup_parser.add_argument("--csv-path", default=DEFAULT_CSV_PATH, help="Path to products CSV file.")
    setup_parser.add_argument("--batch-size", type=int, default=2000, help="Rows per insert batch.")
    setup_parser.add_argument(
        "--truncate",
        action="store_true",
        help="Truncate products table before import.",
    )

    return parser


def main() -> None:
    parser = build_parser()
    args = parser.parse_args()

    with get_db_connection() as conn:
        if args.command == "create-table":
            create_products_table(conn)
            print("Products table is ready.")
            return

        if args.command == "import-csv":
            rows = import_products_csv(
                conn,
                Path(args.csv_path),
                batch_size=args.batch_size,
                truncate_first=args.truncate,
            )
            print(f"Imported {rows} products.")
            return

        if args.command == "setup":
            create_products_table(conn)
            rows = import_products_csv(
                conn,
                Path(args.csv_path),
                batch_size=args.batch_size,
                truncate_first=args.truncate,
            )
            print(f"Setup complete. Imported {rows} products.")
            return


if __name__ == "__main__":
    main()
