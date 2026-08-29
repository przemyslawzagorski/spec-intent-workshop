"""Granica HTTP. Jedyne miejsce, ktore wie o FastAPI.

Odpowiednik ReturnsResource w Javie: przyjmuje, deleguje do control, zwraca.
Zero logiki biznesowej.

Endpoint /score istnieje po to, zeby dalo sie sprawdzic worker BEZ Kafki -
i zeby test systemowy nie musial stawiac brokera, kiedy testuje regule.
"""
from __future__ import annotations

import asyncio
import contextlib
import logging
import os
from collections.abc import AsyncIterator

from fastapi import FastAPI

from scoring.boundary.assistant_api import router as assistant_router
from scoring.control.abuse_score import policz
from scoring.control.policy import ReturnPolicy
from scoring.entity.models import ReturnScored, ReturnSubmitted

LOG = logging.getLogger("scoring.api")


@contextlib.asynccontextmanager
async def cykl_zycia(_: FastAPI) -> AsyncIterator[None]:
    """Startuje konsumenta Kafki obok serwera HTTP.

    Bez tego serwis odpowiada na /score, ale NIE konsumuje `return.submitted` -
    czyli szew asynchroniczny jest martwy, choc wszystko wyglada na uruchomione.

    Konsument jest domyslnie WYLACZONY (`KAFKA_ENABLED`), bo testy i lokalne
    uruchomienie nie maja brokera. Compose wlacza go jawnie.
    """
    zadanie: asyncio.Task | None = None
    if os.environ.get("KAFKA_ENABLED", "").lower() in {"1", "true", "yes"}:
        from scoring.boundary.events import obsluguj

        zadanie = asyncio.create_task(obsluguj())
        LOG.info("konsument Kafki wystartowal")
    else:
        LOG.info("konsument Kafki wylaczony (ustaw KAFKA_ENABLED=true, zeby wlaczyc)")

    try:
        yield
    finally:
        if zadanie is not None:
            zadanie.cancel()
            with contextlib.suppress(asyncio.CancelledError):
                await zadanie


app = FastAPI(title="scoring-worker", version="2026.0.0", lifespan=cykl_zycia)
app.include_router(assistant_router)
_polityka: ReturnPolicy | None = None


def polityka() -> ReturnPolicy:
    global _polityka
    if _polityka is None:
        _polityka = ReturnPolicy.wczytaj()
    return _polityka


@app.get("/q/health/ready")
def gotowosc() -> dict[str, str]:
    return {"status": "UP", "service": "scoring-worker"}


@app.post("/score", response_model=ReturnScored)
def score(zdarzenie: ReturnSubmitted) -> ReturnScored:
    wynik = policz(
        requested_at=zdarzenie.requestedAt,
        history=zdarzenie.history,
        orders_in_window=zdarzenie.ordersInWindow,
        okno_dni=polityka().abuse_window_days,
    )
    return ReturnScored(returnId=zdarzenie.returnId, abuseScore=wynik)
