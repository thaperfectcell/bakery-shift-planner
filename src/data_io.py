"""Чтение исходных данных из CSV и запись результатов.

Каждый CSV-файл соответствует одной сущности логической модели данных.
Сущность хранится как словарь (dict), набор сущностей - как список
или словарь по первичному ключу. Собственные классы не используются.
"""
import csv
from datetime import datetime
from pathlib import Path

DATETIME_FORMAT = "%Y-%m-%d %H:%M"
DATE_FORMAT = "%Y-%m-%d"
CSV_DELIMITER = ";"


def read_csv(path):
    """Читает CSV-файл и возвращает список строк-словарей."""
    with open(path, encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f, delimiter=CSV_DELIMITER))


def write_csv(path, rows, fields):
    """Записывает список словарей в CSV-файл с заданным порядком столбцов."""
    with open(path, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fields, delimiter=CSV_DELIMITER,
                                extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)


def parse_datetime(text):
    return datetime.strptime(text, DATETIME_FORMAT)


def parse_date(text):
    return datetime.strptime(text, DATE_FORMAT).date()


def index_by(rows, key):
    """Строит словарь {значение первичного ключа: сущность}."""
    return {row[key]: row for row in rows}


def group_by(rows, key):
    """Группирует строки по значению поля: {значение: [строки]}."""
    groups = {}
    for row in rows:
        groups.setdefault(row[key], []).append(row)
    return groups


def convert_products(rows):
    for row in rows:
        row["piece_mass_kg"] = float(row["piece_mass_kg"])
        row["batch_size"] = int(row["batch_size"])
        row["bake_minutes"] = int(row["bake_minutes"])
    return rows


def convert_recipes(rows):
    for row in rows:
        row["yield_kg"] = float(row["yield_kg"])
    return rows


def convert_components(rows):
    for row in rows:
        row["line_no"] = int(row["line_no"])
        row["amount_kg"] = float(row["amount_kg"])
    return rows


def convert_materials(rows):
    for row in rows:
        row["is_flour"] = row["is_flour"] == "1"
    return rows


def convert_stock(rows):
    for row in rows:
        row["stock_date"] = parse_date(row["stock_date"])
        row["quantity_kg"] = float(row["quantity_kg"])
        row["moisture_pct"] = float(row["moisture_pct"]) if row["moisture_pct"] else None
    return rows


def latest_stock(rows):
    """Оставляет по каждому виду сырья самую позднюю запись об остатке."""
    latest = {}
    for row in rows:
        current = latest.get(row["material_id"])
        if current is None or row["stock_date"] > current["stock_date"]:
            latest[row["material_id"]] = row
    return latest


def convert_orders(rows):
    for row in rows:
        row["ship_date"] = parse_date(row["ship_date"])
        row["received_at"] = parse_datetime(row["received_at"])
    return rows


def convert_order_items(rows):
    for row in rows:
        row["quantity"] = int(row["quantity"])
    return rows


def convert_ovens(rows):
    for row in rows:
        row["changeover_min"] = int(row["changeover_min"])
    return rows


def convert_shifts(rows):
    for row in rows:
        row["start"] = parse_datetime(row["start"])
        row["end"] = parse_datetime(row["end"])
    return rows


def load_data(data_dir):
    """Загружает все сущности логической модели из каталога с CSV-файлами."""
    d = Path(data_dir)
    components = convert_components(read_csv(d / "recipe_components.csv"))
    components.sort(key=lambda c: (c["recipe_id"], c["line_no"]))
    return {
        "customers": index_by(read_csv(d / "customers.csv"), "customer_id"),
        "points": index_by(read_csv(d / "delivery_points.csv"), "point_id"),
        "orders": convert_orders(read_csv(d / "orders.csv")),
        "order_items": convert_order_items(read_csv(d / "order_items.csv")),
        "products": index_by(convert_products(read_csv(d / "products.csv")), "product_id"),
        "recipes": index_by(convert_recipes(read_csv(d / "recipes.csv")), "recipe_id"),
        "components": group_by(components, "recipe_id"),
        "materials": index_by(convert_materials(read_csv(d / "materials.csv")), "material_id"),
        "stock": latest_stock(convert_stock(read_csv(d / "stock.csv"))),
        "ovens": convert_ovens(read_csv(d / "ovens.csv")),
        "shifts": sorted(convert_shifts(read_csv(d / "shifts.csv")), key=lambda s: s["start"]),
    }
