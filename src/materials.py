"""Операции «Рассчитать потребность в сырье» (алгоритм 1)
и «Сверить потребность с остатками склада»."""

BASE_FLOUR_MOISTURE = 14.5   # базисная влажность муки в рецептурах, %


def flour_coefficient(actual_moisture):
    """Коэффициент пересчёта массы муки с базисной влажности на фактическую:
    масса сухих веществ муки в тесте должна остаться прежней."""
    return (100 - BASE_FLOUR_MOISTURE) / (100 - actual_moisture)


def expand_recipe(recipe_id, flour_kg, recipes, components, materials,
                  moisture, need, depth):
    """Рекурсивно раскрывает рецептуру на flour_kg кг муки и добавляет
    расход сырья в словарь need {сырьё: кг}.

    Количество компонента задано в кг на 100 кг муки рецептуры. Если
    компонент - полуфабрикат, по его выходу находится количество муки
    в нём, и его рецептура раскрывается тем же способом.
    """
    if depth > len(recipes):
        raise ValueError(f"циклическая ссылка в рецептуре {recipe_id}")
    for comp in components[recipe_id]:
        amount = comp["amount_kg"] * flour_kg / 100
        if comp["semi_recipe_id"]:
            semi = recipes[comp["semi_recipe_id"]]
            semi_flour = amount / semi["yield_kg"] * 100
            expand_recipe(semi["recipe_id"], semi_flour, recipes, components,
                          materials, moisture, need, depth + 1)
        else:
            mid = comp["material_id"]
            if materials[mid]["is_flour"]:
                w = moisture.get(mid, BASE_FLOUR_MOISTURE)
                amount *= flour_coefficient(w)
            need[mid] = need.get(mid, 0.0) + amount


def calc_material_requirements(plan, products, recipes, components,
                               materials, moisture):
    """Алгоритм 1. Потребность в сырье на план выпуска {сырьё: кг}."""
    need = {}
    for position in plan:
        product = products[position["product_id"]]
        recipe = recipes[product["recipe_id"]]
        product_kg = position["planned"] * product["piece_mass_kg"]
        flour_kg = product_kg / recipe["yield_kg"] * 100
        expand_recipe(recipe["recipe_id"], flour_kg, recipes, components,
                      materials, moisture, need, 0)
    return need


def flour_moisture(stock, materials):
    """Фактическая влажность муки по данным лабораторного контроля."""
    return {mid: row["moisture_pct"] for mid, row in stock.items()
            if materials[mid]["is_flour"] and row["moisture_pct"] is not None}


def check_stock(need, stock):
    """Сверяет потребность с остатками и возвращает перечень нехватки."""
    shortages = []
    for mid in sorted(need):
        available = stock[mid]["quantity_kg"] if mid in stock else 0.0
        if need[mid] > available:
            shortages.append({"material_id": mid, "need_kg": need[mid],
                              "available_kg": available,
                              "shortage_kg": need[mid] - available})
    return shortages
