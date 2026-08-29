#!/usr/bin/env -S uv run --script
# /// script
# requires-python = ">=3.11"
# dependencies = []
# ///
"""
sprawdz-odnosniki - czy odnosniki w markdownie prowadza do istniejacych plikow.

Material warsztatowy sklada sie z kilkudziesieciu plikow, ktore wskazuja na
siebie nawzajem. Odnosnik, ktory prowadzi donikad, uczestnik znajdzie w trakcie
zajec - i to jest najgorszy moment, zeby sie o nim dowiedziec.

Sprawdzamy tylko odnosniki lokalne. Adresy http nie sa naszym problemem.

Uzycie:
    uv run tools/sprawdz_odnosniki.py [katalog]
"""
import pathlib
import re
import sys

POMIJANE = {".git", "praca", ".tooling", "node_modules", ".venv", "target",
            "__pycache__", ".pytest_cache", ".site"}
ODNOSNIK = re.compile(r"\[([^\]]+)\]\(([^)]+)\)")


def main() -> int:
    root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    zle: list[str] = []
    ile = 0

    for md in sorted(root.rglob("*.md")):
        if POMIJANE & set(md.relative_to(root).parts):
            continue
        tekst = md.read_text(encoding="utf-8", errors="replace")
        for m in ODNOSNIK.finditer(tekst):
            cel = m.group(2).split("#")[0].strip()
            if not cel or cel.startswith(("http://", "https://", "mailto:")):
                continue
            ile += 1
            if not (md.parent / cel).exists():
                zle.append(f"{md.relative_to(root).as_posix()}  ->  {cel}")

    if zle:
        print(f"FAIL  {len(zle)} zepsutych odnosnikow (na {ile} sprawdzonych):")
        for z in zle[:15]:
            print(f"      {z}")
        if len(zle) > 15:
            print(f"      ... i jeszcze {len(zle) - 15}")
        return 1

    print(f"OK    {ile} odnosnikow lokalnych, wszystkie prowadza do istniejacych plikow")
    return 0


if __name__ == "__main__":
    sys.exit(main())
