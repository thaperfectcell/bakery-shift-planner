"""Операция «Распределить партии по печам и сменам» (алгоритм 2)."""
import bisect
import heapq
from datetime import timedelta

# Партию выпекают не раньше чем за 7 ч до отправления рейса: продукция
# недлительного хранения должна поступить в торговлю свежей.
MAX_LEAD = timedelta(hours=7)


def find_shift(shifts, shift_starts, moment):
    """Смена, в которую попадает момент времени, или None."""
    i = bisect.bisect_right(shift_starts, moment) - 1
    if i >= 0 and moment < shifts[i]["end"]:
        return shifts[i]
    return None


def schedule_batches(batches, ovens, shifts, max_lead=MAX_LEAD):
    """Алгоритм 2. Жадное распределение партий по печам и сменам.

    Партии берутся по возрастанию срока готовности (при равных сроках -
    сначала более долгие), каждая ставится на печь нужного типа, которая
    освобождается раньше других. При смене группы изделий на печи
    добавляется время перенастройки; выпечка начинается не раньше чем
    за max_lead до срока готовности.
    """
    order = sorted(batches, key=lambda b: (b["deadline"], -b["bake_minutes"],
                                           b["product_id"], b["batch_no"]))
    horizon_start, horizon_end = shifts[0]["start"], shifts[-1]["end"]
    shift_starts = [s["start"] for s in shifts]

    free = {}                      # тип печи -> куча (время освобождения, печь)
    changeover, last_group = {}, {}
    for oven in ovens:
        heapq.heappush(free.setdefault(oven["oven_type"], []),
                       (horizon_start, oven["oven_id"]))
        changeover[oven["oven_id"]] = timedelta(minutes=oven["changeover_min"])
        last_group[oven["oven_id"]] = None

    scheduled, unscheduled = [], []
    for batch in order:
        heap = free.get(batch["oven_type"])
        if not heap:
            unscheduled.append({**batch, "reason": "нет печи нужного типа"})
            continue
        ready, oven_id = heapq.heappop(heap)
        start = ready
        group = last_group[oven_id]
        if group is not None and group != batch["product_group"]:
            start += changeover[oven_id]
        earliest = batch["deadline"] - max_lead
        if start < earliest:
            start = earliest
        end = start + timedelta(minutes=batch["bake_minutes"])
        if end > horizon_end:
            heapq.heappush(heap, (ready, oven_id))
            unscheduled.append({**batch, "reason": "не помещается в смены"})
            continue
        late = end > batch["deadline"]
        shift = find_shift(shifts, shift_starts, start)
        scheduled.append({**batch, "oven_id": oven_id,
                          "shift_id": shift["shift_id"],
                          "start": start, "end": end, "late": late})
        last_group[oven_id] = batch["product_group"]
        heapq.heappush(heap, (end, oven_id))
    return scheduled, unscheduled
