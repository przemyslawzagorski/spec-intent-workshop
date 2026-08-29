#!/usr/bin/env -S uv run --script
# /// script
# requires-python = ">=3.11"
# dependencies = ["pyyaml>=6"]
# ///
"""
policy-cases - generuje tabele przypadkow testowych z return-policy.yaml.

Po co to w ogole jest.

Testy z zahardkodowanymi progami trzeba przepisywac za kazdym razem, gdy
biznes zmieni okno zwrotu z 14 na 21 dni. Tutaj testy sa GENEROWANE: bierzemy
wartosci z return-policy.yaml i stosujemy do nich wspolna procedure decyzyjna
(docs/contract/decision-procedure.md). Zmien plik polityki - cala tabela
przypadkow zmieni sie sama, a kod nie drgnie.

Ten plik jest wykonywalna wersja kontraktu. Jesli implementacja nie zgadza sie
z ta tabela, to albo kod jest zly, albo ktos zle przeczytal procedure. Tabela
nie jest zla - bo tabela JEST procedura.

Wyjscie: TSV na stdout. Tabela, nie proza - bo tabele da sie sprawdzic
maszynowo i sa tansze w tokenach.

Uzycie:
    uv run tools/policy_cases.py return-policy.yaml
    uv run tools/policy_cases.py return-policy.yaml > cases.tsv
"""
import io
import pathlib
import sys

import yaml

KOLUMNY = [
    "caseId", "opis", "category", "daysSinceDelivery", "amount",
    "abuseScore", "partial", "reason",
    "expectedDecision", "expectedReasonCodes",
]

ODRZUCAJACE = ("CATEGORY_EXCLUDED", "WINDOW_EXPIRED", "PARTIAL_NOT_ALLOWED", "ABUSE_SUSPECTED")
DO_PRZEGLADU = ("ABUSE_BORDERLINE", "AMOUNT_ABOVE_THRESHOLD")


def okno(p: dict, kategoria: str) -> int:
    return p["windows"].get(kategoria, p["windows"]["default"])


def decyduj(p: dict, *, category: str, days: int, amount: float,
            abuse: float, partial: bool) -> tuple[str, list[str]]:
    """Procedura z docs/contract/decision-procedure.md. Kolejnosc krokow
    i domknietosc granic sa czescia kontraktu - patrz sekcja 'Granice'."""
    kody: list[str] = []

    if category in p["excludedCategories"]:
        kody.append("CATEGORY_EXCLUDED")
    if days > okno(p, category):                       # ostre: dzien graniczny JEST w oknie
        kody.append("WINDOW_EXPIRED")
    if partial and not p["partialReturnAllowed"]:
        kody.append("PARTIAL_NOT_ALLOWED")
    if abuse >= p["abuse"]["rejectAt"]:                # domkniete
        kody.append("ABUSE_SUSPECTED")
    elif abuse >= p["abuse"]["reviewAt"]:              # domkniete
        kody.append("ABUSE_BORDERLINE")
    if amount > p["manualReviewAboveAmount"]:          # ostre: rowna progowi NIE idzie do przegladu
        kody.append("AMOUNT_ABOVE_THRESHOLD")

    if not kody:
        kody.append("WITHIN_POLICY")

    if any(k in ODRZUCAJACE for k in kody):
        return "REJECTED", kody
    if any(k in DO_PRZEGLADU for k in kody):
        return "MANUAL_REVIEW", kody
    return "AUTO_APPROVED", kody


