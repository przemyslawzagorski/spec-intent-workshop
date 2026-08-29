# ADR-003: Wskaźnik nadużyć liczony asynchronicznie

**Status:** przyjęty · **Data:** 2026-08-27

## Kontekst

Wskaźnik nadużyć wymaga przejrzenia historii zwrotów klienta. `POST /returns` musi odpowiedzieć klientowi natychmiast — kontrakt HTTP nie przewiduje czekania.

## Decyzja

`returns-service` publikuje `return.submitted` po zapisaniu **decyzji wstępnej** (`abuseScore = 0`). `scoring-worker` liczy wskaźnik i publikuje `return.scored`. `returns-service` konsumuje wynik i **przelicza decyzję ponownie**.

Transport: Kafka. Po stronie Javy zależność **`quarkus-messaging-kafka`** (SmallRye Reactive Messaging), po stronie Pythona `aiokafka`. Kontrakt zdarzeń: [`docs/contract/events.md`](../contract/events.md).

## Uzasadnienie

**Zdarzenie niesie wszystko, czego konsument potrzebuje** — historię i liczbę zamówień. Worker nie sięga do bazy `returns-service`.

Dwa serwisy na jednej bazie to sprzężenie, które psuje oba: nie da się zmienić schematu bez uzgodnienia, a granica komponentu przestaje istnieć. Skoro i tak wysyłamy zdarzenie, niech niesie dane.

Efekt uboczny jest cenniejszy niż sama niezależność: **worker staje się czystą funkcją**. Da się go przetestować tabelą przypadków bez stawiania bazy, brokera ani drugiego serwisu.

## Konsekwencje

- **Decyzja jest ostatecznie spójna, nie natychmiastowa.** Zapisane wprost w specyfikacji jako założenie **A5**, nie ukryte. `GET /returns/{id}` zwraca zawsze stan aktualny.
- Przeliczenie używa **tej samej** funkcji `EligibilityCheck.resolve`, co pierwsza decyzja. Dlatego jest publiczna. Gdyby istniały dwie funkcje rozstrzygające, rozjechałyby się przy pierwszej zmianie precedencji.
- Nie przechowujemy oryginalnego zgłoszenia. Kody powodów są zapisane, więc przy przeliczeniu wymieniamy tylko te dotyczące nadużyć.
- Oba serwisy czytają **ten sam** `return-policy.yaml`. Polityka jest jedna.

## Odrzucone alternatywy

**Liczenie synchroniczne w `POST`** — najprostsze, ale wiąże czas odpowiedzi z rozmiarem historii klienta. Klient z tysiącem zwrotów czekałby najdłużej, czyli dokładnie ten, którego chcemy sprawdzić.

**Worker czytający bazę `returns-service`** — mniej danych w zdarzeniu, ale sprzęga serwisy schematem i odbiera workerowi testowalność bez infrastruktury.

**Zapisanie całego zgłoszenia, żeby móc je przeliczyć od zera** — więcej stanu do utrzymania po to, żeby odtworzyć coś, co już mamy w postaci kodów powodów.
