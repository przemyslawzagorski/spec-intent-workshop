"""Encje - dane, zero zachowania, zero zaleznosci poza pydantic.

Odpowiednik pakietu `entity` w Javie. Ten sam uklad BCE, drugi jezyk -
i na tym polega szew asynchroniczny.
"""
from datetime import datetime
from enum import StrEnum
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field


class Decision(StrEnum):
    AUTO_APPROVED = "AUTO_APPROVED"
    MANUAL_REVIEW = "MANUAL_REVIEW"
    REJECTED = "REJECTED"


class PastReturn(BaseModel):
    """Jeden wczesniejszy zwrot tego klienta."""

    model_config = ConfigDict(extra="forbid")

    returnedAt: datetime
    decision: Decision


class ReturnSubmitted(BaseModel):
    """Zdarzenie wejsciowe. Kontrakt: docs/contract/events/return-submitted.schema.json

    Niesie WSZYSTKO, czego worker potrzebuje - dzieki temu worker nie siega
    do bazy i jest czysta funkcja.
    """

    model_config = ConfigDict(extra="forbid")

    returnId: UUID
    customerId: UUID
    requestedAt: datetime
    ordersInWindow: int = Field(ge=0)
    history: list[PastReturn] = Field(default_factory=list)


class ReturnScored(BaseModel):
    """Zdarzenie wyjsciowe. Minimalne - tylko to, czego tamten serwis nie policzy sam."""

    model_config = ConfigDict(extra="forbid")

    returnId: UUID
    abuseScore: float = Field(ge=0.0, le=1.0)
