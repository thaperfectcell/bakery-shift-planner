"""Операция «Сформировать сменные задания и требование-накладную»
и вывод результатов в консоль."""


def build_shift_tasks(scheduled, shifts):
    """Группирует партии по сменам: одно сменное задание на смену."""
    tasks = []
    for shift in shifts:
        in_shift = sorted((b for b in scheduled if b["shift_id"] == shift["shift_id"]),
                          key=lambda b: (b["start"], b["oven_id"]))
        if not in_shift:
            continue
        task_id = "СЗ-" + shift["shift_id"]
        batches = [{**b, "batch_id": f"{task_id}-{n:02d}", "task_id": task_id}
                   for n, b in enumerate(in_shift, 1)]
        tasks.append({"task_id": task_id, "shift_id": shift["shift_id"],
                      "start": shift["start"], "end": shift["end"],
                      "status": "проект", "batches": batches})
    return tasks


def build_requisition(need, materials):
    """Строки требования-накладной на отпуск сырья в производство."""
    return [{"material_id": mid, "name": materials[mid]["name"],
             "unit": materials[mid]["unit"], "quantity": round(need[mid], 1)}
            for mid in sorted(need)]


def fmt_time(moment):
    return moment.strftime("%d.%m %H:%M")


def fmt_kg(value):
    return f"{value:,.1f}".replace(",", " ").replace(".", ",")


def print_table(title, header, rows):
    """Печатает таблицу с выравниванием столбцов по ширине."""
    widths = [max(len(str(x)) for x in col) for col in zip(header, *rows)]
    line = "  ".join("-" * w for w in widths)
    print(f"\n{title}")
    print("  ".join(str(h).ljust(w) for h, w in zip(header, widths)).rstrip())
    print(line)
    for row in rows:
        print("  ".join(str(x).ljust(w) for x, w in zip(row, widths)).rstrip())


def print_orders(summary, ship_date):
    print(f"СВОДКА ЗАЯВОК НА {ship_date:%d.%m.%Y}")
    print(f"Принято заявок: {len(summary['accepted'])}")
    for d in summary["duplicates"]:
        print(f"Повтор: заявка {d['order_id']} совпадает с {d['duplicate_of']} - пропущена")
    for r in summary["rejected"]:
        print(f"Отклонена: заявка {r['order_id']} - {r['reason']}")


def print_plan(plan):
    rows = [(p["product_id"], p["name"], p["ordered"], p["planned"], p["batch_count"])
            for p in plan]
    print_table("ПЛАН ВЫПУСКА", ("Код", "Изделие", "Заказано, шт", "План, шт", "Партий"), rows)


def print_requirements(need, stock, materials, shortages):
    rows = [(mid, materials[mid]["name"], fmt_kg(need[mid]),
             fmt_kg(stock[mid]["quantity_kg"] if mid in stock else 0.0))
            for mid in sorted(need)]
    print_table("ПОТРЕБНОСТЬ В СЫРЬЕ", ("Код", "Сырьё", "Нужно, кг", "Остаток, кг"), rows)
    if not shortages:
        print("Сырья достаточно.")
        return
    print("Сырья недостаточно - требуется решение начальника производства:")
    for s in shortages:
        print(f"  {materials[s['material_id']]['name']}: не хватает {fmt_kg(s['shortage_kg'])} кг")


def print_schedule(tasks, unscheduled, ovens):
    oven_names = {o["oven_id"]: o["name"] for o in ovens}
    for task in tasks:
        rows = [(b["batch_id"], oven_names[b["oven_id"]], b["product_id"], b["quantity"],
                 fmt_time(b["start"]), fmt_time(b["end"]), fmt_time(b["deadline"]),
                 "опоздание" if b["late"] else "")
                for b in task["batches"]]
        title = (f"СМЕННОЕ ЗАДАНИЕ {task['task_id']} "
                 f"({fmt_time(task['start'])} - {fmt_time(task['end'])}), статус: {task['status']}")
        print_table(title, ("Партия", "Печь", "Изделие", "Шт", "Начало", "Окончание",
                            "Срок", "Отметка"), rows)
    late = sum(b["late"] for t in tasks for b in t["batches"])
    print(f"\nПартий с опозданием: {late}")
    for b in unscheduled:
        print(f"Не распределена партия {b['product_id']} № {b['batch_no']}: {b['reason']}")


def print_requisition(requisition):
    rows = [(r["material_id"], r["name"], r["unit"], fmt_kg(r["quantity"]))
            for r in requisition]
    print_table("ТРЕБОВАНИЕ-НАКЛАДНАЯ НА ОТПУСК СЫРЬЯ", ("Код", "Сырьё", "Ед.", "Количество"), rows)
