#!/usr/bin/env python3
"""Generate realistic commit timestamps spread across a date range."""

import argparse
import csv
import random
import sys
from datetime import date, datetime, time, timedelta


def parse_date(s: str) -> date:
    return datetime.strptime(s, "%Y-%m-%d").date()


def is_weekday(d: date) -> bool:
    return d.weekday() < 6  # Mon-Sat; skip Sunday only


def random_work_time(rng: random.Random) -> time:
    hour = rng.randint(9, 20)
    minute = rng.choice([0, 5, 10, 15, 20, 30, 35, 40, 45, 50, 55])
    return time(hour, minute, rng.randint(0, 59))


def collect_days(start: date, end: date, skip_sunday: bool) -> list[date]:
    days = []
    cur = start
    while cur <= end:
        if not skip_sunday or is_weekday(cur):
            days.append(cur)
        cur += timedelta(days=1)
    return days


def distribute(count: int, start: date, end: date, seed: int, skip_sunday: bool) -> list[str]:
    rng = random.Random(seed)
    days = collect_days(start, end, skip_sunday)
    if not days:
        raise SystemExit("No valid days in range.")

    # Weight later days slightly for sprint-like clustering
    weights = [1.0 + (i / max(len(days) - 1, 1)) * 0.5 for i in range(len(days))]
    dates_out: list[datetime] = []
    prev: datetime | None = None

    for _ in range(count):
        day = rng.choices(days, weights=weights, k=1)[0]
        dt = datetime.combine(day, random_work_time(rng))
        if prev is not None and dt <= prev:
            dt = prev + timedelta(minutes=rng.randint(15, 120))
        dates_out.append(dt)
        prev = dt

    dates_out.sort()
    return [dt.strftime("%Y-%m-%dT%H:%M:%S+0700") for dt in dates_out]


def main() -> None:
    parser = argparse.ArgumentParser(description="Distribute commit dates across a range.")
    parser.add_argument("--count", type=int, help="Number of commits (if no --hashes)")
    parser.add_argument("--hashes", help="File with one commit hash per line")
    parser.add_argument("--start", required=True, help="Start date YYYY-MM-DD")
    parser.add_argument("--end", required=True, help="End date YYYY-MM-DD")
    parser.add_argument("--output", default="dates.csv", help="Output CSV path")
    parser.add_argument("--seed", type=int, default=42, help="RNG seed for reproducibility")
    parser.add_argument("--include-sunday", action="store_true", help="Allow Sundays")
    args = parser.parse_args()

    start = parse_date(args.start)
    end = parse_date(args.end)
    if end < start:
        raise SystemExit("end must be >= start")

    hashes: list[str] = []
    if args.hashes:
        with open(args.hashes, encoding="utf-8") as f:
            hashes = [line.strip() for line in f if line.strip()]
        count = len(hashes)
    elif args.count:
        count = args.count
        hashes = [f"COMMIT_{i + 1}" for i in range(count)]
    else:
        raise SystemExit("Provide --count or --hashes")

    skip_sunday = not args.include_sunday
    timestamps = distribute(count, start, end, args.seed, skip_sunday)

    with open(args.output, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["hash", "author_date", "committer_date"])
        for h, ts in zip(hashes, timestamps):
            writer.writerow([h, ts, ts])

    print(f"Wrote {count} rows to {args.output}", file=sys.stderr)
    print(f"Span: {timestamps[0]} .. {timestamps[-1]}", file=sys.stderr)


if __name__ == "__main__":
    main()
