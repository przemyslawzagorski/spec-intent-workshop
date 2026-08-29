"""Granica HTTP asystenta."""
from __future__ import annotations

import os
import pathlib
from uuid import UUID

from fastapi import APIRouter
from pydantic import BaseModel, ConfigDict

from scoring.control.assistant import odpowiedz
from scoring.control.llm_port import LlmPort, ReplayLlm
from scoring.entity.assistant import AssistantReply

router = APIRouter()


class Pytanie(BaseModel):
    model_config = ConfigDict(extra="forbid")

    customerId: UUID
    question: str


def llm() -> LlmPort:
    """Wybor dostawcy. Jedyne miejsce w kodzie, ktore o nim wie.

    Domyslnie odtwarzacz nagran - zeby dalo sie uruchomic serwis bez klucza API.
    Podmiana na prawdziwego dostawce to jedna linia, bo reszta kodu zna tylko port.
    """
    nagrania = pathlib.Path(
        os.environ.get("LLM_RECORDINGS", "tests/fixtures/llm-recordings.json")
    )
    return ReplayLlm(nagrania)


@router.post("/assistant/ask", response_model=AssistantReply)
def zapytaj(pytanie: Pytanie) -> AssistantReply:
    return odpowiedz(pytanie=pytanie.question, klient=pytanie.customerId, llm=llm())
