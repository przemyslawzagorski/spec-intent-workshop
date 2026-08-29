"""Tabela decyzyjna dla scoringu - dokladnie ten sam wzorzec co w Javie.

Tabela nie jest pisana recznie. Generuje ja tools/score_cases.py z twojego
return-policy.yaml, stosujac wspolna regule z docs/contract/events.md.

To jest dowod, ze wzorzec "spec jako dane -> generator -> tabela -> testy"
nie byl sztuczka na jeden serwis ani na jeden jezyk.
"""
from __future__ import annotations

import csv
import os
import pathlib
import subprocess
import sys
from datetime import datetime, timedelta, timezone

import pytest

from scoring.control.abuse_score import policz
from scoring.control.policy import ReturnPolicy
from scoring.entity.models import Decision, PastReturn

KORZEN = pathlib.Path(__file__).resolve().parent.parent
POLITYKA = pathlib.Path(os.environ["POLICY_FILE"])


def _korzen_repo() -> pathlib.Path:
    """Szuka korzenia repozytorium w gore, zamiast zakladac glebokosc katalogu.

    Bez tego testy dzialaja tylko wtedy, gdy worker lezy dokladnie dwa poziomy
    ponizej korzenia repo, niezaleznie od tego, skad uruchomiles pytest.
    """
    for katalog in [KORZEN, *KORZEN.parents]:
        if (katalog / "tools" / "score_cases.py").is_file():
            return katalog
    raise RuntimeError(
        "nie znalazlem korzenia repozytorium (szukam tools/score_cases.py w gore od "
        f"{KORZEN})"
    )


REPO = _korzen_repo()

TERAZ = datetime(2026, 6, 15, 12, 0, tzinfo=timezone.utc)


def wiersze() -> list[dict[str, str]]:
    """Generuje tabele w locie - nie ma szansy rozjechac sie z polityka."""
    wynik = subprocess.run(
        [sys.executable, str(REPO / "tools" / "score_cases.py"), str(POLITYKA)],
        capture_output=True, text=True, check=True,
    )
    return list(csv.DictReader(wynik.stdout.splitlines(), delimiter="\t"))


@pytest.mark.parametrize("wiersz", wiersze(), ids=lambda w: f"{w['caseId']} {w['opis']}")
def test_wynik_zgadza_sie_z_tabela(wiersz: dict[str, str]) -> None:
    okno = ReturnPolicy.wczytaj(POLITYKA).abuse_window_days
    offsety = [int(o) for o in wiersz["historyOffsetsDays"].split(",") if o]
    history = [
        PastReturn(returnedAt=TERAZ - timedelta(days=o), decision=Decision.AUTO_APPROVED)
        for o in offsety
    ]

    otrzymany = policz(
        requested_at=TERAZ,
        history=history,
        orders_in_window=int(wiersz["ordersInWindow"]),
        okno_dni=okno,
    )

    assert otrzymany == pytest.approx(float(wiersz["expectedScore"]), abs=1e-6), (
        f"{wiersz['caseId']}: {wiersz['opis']}"
    )


def test_wynik_zawsze_w_przedziale_jednostkowym() -> None:
    """Kontrakt: abuseScore ma byc w [0, 1] - takze przy absurdalnych danych."""
    okno = ReturnPolicy.wczytaj(POLITYKA).abuse_window_days
    history = [
        PastReturn(returnedAt=TERAZ - timedelta(days=1), decision=Decision.REJECTED)
        for _ in range(1000)
    ]
    assert 0.0 <= policz(
        requested_at=TERAZ, history=history, orders_in_window=0, okno_dni=okno
    ) <= 1.0
