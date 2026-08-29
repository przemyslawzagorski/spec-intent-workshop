"""Port do modelu jezykowego.

TO JEST PUENTA CALEGO WARSZTATU.

Niedeterminizm nie jest zakazany. Ma byc **odizolowany za portem** i testowany
**na kontrakcie, nie na tresci**.

Za tym interfejsem moze stac cokolwiek: @anthropic-ai/claude-agent-sdk,
@github/copilot-sdk, @augmentcode/auggie-sdk, zwykle API HTTP albo - w testach -
odtwarzacz nagranych odpowiedzi. Kod aplikacji nie wie, ktore z nich dziala,
i to jest jedyny powod, dla ktorego da sie go przetestowac deterministycznie.
"""
from __future__ import annotations

import json
import pathlib
from typing import Protocol

from scoring.entity.assistant import ToolCall


class LlmPort(Protocol):
    """Jedyne wejscie do modelu. Aplikacja nie zna zadnego dostawcy."""

    def decide(self, question: str, available_tools: list[str]) -> tuple[str, list[ToolCall]]:
        """Zwraca (tresc odpowiedzi, lista zadanych wywolan narzedzi)."""
        ...


class ReplayLlm:
    """Odtwarzacz nagranych odpowiedzi - LlmPort na potrzeby testow.

    Dokladnie ten sam wzorzec, co zlote wzorce przy refaktorze: nagrywasz raz, odtwarzasz
    zawsze. Roznica jest taka, ze tam nagrywales legacy, a tu model.
    """

    def __init__(self, nagrania: pathlib.Path) -> None:
        self._dane = json.loads(nagrania.read_text(encoding="utf-8"))

    def decide(self, question: str, available_tools: list[str]) -> tuple[str, list[ToolCall]]:
        wpis = self._dane.get(question)
        if wpis is None:
            raise KeyError(
                f"brak nagrania dla pytania: {question!r}. "
                "Dograj je do fixtures/llm-recordings.json albo uzyj innego pytania. "
                "Test NIE MOZE isc do prawdziwego modelu - byloby flaky."
            )
        return wpis["text"], [ToolCall.model_validate(t) for t in wpis.get("tool_calls", [])]
