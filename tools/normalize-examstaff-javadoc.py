#!/usr/bin/env python3
"""Normalize examstaff Javadoc: replace h2/ul/li with plain Javadoc text."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src" / "java" / "examstaff"


def normalize_block(block: str) -> str:
    s = block
    s = re.sub(r"<h2>(.*?)</h2>", lambda m: m.group(1).strip() + ":", s)
    s = re.sub(r"<h3>(.*?)</h3>", lambda m: m.group(1).strip() + ":", s)
    s = re.sub(r"</?ul>", "", s)
    s = re.sub(r"</?ol>", "", s)

    def li_repl(m: re.Match) -> str:
        return "- " + m.group(1).strip()

    s = re.sub(r"<li>(.*?)</li>", li_repl, s, flags=re.DOTALL)
    return s


def polish_block(block: str) -> str:
    lines = block.splitlines()
    normalized: list[str] = []
    for line in lines:
        if re.match(r"^\s*\*\s*-\s+", line):
            line = re.sub(r"^(\s*\*)\s+-\s+", r"\1 - ", line)
        normalized.append(line)

    out: list[str] = []
    for i, line in enumerate(normalized):
        if i > 0 and re.match(r"^\s*\* [^@\-].+:\s*$", line):
            prev = out[-1]
            if not re.match(r"^\s*\*\s*$", prev) and prev.strip() != "/**":
                out.append(" *")
        out.append(line)
    return "\n".join(out)


def main() -> None:
    changed = 0
    for path in sorted(ROOT.rglob("*.java")):
        text = path.read_text(encoding="utf-8")

        def transform(m: re.Match) -> str:
            block = m.group(0)
            if any(tag in block for tag in ("<h2>", "<h3>", "<ul>", "<li>", "<ol>")):
                block = normalize_block(block)
            return polish_block(block)

        new_text = re.sub(r"/\*\*.*?\*/", transform, text, flags=re.DOTALL)
        if new_text != text:
            path.write_text(new_text, encoding="utf-8")
            changed += 1
            print(path.relative_to(ROOT.parent.parent.parent))

    print(f"Updated {changed} files")


if __name__ == "__main__":
    main()