def scenariusze(p: dict):
    """Systematyczne pokrycie: kazda granica z osobna, potem kombinacje."""
    prog = p["manualReviewAboveAmount"]
    ab = p["abuse"]
    bezpieczna_kwota = max(prog - 1, 0)
    kategorie_ok = [k for k in p["windows"] if k != "default"
                    and k not in p["excludedCategories"]] or ["default"]

    n = 0
    # --- granice okna, dla kazdej kategorii z wlasnym oknem + default ---
    for kat in ["default", *kategorie_ok]:
        w = okno(p, kat)
        for dni, etykieta in ((max(w - 1, 0), "dzien przed koncem okna"),
                              (w, "OSTATNI dzien okna - wciaz w oknie"),
                              (w + 1, "pierwszy dzien PO oknie")):
            n += 1
            yield dict(caseId=f"W{n:02d}", opis=f"{kat}: {etykieta}", category=kat,
                       daysSinceDelivery=dni, amount=bezpieczna_kwota, abuseScore=0.0,
                       partial=False, reason="CHANGED_MIND")

    # --- kategorie wykluczone ---
    for i, kat in enumerate(p["excludedCategories"], 1):
        yield dict(caseId=f"X{i:02d}", opis=f"kategoria wykluczona: {kat}", category=kat,
                   daysSinceDelivery=0, amount=bezpieczna_kwota, abuseScore=0.0,
                   partial=False, reason="DAMAGED")

    # --- prog kwotowy ---
    for i, (kwota, etykieta) in enumerate((
            (bezpieczna_kwota, "ponizej progu kwotowego"),
            (prog, "DOKLADNIE prog - NIE idzie do przegladu"),
            (prog + 1, "tuz powyzej progu")), 1):
        yield dict(caseId=f"A{i:02d}", opis=etykieta, category="default",
                   daysSinceDelivery=0, amount=kwota, abuseScore=0.0,
                   partial=False, reason="CHANGED_MIND")

    # --- progi naduzyc ---
    krok = 0.01
    for i, (score, etykieta) in enumerate((
            (max(ab["reviewAt"] - krok, 0.0), "tuz ponizej progu przegladu"),
            (ab["reviewAt"], "DOKLADNIE prog przegladu - juz dziala"),
            (max(ab["rejectAt"] - krok, 0.0), "tuz ponizej progu odrzucenia"),
            (ab["rejectAt"], "DOKLADNIE prog odrzucenia - juz dziala")), 1):
        yield dict(caseId=f"B{i:02d}", opis=etykieta, category="default",
                   daysSinceDelivery=0, amount=bezpieczna_kwota,
                   abuseScore=round(score, 4), partial=False, reason="CHANGED_MIND")

    # --- zwrot czesciowy ---
    yield dict(caseId="P01", opis="zwrot czesciowy", category="default",
               daysSinceDelivery=0, amount=bezpieczna_kwota, abuseScore=0.0,
               partial=True, reason="CHANGED_MIND")

    # --- precedencja: wiele powodow naraz ---
    yield dict(caseId="C01", opis="po oknie ORAZ powyzej progu kwotowego -> wygrywa REJECTED",
               category="default", daysSinceDelivery=okno(p, "default") + 10,
               amount=prog + 100, abuseScore=0.0, partial=False, reason="CHANGED_MIND")
    yield dict(caseId="C02", opis="naduzycie ORAZ powyzej progu -> wygrywa REJECTED",
               category="default", daysSinceDelivery=0, amount=prog + 100,
               abuseScore=ab["rejectAt"], partial=False, reason="CHANGED_MIND")

    # --- kto placi za przesylke ---
    for i, powod in enumerate(sorted(p["shipping"]["paidBy"]), 1):
        yield dict(caseId=f"S{i:02d}", opis=f"platnosc za przesylke, powod {powod}",
                   category="default", daysSinceDelivery=0, amount=bezpieczna_kwota,
                   abuseScore=0.0, partial=False, reason=powod)


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
        print("      najpierw napisz swoja polityke - patrz docs/domain-cards/", file=sys.stderr)
        return 1
    try:
        p = yaml.safe_load(plik.read_text(encoding="utf-8"))
    except yaml.YAMLError as e:
        print(f"FAIL  {plik} nie jest poprawnym YAML-em: {e}", file=sys.stderr)
        return 1
    if not isinstance(p, dict):
        print(f"FAIL  {plik}: oczekiwano mapy na najwyzszym poziomie", file=sys.stderr)
        return 1
    try:
        p["windows"]["default"]
    except (KeyError, TypeError):
        print(f"FAIL  {plik}: brakuje windows.default - uruchom najpierw policy_check.py",
              file=sys.stderr)
        return 1

    print("\t".join(KOLUMNY))
    for s in scenariusze(p):
        decyzja, kody = decyduj(p, category=s["category"], days=s["daysSinceDelivery"],
                                amount=s["amount"], abuse=s["abuseScore"],
                                partial=s["partial"])
        wiersz = {**s, "expectedDecision": decyzja, "expectedReasonCodes": ",".join(sorted(kody))}
        print("\t".join(str(wiersz[k]) for k in KOLUMNY))
    return 0


if __name__ == "__main__":
    sys.exit(main())
