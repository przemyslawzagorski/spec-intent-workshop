"""Logika asystenta: zapytaj model, przepusc jego zadania przez hook, zloz odpowiedz.

Zero HTTP, zero dostawcy LLM. Dostaje port, wiec da sie to przetestowac
odtwarzaczem nagran - deterministycznie, bez sieci i bez kosztow.
"""
from __future__ import annotations

import logging
from uuid import UUID

from scoring.control.llm_port import LlmPort
from scoring.control.tools import DOZWOLONE, ToolBlocked, wywolaj
from scoring.entity.assistant import AssistantReply

LOG = logging.getLogger("scoring.assistant")


def odpowiedz(*, pytanie: str, klient: UUID, llm: LlmPort) -> AssistantReply:
    tresc, zadania = llm.decide(pytanie, sorted(DOZWOLONE))

    uzyte: list[str] = []
    for zadanie in zadania:
        try:
            wywolaj(zadanie, sesja_klienta=klient)
        except ToolBlocked as e:
            LOG.warning("%s", e)
            # Zablokowane wywolanie NIE jest bledem serwera. Jest normalnym
            # stanem systemu, ktory ma kontrole - i klient musi to zobaczyc.
            return AssistantReply(
                text="Nie moge wykonac tej operacji.",
                used_tools=uzyte,
                status="BLOCKED",
            )
        uzyte.append(zadanie.name)

    return AssistantReply(text=tresc, used_tools=uzyte, status="OK")
