"""Формирование сменного производственного задания по заказам покупателей.

Процедурная реализация (КТ2): только функции и стандартные структуры данных.
Запуск: python main.py [--data КАТАЛОГ] [--date ГГГГ-ММ-ДД] [--out КАТАЛОГ]
"""
import argparse
import sys
from pathlib import Path

from data_io import load_data, parse_date, write_csv, DATETIME_FORMAT
from documents import (build_requisition, build_shift_tasks, print_orders, print_plan,
                       print_requirements, print_requisition, print_schedule)
from materials import calc_material_requirements, check_stock, flour_moisture
from orders import consolidate_orders
from planning import build_production_plan
from scheduling import schedule_batches

HERE = Path(__file__).resolve().parent


def parse_args(argv):
    parser = argparse.ArgumentParser(
        description="Расчёт плана выпуска, потребности в сырье и сменных заданий хлебозавода")
    parser.add_argument("--data", default=str(HERE / "data"),
                        help="каталог с исходными CSV-файлами (по умолчанию data)")
    parser.add_argument("--date", help="дата отгрузки ГГГГ-ММ-ДД "
                        "(по умолчанию - ближайшая дата в заявках)")
    parser.add_argument("--out", default="out",
                        help="каталог для результатов в CSV (по умолчанию out)")
    return parser.parse_args(argv)


def save_results(out_dir, plan, need, shortages, tasks, requisition):
    """Сохраняет результаты расчёта в CSV-файлы."""
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    write_csv(out / "plan.csv", plan,
              ["product_id", "name", "ordered", "planned", "batch_count"])
    write_csv(out / "material_requirements.csv",
              [{"material_id": m, "need_kg": round(v, 2)} for m, v in sorted(need.items())],
              ["material_id", "need_kg"])
    write_csv(out / "shortages.csv",
              [{**s, "shortage_kg": round(s["shortage_kg"], 2), "need_kg": round(s["need_kg"], 2)}
               for s in shortages],
              ["material_id", "need_kg", "available_kg", "shortage_kg"])
    write_csv(out / "shift_tasks.csv",
              [{"task_id": t["task_id"], "shift_id": t["shift_id"], "status": t["status"]}
               for t in tasks],
              ["task_id", "shift_id", "status"])
    batches = [{**b, "start": b["start"].strftime(DATETIME_FORMAT),
                "end": b["end"].strftime(DATETIME_FORMAT)}
               for t in tasks for b in t["batches"]]
    write_csv(out / "batches.csv", batches,
              ["batch_id", "task_id", "product_id", "oven_id", "quantity", "start", "end"])
    write_csv(out / "requisition.csv", requisition, ["material_id", "name", "unit", "quantity"])


def main(argv=None):
    args = parse_args(sys.argv[1:] if argv is None else argv)
    data = load_data(args.data)
    ship_date = parse_date(args.date) if args.date else min(o["ship_date"] for o in data["orders"])

    summary = consolidate_orders(data["orders"], data["order_items"], data["products"],
                                 data["points"], ship_date)
    print_orders(summary, ship_date)
    if not summary["demand"]:
        print("Нет принятых заявок на эту дату.")
        return 1

    plan, batches = build_production_plan(summary["demand"], summary["deliveries"],
                                          data["products"])
    print_plan(plan)

    moisture = flour_moisture(data["stock"], data["materials"])
    need = calc_material_requirements(plan, data["products"], data["recipes"],
                                      data["components"], data["materials"], moisture)
    shortages = check_stock(need, data["stock"])
    print_requirements(need, data["stock"], data["materials"], shortages)

    scheduled, unscheduled = schedule_batches(batches, data["ovens"], data["shifts"])
    tasks = build_shift_tasks(scheduled, data["shifts"])
    print_schedule(tasks, unscheduled, data["ovens"])

    requisition = build_requisition(need, data["materials"])
    print_requisition(requisition)

    save_results(args.out, plan, need, shortages, tasks, requisition)
    print(f"\nРезультаты сохранены в каталог {args.out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
