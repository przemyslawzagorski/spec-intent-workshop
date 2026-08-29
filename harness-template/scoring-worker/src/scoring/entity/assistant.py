"""Encje asystenta. Kontrakt odpowiedzi jest tym, co testujemy - nie tresc."""
from __future__ import annotations

from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field


class ToolCall(BaseModel):
    """Zadanie wywolania narzedzia, zgloszone przez model."""

    model_config = ConfigDict(extra="forbid")

    name: str
    arguments: dict[str, Any] = Field(default_factory=dict)


class AssistantReply(BaseModel):
    """Odpowiedz asystenta.

    Asercje w testach dotycza TEGO KSZTALTU, nigdy tresci pola `text`.
    Tresc jest niedeterministyczna z definicji i asercja na niej byla by
    flaky - a przez poltora dnia uczylismy sie czegos odwrotnego.
    """

    model_config = ConfigDict(extra="forbid")

    text: str
    used_tools: list[str] = Field(default_factory=list)
    status: Literal["OK", "BLOCKED"] = "OK"
