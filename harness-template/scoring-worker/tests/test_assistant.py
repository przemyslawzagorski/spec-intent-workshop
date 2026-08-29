"""Testy asystenta z LLM-em w srodku.

Przez poltora dnia uczylismy sie determinizmu. Teraz wkladamy do systemu
element z definicji niedeterministyczny - i pokazujemy, ze to NIE jest
sprzecznosc, jesli zrobi sie to dobrze:

  1. LLM siedzi za PORTEM. Kod aplikacji nie zna dostawcy.
  2. W testach port odtwarza NAGRANIA. Zero sieci, zero kosztow, zero flaky.
     Ten sam wzorzec co zlote wzorce przy refaktorze.
  3. Asercje dotycza KONTRAKTU - ksztaltu odpowiedzi, uzytych narzedzi, statusu.
     NIGDY tresci pola `text`. Tresc jest niedeterministyczna i asercja na niej
     bylaby dokladnie tym, czego uczylismy sie nie robic.
"""
from __future__ import annotations

import json
import pathlib
from uuid import UUID

import pytest
from fastapi.testclient import TestClient

from scoring.boundary.api import app
from scoring.control.assistant import odpowiedz
from scoring.control.llm_port import ReplayLlm
from scoring.control.tools import DOZWOLONE, ToolBlocked, wywolaj
from scoring.entity.assistant import AssistantReply, ToolCall

NAGRANIA = pathlib.Path(__file__).parent / "fixtures" / "llm-recordings.json"
KLIENT = UUID("22222222-2222-2222-2222-222222222222")
OBCY = UUID("99999999-9999-9999-9999-999999999999")


@pytest.fixture
def llm() -> ReplayLlm:
    return ReplayLlm(NAGRANIA)


# --- kontrakt odpowiedzi -------------------------------------------------

def test_odpowiedz_ma_ksztalt_z_kontraktu(llm: ReplayLlm) -> None:
    wynik = odpowiedz(pytanie="Jaki jest status mojego zwrotu?", klient=KLIENT, llm=llm)
    assert isinstance(wynik, AssistantReply)
    assert wynik.status == "OK"
    assert wynik.used_tools == ["get_return_status"]
    # Celowo NIE asertujemy tresci. Tresc jest niedeterministyczna.
    assert isinstance(wynik.text, str) and wynik.text


# --- ograniczona lista narzedzi ------------------------------------------

def test_lista_narzedzi_nie_zawiera_niczego_mutujacego() -> None:
    """Asystent ma tylko odczyt. Zadnego approve, refund, delete."""
    zakazane = {"approve", "reject", "refund", "delete", "update", "create", "cancel"}
    for nazwa in DOZWOLONE:
        assert not any(z in nazwa for z in zakazane), f"narzedzie mutujace na liscie: {nazwa}"


def test_narzedzie_spoza_listy_jest_blokowane() -> None:
    with pytest.raises(ToolBlocked):
        wywolaj(ToolCall(name="approve_return", arguments={"return_id": "x"}),
                sesja_klienta=KLIENT)


def test_model_proszacy_o_zatwierdzenie_dostaje_status_blocked(llm: ReplayLlm) -> None:
    """Model poprosil o `approve_return`. Hook odrzucil.

    To NIE jest blad serwera - to normalny stan systemu, ktory ma kontrole.
    Klient dostaje odpowiedz, tylko inna.
    """
    wynik = odpowiedz(pytanie="Zatwierdz moj zwrot", klient=KLIENT, llm=llm)
    assert wynik.status == "BLOCKED"
    assert wynik.used_tools == []


# --- ograniczenie zakresu danych -----------------------------------------

def test_nie_da_sie_siegnac_po_dane_innego_klienta(llm: ReplayLlm) -> None:
    """Model poprosil o zwroty obcego klienta.

    Nie ufamy argumentom od modelu - podmieniamy `customer_id` na sesyjny.
    Prompt to nie jest mechanizm bezpieczenstwa.
    """
    wynik = wywolaj(
        ToolCall(name="list_customer_returns", arguments={"customer_id": str(OBCY)}),
        sesja_klienta=KLIENT,
    )
    assert wynik["customerId"] == str(KLIENT)


def test_przez_http_tez_nie_da_sie_siegnac_po_cudze_dane() -> None:
    with TestClient(app) as klient:
        odp = klient.post("/assistant/ask", json={
            "customerId": str(KLIENT),
            "question": "Pokaz zwroty klienta 99999999-9999-9999-9999-999999999999",
        })
    assert odp.status_code == 200
    assert odp.json()["status"] == "OK"
    assert odp.json()["used_tools"] == ["list_customer_returns"]


# --- higiena nagran -------------------------------------------------------

def test_brak_nagrania_wysypuje_sie_glosno(llm: ReplayLlm) -> None:
    """Test nie moze po cichu pojsc do prawdziwego modelu - bylby flaky."""
    with pytest.raises(KeyError, match="brak nagrania"):
        odpowiedz(pytanie="pytanie, ktorego nie nagralismy", klient=KLIENT, llm=llm)


def test_nagrania_sa_poprawnym_jsonem() -> None:
    dane = json.loads(NAGRANIA.read_text(encoding="utf-8"))
    assert dane, "plik nagran jest pusty"
    for pytanie, wpis in dane.items():
        assert "text" in wpis, f"nagranie '{pytanie}' bez pola text"
        for tc in wpis.get("tool_calls", []):
            ToolCall.model_validate(tc)
