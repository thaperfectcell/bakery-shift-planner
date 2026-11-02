"""Операция «Свести заявки по изделиям»."""
from datetime import datetime


def order_deadline(order, points):
    """Срок готовности продукции по заявке: дата отгрузки и время отправления
    рейса, который обслуживает точку доставки."""
    hours, minutes = map(int, points[order["point_id"]]["ship_time"].split(":"))
    return datetime(order["ship_date"].year, order["ship_date"].month,
                    order["ship_date"].day, hours, minutes)


def check_order(order, items, products, points):
    """Проверяет заявку и возвращает список найденных ошибок."""
    errors = []
    if order["point_id"] not in points:
        errors.append(f"неизвестная точка доставки {order['point_id']}")
    if not items:
        errors.append("в заявке нет позиций")
    for item in items:
        if item["product_id"] not in products:
            errors.append(f"неизвестное изделие {item['product_id']}")
        elif item["quantity"] <= 0:
            errors.append(f"неположительное количество изделия {item['product_id']}")
    return errors


def consolidate_orders(orders, order_items, products, points, ship_date):
    """Сводит заявки на дату отгрузки в потребность по изделиям.

    Заявки обрабатываются в порядке поступления. Заявка с ошибками
    отклоняется, повтор уже принятой заявки (та же точка доставки и тот же
    состав) пропускается. Для каждого изделия запоминаются поставки
    (срок готовности, заявка, количество) - по ним строятся сроки партий.
    """
    items_by_order = {}
    for item in order_items:
        items_by_order.setdefault(item["order_id"], []).append(item)

    demand = {}          # изделие -> суммарное количество, шт
    deliveries = {}      # изделие -> [(срок готовности, заявка, количество)]
    accepted, duplicates, rejected = [], [], []
    seen = {}            # ключ заявки -> номер первой такой заявки

    day_orders = [o for o in orders if o["ship_date"] == ship_date]
    for order in sorted(day_orders, key=lambda o: o["received_at"]):
        items = items_by_order.get(order["order_id"], [])
        errors = check_order(order, items, products, points)
        if errors:
            rejected.append({"order_id": order["order_id"], "reason": "; ".join(errors)})
            continue
        key = (order["point_id"],
               tuple(sorted((i["product_id"], i["quantity"]) for i in items)))
        if key in seen:
            duplicates.append({"order_id": order["order_id"], "duplicate_of": seen[key]})
            continue
        seen[key] = order["order_id"]
        accepted.append(order["order_id"])
        deadline = order_deadline(order, points)
        for item in items:
            pid = item["product_id"]
            demand[pid] = demand.get(pid, 0) + item["quantity"]
            deliveries.setdefault(pid, []).append((deadline, order["order_id"], item["quantity"]))

    return {"demand": demand, "deliveries": deliveries, "accepted": accepted,
            "duplicates": duplicates, "rejected": rejected}
