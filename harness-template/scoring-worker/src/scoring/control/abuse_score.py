"""Wyliczenie wskaznika naduzyc.

Regula jest czescia wspolnego kontraktu - docs/contract/events.md.
Ta funkcja jest CZYSTA: bez wejscia/wyjscia, bez zegara, bez bazy, bez Kafki.
Dzieki temu tabela przypadkow z tools/score_cases.py testuje ja bezposrednio,
bez stawiania czegokolwiek.
"""
from __future__ import annotations

from datetime import datetime, timedelta

from scoring.entity.models import PastReturn


def w_oknie(*, requested_at: datetime, returned_at: datetime, okno_dni: int) -> bool:
    """Czy zwrot miesci sie w oknie naduzyc.

    Okno jest DOMKNIETE z obu stron: zwrot dokladnie sprzed `okno_dni` dni
    jeszcze sie liczy. Zwroty z przyszlosci sa ignorowane, nie sa bledem -
    zegary serwisow chodza inaczej i to jest normalne.
    """
    roznica = requested_at - returned_at
    return timedelta(0) <= roznica <= timedelta(days=okno_dni)


def policz(
    *,
    requested_at: datetime,
    history: list[PastReturn],
    orders_in_window: int,
    okno_dni: int,
) -> float:
    """abuseScore = zwroty_w_oknie / max(zamowienia_w_oknie, 1), obciete do [0, 1]."""
    zwroty = sum(
        1
        for h in history
        if w_oknie(requested_at=requested_at, returned_at=h.returnedAt, okno_dni=okno_dni)
    )
    return min(1.0, zwroty / max(orders_in_window, 1))
