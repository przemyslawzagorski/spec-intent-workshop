#!/usr/bin/env -S uv run --script
# /// script
# requires-python = ">=3.11"
# dependencies = ["pyyaml>=6"]
# ///
"""
score-cases - generuje tabele przypadkow dla scoring-workera.

Ten sam wzorzec co tools/policy_cases.py, tylko dla drugiego serwisu i drugiego
jezyka. Regula wyliczenia jest w docs/contract/events.md i jest WSPOLNA -
z twojej polityki bierzemy tylko `abuse.windowDays`.

To jest dowod, ze wzorzec "spec jako dane -> generator -> tabela -> testy"
nie byl sztuczka na jeden serwis.

Uzycie:
    uv run tools/score_cases.py return-policy.yaml
"""
import io
import pathlib
import sys

import yaml

KOLUMNY = [
    "caseId", "opis", "ordersInWindow", "historyOffsetsDays", "expectedScore",
]


def wynik(*, orders: int, offsety: list[int], okno: int) -> float:
    """Regula z docs/contract/events.md.

    Okno domkniete z obu stron; zwroty z przyszlosci (offset ujemny) ignorowane.
    """
    w_oknie = sum(1 for d in offsety if 0 <= d <= okno)
    return min(1.0, w_oknie / max(orders, 1))


def scenariusze(okno: int):
    yield "S01", "brak historii", 10, []
    yield "S02", "jeden zwrot w oknie", 10, [1]
    yield "S03", "zwrot DOKLADNIE na granicy okna - liczy sie", 10, [okno]
    yield "S04", "zwrot dzien PO oknie - nie liczy sie", 10, [okno + 1]
    yield "S05", "mieszanka: dwa w oknie, jeden poza", 10, [1, okno, okno + 5]
    yield "S06", "zwrot z przyszlosci - ignorowany, nie blad", 10, [-3]
    yield "S07", "wiecej zwrotow niz zamowien - wynik obciety do 1.0", 2, [1, 2, 3, 4]
    yield "S08", "zero zamowien w oknie - brak dzielenia przez zero", 0, [1]
    yield "S09", "zero zamowien i brak historii", 0, []
    yield "S10", "duza historia, duzo zamowien", 100, list(range(1, 21))


# Pod Windowsem print() tlumaczy koniec linii na CRLF, wiec wygenerowany plik
# mialby inne konce linii niz reszta repo, a .gitattributes wymusza LF.
# Efekt: git status nigdy nie jest czysty po uruchomieniu bramki.
# Owijamy strumien wyjscia tak, zeby zawsze pisal LF.
sys.stdout = io.TextIOWrapper(
    sys.stdout.buffer, encoding="utf-8", newline=chr(10), write_through=True
)


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__, file=sys.stderr)
        return 2
    plik = pathlib.Path(sys.argv[1])
    if not plik.is_file():
        print(f"FAIL  nie ma pliku: {plik}", file=sys.stderr)
        return 1
    polityka = yaml.safe_load(plik.read_text(encoding="utf-8"))
    okno = polityka["abuse"]["windowDays"]

    print("\t".join(KOLUMNY))
    for case_id, opis, orders, offsety in scenariusze(okno):
        print("\t".join([
            case_id, opis, str(orders),
            ",".join(str(o) for o in offsety),
            f"{wynik(orders=orders, offsety=offsety, okno=okno):.6f}",
        ]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
