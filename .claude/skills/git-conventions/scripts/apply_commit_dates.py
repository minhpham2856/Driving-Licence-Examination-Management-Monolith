#!/usr/bin/env python3
"""Apply author/committer dates from CSV to local commits via git filter-branch."""

import argparse
import csv
import subprocess
import sys


def run(cmd: list[str]) -> subprocess.CompletedProcess:
    print("+", " ".join(cmd), file=sys.stderr)
    return subprocess.run(cmd, text=True, capture_output=True)


def main() -> None:
    parser = argparse.ArgumentParser(description="Apply commit dates from CSV.")
    parser.add_argument("csv_file", help="CSV: hash,author_date,committer_date")
    parser.add_argument("--dry-run", action="store_true", help="Print mapping only")
    args = parser.parse_args()

    mapping: dict[str, tuple[str, str]] = {}
    with open(args.csv_file, encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            h = row["hash"].strip()
            if h.startswith("COMMIT_"):
                continue
            author = row["author_date"].strip()
            committer = row["committer_date"].strip()
            mapping[h] = (author, committer)

    if not mapping:
        print("No real commit hashes in CSV (only COMMIT_N placeholders?).", file=sys.stderr)
        print("Regenerate with --hashes from: git log --reverse --format=%H", file=sys.stderr)
        if not args.dry_run:
            raise SystemExit(1)

    if args.dry_run:
        for h, (a, c) in mapping.items():
            print(f"{h[:12]}  author={a}  committer={c}")
        print(f"\n{len(mapping)} commits (dry-run)", file=sys.stderr)
        return

    inside = run(["git", "rev-parse", "--is-inside-work-tree"])
    if inside.stdout.strip() != "true":
        raise SystemExit("Not inside a git repository.")

    log_hashes = set(run(["git", "log", "--format=%H"]).stdout.splitlines())
    missing = [h for h in mapping if h not in log_hashes]
    if missing:
        print(f"Warning: {len(missing)} hashes not found in current branch.", file=sys.stderr)

    # case/esac env-filter scales better than 90+ if-lines
    cases = []
    for h, (author, committer) in mapping.items():
        cases.append(
            f'  {h}) export GIT_AUTHOR_DATE="{author}"; export GIT_COMMITTER_DATE="{committer}" ;;'
        )
    env_filter = "case \"$GIT_COMMIT\" in\n" + "\n".join(cases) + "\nesac"

    oldest = run(["git", "log", "--reverse", "--format=%H"] + list(mapping.keys()))
    if oldest.returncode != 0 or not oldest.stdout.strip():
        # fallback: rewrite all commits reachable from HEAD that are in mapping
        rev_range = "HEAD"
    else:
        first_hash = oldest.stdout.strip().splitlines()[0]
        rev_range = f"{first_hash}^..HEAD"

    proc = subprocess.run(
        ["git", "filter-branch", "-f", "--env-filter", env_filter, rev_range],
        text=True,
    )
    if proc.returncode != 0:
        raise SystemExit(
            "git filter-branch failed. For small sets, use interactive rebase (see references/backdating.md)."
        )

    print(f"Applied dates to {len(mapping)} commits.", file=sys.stderr)
    print("Verify: git log --format='%ai %h %s' -10", file=sys.stderr)
    print("Cleanup refs: git update-ref -d refs/original/refs/heads/$(git branch --show-current)", file=sys.stderr)


if __name__ == "__main__":
    main()
