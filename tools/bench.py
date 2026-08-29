#!/usr/bin/env -S uv run --script
# /// script
# requires-python = ">=3.11"
# dependencies = []
# ///
"""
bench - pomiary warsztatowe: koszt kontekstu i porownanie modeli.

Uzywane tam, gdzie chcemy porownac dwa podejscia liczbami, a nie wrazeniem:
  Z02  proza kontra wskaznik na wzorzec
  Z04  caly kontekst kontra celowany fragment
  B2   ten sam task i ta sama bramka na roznych modelach

Pomiary trafiaja do wspolnego pliku bench.tsv - tabela, nie proza.
Sala widzi rozrzut w grupie, nie pojedynczy wynik.

    uv run tools/bench.py estimate AGENTS.md docs/contract/decision-procedure.md
    uv run tools/bench.py record --etykieta "Z02 proza" --model sonnet --iteracje 4 --sekundy 95 --tokeny 8200
    uv run tools/bench.py report
"""
import argparse
import csv
import pathlib
import sys

KORZEN = pathlib.Path(__file__).resolve().parent.parent
WYNIKI = KORZEN / "bench.tsv"
KOLUMNY = ["etykieta", "model", "iteracje", "sekundy", "tokeny", "kto"]

# Przyblizenie. Dla tekstu lacinskiego i kodu wychodzi zwykle 3.5-4.5 znaku
# na token. Nie mierzymy tu dokladnie - mierzymy RZAD WIELKOSCI, bo o niego
# chodzi w porownaniu "strona prozy kontra wskaznik na plik".
ZNAKOW_NA_TOKEN = 4.0


def estimate(sciezki: list[str]) -> int:
    print(f"{'plik':<52} {'bajty':>8} {'~tokeny':>8}")
    print("-" * 70)
    razem = 0
    for s in sciezki:
        p = pathlib.Path(s)
        if not p.is_file():
            print(f"{s:<52} {'BRAK':>8}")
            continue
        b = len(p.read_bytes())
        t = round(b / ZNAKOW_NA_TOKEN)
        razem += t
        print(f"{s:<52} {b:>8} {t:>8}")
    print("-" * 70)
    print(f"{'RAZEM':<52} {'':>8} {razem:>8}")
    print()
    print("Pamietaj, co z tego siedzi w KAZDYM promptcie (AGENTS.md), a co")
    print("laduje sie na zadanie (skille, wzorce, dokumentacja). Mnoznikiem")
    print("jest liczba waszych interakcji, nie liczba plikow.")
    return 0


def record(a: argparse.Namespace) -> int:
    nowy = not WYNIKI.exists()
    with WYNIKI.open("a", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=KOLUMNY, delimiter="\t", lineterminator="\n")
        if nowy:
            w.writeheader()
        w.writerow({
            "etykieta": a.etykieta, "model": a.model, "iteracje": a.iteracje,
            "sekundy": a.sekundy, "tokeny": a.tokeny, "kto": a.kto,
        })
    print(f"OK    zapisano do {WYNIKI.name}")
    return 0


def report() -> int:
    if not WYNIKI.exists():
        print("Brak pomiarow. Najpierw: uv run tools/bench.py record ...")
        return 1
    wiersze = list(csv.DictReader(WYNIKI.read_text(encoding="utf-8").splitlines(), delimiter="\t"))
    if not wiersze:
        print("bench.tsv jest pusty.")
        return 1

    grupy: dict[tuple[str, str], list[dict]] = {}
    for w in wiersze:
        grupy.setdefault((w["etykieta"], w["model"]), []).append(w)

    print(f"{'zadanie':<28} {'model':<14} {'n':>3} {'iteracje':>9} {'sekundy':>8} {'tokeny':>9}")
    print("-" * 76)
    for (etykieta, model), g in sorted(grupy.items()):
        def sr(k: str) -> float:
            wartosci = [float(x[k]) for x in g if x[k]]
            return sum(wartosci) / len(wartosci) if wartosci else 0.0
        print(f"{etykieta:<28} {model:<14} {len(g):>3} "
              f"{sr('iteracje'):>9.1f} {sr('sekundy'):>8.0f} {sr('tokeny'):>9.0f}")
    print()
    print("Teza do sprawdzenia na tych liczbach:")
    print("  im lepszy harness i spec, tym slabszy model wystarcza.")
    print("Jesli liczby jej nie potwierdzaja - to tez jest wynik. Omowcie go uczciwie.")
    return 0


def main() -> int:
    p = argparse.ArgumentParser(prog="bench", description=__doc__,
                                formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = p.add_subparsers(dest="cmd", required=True)

    e = sub.add_parser("estimate", help="oszacuj koszt kontekstu podanych plikow")
    e.add_argument("pliki", nargs="+")

    r = sub.add_parser("record", help="zapisz pomiar")
    r.add_argument("--etykieta", required=True, help='np. "Z02 proza" albo "Z04 caly repo"')
    r.add_argument("--model", required=True, help="nazwa modelu albo narzedzia")
    r.add_argument("--iteracje", type=int, required=True, help="ile podejsc do zielonego")
    r.add_argument("--sekundy", type=int, default=0)
    r.add_argument("--tokeny", type=int, default=0)
    r.add_argument("--kto", default="", help="imie, zeby dalo sie porownac rozrzut w grupie")

    sub.add_parser("report", help="pokaz zebrane pomiary")

    a = p.parse_args()
    if a.cmd == "estimate":
        return estimate(a.pliki)
    if a.cmd == "record":
        return record(a)
    return report()


if __name__ == "__main__":
    sys.exit(main())
