#!/usr/bin/env -S uv run --script
# /// script
# requires-python = ">=3.11"
# dependencies = ["pyyaml>=6", "jsonschema>=4"]
# ///
"""
policy-check - waliduje return-policy.yaml.

Dwie warstwy, celowo rozdzielone:

  HARD  schemat + sprzecznosci, ktore czynia polityke niewykonalna -> blokuje
  SOFT  redundancje i podejrzane wartosci -> ostrzega, nie blokuje

Ten sam podzial co w ./bramka. Narzedzie, ktore uczy o bramkach, samo musi
odrozniac "nie wpuszcze" od "zwracam uwage".

Uzycie:
    uv run tools/policy_check.py return-policy.yaml
"""
import json
import pathlib
import sys

import jsonschema
import yaml

ROOT = pathlib.Path(__file__).resolve().parent.parent
SCHEMA = ROOT / "docs" / "contract" / "return-policy.schema.json"


def spojnosc(p: dict) -> tuple[list[str], list[str]]:
    """Reguly miedzypolowe, ktorych schema JSON nie wyrazi.
    Zwraca (bledy_hard, ostrzezenia_soft)."""
    bledy: list[str] = []
    ostrzezenia: list[str] = []

    ab = p["abuse"]
    if ab["reviewAt"] > ab["rejectAt"]:
        bledy.append(
            f"abuse.reviewAt ({ab['reviewAt']}) > abuse.rejectAt ({ab['rejectAt']}): "
            "prog recznej akceptacji musi byc nizszy niz prog odrzucenia, "
            "inaczej ABUSE_BORDERLINE nigdy nie zadziala"
        )

    wykluczone = set(p["excludedCategories"])
    okna = {k for k in p["windows"] if k != "default"}
    for kat in sorted(wykluczone & okna):
        ostrzezenia.append(
            f"kategoria '{kat}' jest jednoczesnie w excludedCategories i w windows: "
            "okno nigdy nie zostanie uzyte, bo CATEGORY_EXCLUDED zadziala wczesniej. "
            "To nie jest blad krytyczny, ale prawdopodobnie nie o to ci chodzilo"
        )

    if p["manualReviewAboveAmount"] == 0:
        ostrzezenia.append(
            "manualReviewAboveAmount = 0: kazdy zwrot o dodatniej kwocie pojdzie "
            "do recznej akceptacji. Jesli to celowe - podnies prog do wartosci, "
            "ktora to wyraza wprost"
        )

    return bledy, ostrzezenia


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__)
        return 2

    plik = pathlib.Path(sys.argv[1])
    if not plik.is_file():
        print(f"FAIL  nie ma pliku: {plik}")
        return 1

    try:
        polityka = yaml.safe_load(plik.read_text(encoding="utf-8"))
    except yaml.YAMLError as e:
        print(f"FAIL  {plik} nie jest poprawnym YAML-em:\n      {e}")
        return 1

    if not isinstance(polityka, dict):
        print(f"FAIL  {plik}: oczekiwano mapy na najwyzszym poziomie")
        return 1

    schema = json.loads(SCHEMA.read_text(encoding="utf-8"))
    walidator = jsonschema.Draft202012Validator(schema)
    naruszenia = sorted(walidator.iter_errors(polityka), key=lambda e: list(e.path))

    if naruszenia:
        print(f"FAIL  {plik}: niezgodnosc ze schematem")
        for e in naruszenia:
            sciezka = "/".join(str(x) for x in e.path) or "(korzen)"
            print(f"      {sciezka}: {e.message}")
        return 1

    bledy, ostrzezenia = spojnosc(polityka)
    if bledy:
        print(f"FAIL  {plik}: schemat OK, ale polityka jest wewnetrznie sprzeczna")
        for b in bledy:
            print(f"      {b}")
        return 1

    for o in ostrzezenia:
        print(f"WARN  {o}")

    kat = len(polityka["windows"]) - 1
    print(
        f"OK    {plik}: schemat i spojnosc. "
        f"{kat} kategorii z wlasnym oknem, "
        f"{len(polityka['excludedCategories'])} wykluczonych, "
        f"prog recznej akceptacji {polityka['manualReviewAboveAmount']}"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
