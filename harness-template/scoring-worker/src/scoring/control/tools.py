"""Rejestr narzedzi asystenta i hook blokujacy.

Dwie warstwy kontroli, celowo rozdzielone:

  1. OGRANICZONA LISTA - model widzi tylko to, co mu damy. Nie ma tu zadnego
     narzedzia mutujacego. Nie da sie poprosic o cos, czego nie ma na liscie.

  2. HOOK BLOKUJACY - nawet jesli model poprosi o cos spoza listy (bo halucynuje,
     bo prompt injection w danych klienta, bo zmienila sie wersja modelu),
     wywolanie jest odrzucane TUTAJ, w kodzie, a nie w promptcie.

Prompt to nie jest mechanizm bezpieczenstwa. Prompt to prosba.
"""
from __future__ import annotations

import logging
from collections.abc import Callable
from uuid import UUID

from scoring.entity.assistant import ToolCall

LOG = logging.getLogger("scoring.tools")


class ToolBlocked(Exception):
    """Hook odrzucil wywolanie."""


def get_return_status(*, return_id: str, customer_id: str) -> dict[str, str]:
    """Status jednego zwrotu. W prawdziwym systemie: zapytanie do returns-service."""
    return {"returnId": return_id, "status": "MANUAL_REVIEW"}


def list_customer_returns(*, customer_id: str) -> dict[str, list[str]]:
    """Zwroty JEDNEGO klienta - tego z sesji, nie dowolnego."""
    return {"customerId": customer_id, "returns": []}


# Tylko odczyt. Zadnego approve, refund, delete - swiadomie.
DOZWOLONE: dict[str, Callable[..., dict]] = {
    "get_return_status": get_return_status,
    "list_customer_returns": list_customer_returns,
}


def wywolaj(call: ToolCall, *, sesja_klienta: UUID) -> dict:
    """Hook blokujacy. Kazde wywolanie narzedzia przechodzi tedy."""
    if call.name not in DOZWOLONE:
        LOG.warning("zablokowane narzedzie spoza listy: %s", call.name)
        raise ToolBlocked(f"narzedzie '{call.name}' nie jest dozwolone")

    # Ograniczenie zakresu danych: asystent widzi wylacznie klienta z tej sesji.
    # Model moze poprosic o cudze dane - i nie dostanie ich, bo podmieniamy
    # argument, zamiast ufac temu, co przyszlo z modelu.
    argumenty = dict(call.arguments)
    if "customer_id" in argumenty and argumenty["customer_id"] != str(sesja_klienta):
        LOG.warning("proba siegniecia po dane innego klienta - podmieniam na sesyjnego")
    argumenty["customer_id"] = str(sesja_klienta)

    return DOZWOLONE[call.name](**argumenty)
