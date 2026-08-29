"""Test kontraktowy szwu asynchronicznego.

Sprawdzamy SCHEMAT zdarzen, nie to, czy Kafka dowiozla. Dowozenie jest problemem
Kafki i ma wlasne testy - u jej autorow.

To jest ta sama zasada co przy testach systemowych w Javie: testujemy publiczna
powierzchnie, nie internale. Tu publiczna powierzchnia to ksztalt zdarzenia.
"""
from __future__ import annotations

import json
import pathlib
from datetime import datetime, timedelta, timezone
from uuid import uuid4

import jsonschema
import pytest
from fastapi.testclient import TestClient

from scoring.boundary.api import app
from scoring.entity.models import ReturnSubmitted

def _katalog_schematow() -> pathlib.Path:
    """Szuka kontraktu w gore, zamiast zakladac glebokosc katalogu.

    Sciezke znajdujemy szukajac korzenia repo w gore -
    sztywna liczba .parent dziala tylko w jednym z tych przypadkow.
    """
    start = pathlib.Path(__file__).resolve().parent
    for katalog in [start, *start.parents]:
        kandydat = katalog / "docs" / "contract" / "events"
        if kandydat.is_dir():
            return kandydat
    raise RuntimeError(f"nie znalazlem docs/contract/events w gore od {start}")


SCHEMATY = _katalog_schematow()
TERAZ = datetime(2026, 6, 15, 12, 0, tzinfo=timezone.utc)


def schemat(nazwa: str) -> dict:
    return json.loads((SCHEMATY / nazwa).read_text(encoding="utf-8"))


def przykladowe_zdarzenie() -> dict:
    return {
        "returnId": str(uuid4()),
        "customerId": str(uuid4()),
        "requestedAt": TERAZ.isoformat().replace("+00:00", "Z"),
        "ordersInWindow": 12,
        "history": [
            {
                "returnedAt": (TERAZ - timedelta(days=3)).isoformat().replace("+00:00", "Z"),
                "decision": "AUTO_APPROVED",
            }
        ],
    }


def test_zdarzenie_wejsciowe_zgodne_ze_schematem() -> None:
    jsonschema.validate(przykladowe_zdarzenie(), schemat("return-submitted.schema.json"))


def test_odpowiedz_zgodna_ze_schematem_wyjsciowym() -> None:
    """R: /score zwraca dokladnie ksztalt return.scored."""
    with TestClient(app) as klient:
        odpowiedz = klient.post("/score", json=przykladowe_zdarzenie())
    assert odpowiedz.status_code == 200
    jsonschema.validate(odpowiedz.json(), schemat("return-scored.schema.json"))


def test_zdarzenie_z_nadmiarowym_polem_jest_odrzucane() -> None:
    """Kontrakt jest zamkniety (additionalProperties: false).

    Nadmiarowe pole to najczestszy objaw rozjechania sie wersji producenta
    i konsumenta - lepiej, zeby wysypalo sie glosno tutaj niz cicho na produkcji.
    """
    zdarzenie = przykladowe_zdarzenie() | {"nieznanePole": "cokolwiek"}
    with pytest.raises(jsonschema.ValidationError):
        jsonschema.validate(zdarzenie, schemat("return-submitted.schema.json"))
    with pytest.raises(Exception):
        ReturnSubmitted.model_validate(zdarzenie)


def test_serwis_zglasza_gotowosc() -> None:
    with TestClient(app) as klient:
        assert klient.get("/q/health/ready").json()["status"] == "UP"
