"""Polityka zwrotow - ten sam plik, ktory czyta returns-service.

Polityka jest JEDNA. Dwa serwisy, dwa jezyki, jedno zrodlo wartosci.
Gdyby kazdy mial swoja kopie, rozjechalyby sie przy pierwszej zmianie progu.
"""
from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path

import yaml

DOMYSLNA_SCIEZKA = Path(os.environ.get("POLICY_FILE", "../return-policy.yaml"))


@dataclass(frozen=True, slots=True)
class ReturnPolicy:
    abuse_window_days: int

    @staticmethod
    def wczytaj(sciezka: Path | None = None) -> ReturnPolicy:
        plik = sciezka or DOMYSLNA_SCIEZKA
        if not plik.is_file():
            raise FileNotFoundError(
                f"nie ma polityki pod {plik.resolve()}. "
                "Ustaw POLICY_FILE albo uruchom z katalogu serwisu."
            )
        dane = yaml.safe_load(plik.read_text(encoding="utf-8"))
        return ReturnPolicy(abuse_window_days=int(dane["abuse"]["windowDays"]))
