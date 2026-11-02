"""Операция «Скорректировать план выпуска»."""
import math


def batch_deadlines(deliveries, batch_size, batch_count):
    """Срок готовности каждой партии изделия.

    Поставки упорядочиваются по сроку, партии закрывают их по очереди:
    партия k начинается с изделия номер k * batch_size + 1 и должна быть
    готова к сроку той поставки, в которую это изделие попадает.
    """
    ordered = sorted(deliveries)
    deadlines = []
    index, covered = 0, ordered[0][2]
    for k in range(batch_count):
        first_unit = k * batch_size + 1
        while covered < first_unit and index < len(ordered) - 1:
            index += 1
            covered += ordered[index][2]
        deadlines.append(ordered[index][0])
    return deadlines


def build_production_plan(demand, deliveries, products):
    """Округляет потребность до кратности загрузки печи и разбивает выпуск
    на партии со сроками готовности."""
    plan, batches = [], []
    for pid in sorted(demand):
        product = products[pid]
        size = product["batch_size"]
        count = math.ceil(demand[pid] / size)
        plan.append({"product_id": pid, "name": product["name"],
                     "ordered": demand[pid], "planned": count * size,
                     "batch_count": count})
        for k, deadline in enumerate(batch_deadlines(deliveries[pid], size, count), 1):
            batches.append({"product_id": pid, "batch_no": k, "quantity": size,
                            "deadline": deadline, "oven_type": product["oven_type"],
                            "product_group": product["product_group"],
                            "bake_minutes": product["bake_minutes"]})
    return plan, batches
