# Kontrakt zdarzeń — szew między serwisami

Wspólny, niezmienny. Tak samo jak `openapi.yaml`, tylko dla szwu asynchronicznego.

## Po co w ogóle asynchronicznie

`returns-service` musi odpowiedzieć klientowi natychmiast. Wyliczenie wskaźnika
nadużyć wymaga przejrzenia historii klienta i **nie może blokować odpowiedzi**.

Konsekwencja jest uczciwa i trzeba ją zapisać w specyfikacji, a nie ukryć:
**pierwsza decyzja zapada bez wskaźnika nadużyć** (`abuseScore = 0`, założenie A5),
a po nadejściu wyniku scoringu decyzja jest **przeliczana ponownie i aktualizowana**.

`GET /returns/{returnId}` zwraca zawsze **aktualny** stan. Kształt odpowiedzi się
nie zmienia — kontrakt HTTP zostaje nietknięty.

## Przepływ

```
POST /returns
     │
     ├─► decyzja wstępna (abuseScore = 0)  ──► 201 do klienta
     │
     └─► return.submitted ──────────────► scoring-worker
                                               │ liczy abuseScore z historii
                                               ▼
         returns-service ◄────────── return.scored
                │
                └─► przelicza decyzję i aktualizuje zapis
```

## Temat `return.submitted`

Producent: `returns-service` · Konsument: `scoring-worker`

```json
{
  "returnId": "3f2b1c4e-...",
  "customerId": "9a8b7c6d-...",
  "requestedAt": "2026-06-15T12:00:00Z",
  "ordersInWindow": 12,
  "history": [
    { "returnedAt": "2026-05-02T09:15:00Z", "decision": "AUTO_APPROVED" },
    { "returnedAt": "2026-04-18T17:40:00Z", "decision": "REJECTED" }
  ]
}
```

| Pole | Znaczenie |
|---|---|
| `ordersInWindow` | ile zamówień klient złożył w oknie nadużyć z polityki |
| `history` | wcześniejsze zwroty tego klienta; **wszystkie**, filtrowanie oknem należy do workera |

> **Dlaczego zdarzenie niesie historię, a worker nie sięga do bazy.**
> Dwa serwisy na jednej bazie to sprzęgnięcie, które psuje oba. Zdarzenie niesie
> wszystko, czego konsument potrzebuje, więc worker jest **czystą funkcją** —
> a czystą funkcję da się przetestować bez stawiania czegokolwiek.

## Temat `return.scored`

Producent: `scoring-worker` · Konsument: `returns-service`

```json
{
  "returnId": "3f2b1c4e-...",
  "abuseScore": 0.42
}
```

`abuseScore` jest liczbą z przedziału `[0, 1]`.

## Reguła wyliczenia

Wspólna, tak samo jak procedura decyzyjna:

```
returnsInWindow = liczba zwrotow w history, dla ktorych
                  0 <= (requestedAt - returnedAt) <= abuse.windowDays

abuseScore      = returnsInWindow / max(ordersInWindow, 1)
```

Wynik obcinany do `[0, 1]`. Okno jest **domknięte z obu stron** — zwrot dokładnie
sprzed `windowDays` dni jeszcze się liczy. Zwroty z przyszłości (`returnedAt`
później niż `requestedAt`) są **ignorowane**, nie traktowane jako błąd.

`abuse.windowDays` pochodzi z `return-policy.yaml` — **tego samego pliku**, który
czyta `returns-service`. Polityka jest jedna, czytają ją oba serwisy.

## Jak to testujemy

Tak samo jak decyzje: **tabelą wygenerowaną z polityki**.

```bash
uv run tools/score_cases.py return-policy.yaml
```

Kontrakt zdarzenia sprawdzamy schematem, nie porównaniem pól po kolei —
`docs/contract/events/*.schema.json`.

> Testujemy **schemat i regułę**, nie to, czy Kafka dowiozła. Dowożenie jest
> problemem Kafki i ma własne testy — u jej autorów.
