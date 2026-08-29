# Procedura decyzyjna — część wspólnego kontraktu

## Po co to jest

Każdy uczestnik dostaje **inną kartę domenową**, więc każdy ma inne progi, okna
i wyjątki. Gdyby różniła się też *procedura*, nie dałoby się napisać jednej bramki —
a bez bramki nie ma warsztatu.

Dlatego dzielimy to na dwie warstwy:

| | Gdzie mieszka | Kto ustala | Przykład |
|---|---|---|---|
| **Kontrakt** | ten plik + `openapi.yaml` | my, raz | „REJECTED wygrywa z MANUAL_REVIEW" |
| **Polityka** | `return-policy.yaml` | ty | „okno to 30 dni dla elektroniki" |

Procedura poniżej jest **sparametryzowana polityką**. Ty dostarczasz stałe, ona
mówi, co z nimi zrobić. Dzięki temu `tools/policy-cases` potrafi wygenerować
tabelę oczekiwanych decyzji z *twojego* pliku i sprawdzić *twoją* implementację.

## Kroki

Wykonywane w tej kolejności. **Każdy krok, który zadziała, dokłada swój kod do
`reasonCodes`** — nawet jeśli decyzja została już przesądzona wcześniej.

```
0.  Zamówienie nie istnieje                    → 404 (nie ReturnDecision)
1.  Któraś pozycja w kategorii wykluczonej     → dodaj CATEGORY_EXCLUDED
2.  Zgłoszenie po oknie zwrotu dla kategorii   → dodaj WINDOW_EXPIRED
3.  Zwrot częściowy, a polityka go zabrania    → dodaj PARTIAL_NOT_ALLOWED
4.  abuseScore >= abuse.rejectAt               → dodaj ABUSE_SUSPECTED
5.  abuseScore >= abuse.reviewAt               → dodaj ABUSE_BORDERLINE
6.  Kwota > manualReviewAboveAmount            → dodaj AMOUNT_ABOVE_THRESHOLD
7.  Żaden z powyższych                         → dodaj WITHIN_POLICY
```

## Rozstrzygnięcie

```
REJECTED        jeśli zadziałał którykolwiek z:
                CATEGORY_EXCLUDED, WINDOW_EXPIRED,
                PARTIAL_NOT_ALLOWED, ABUSE_SUSPECTED

MANUAL_REVIEW   w przeciwnym razie, jeśli zadziałał którykolwiek z:
                ABUSE_BORDERLINE, AMOUNT_ABOVE_THRESHOLD

AUTO_APPROVED   w przeciwnym razie
```

**Precedencja jest twarda: REJECTED > MANUAL_REVIEW > AUTO_APPROVED.**

## Granice — czytaj uważnie

To jest miejsce, w którym najczęściej powstaje błąd, i to jest celowe.

- **Okno zwrotu jest domknięte.** Zgłoszenie dokładnie w `windowDays`-tym dniu
  jest **w oknie**. Wygasa dopiero od `windowDays + 1`.
  Formalnie: `WINDOW_EXPIRED  ⟺  daysBetween(deliveredAt, requestedAt) > windowDays`.
  Nie `>=`. To jest najczęstszy błąd graniczny, jaki popełnia i człowiek,
  i agent — dlatego jest tu wypisany wprost, a nie zostawiony do domysłu.
- **Dzień liczymy jako pełne 24h w UTC**, od `deliveredAt` do `requestedAt`.
  Bez dni roboczych, bez stref lokalnych.
- **Progi kwotowe są ostre.** `AMOUNT_ABOVE_THRESHOLD ⟺ refundAmount > próg`.
  Kwota równa progowi **nie** wywołuje review.
- **Progi abuse są domknięte.** `>=`, nie `>`. Tak, celowo odwrotnie niż kwota —
  bo w prawdziwych systemach też nie jest to spójne, a spec ma być czytana,
  nie zgadywana.

## Wyliczenie kwoty

`refundAmount` = suma `unitPrice × quantity` po pozycjach zwracanych.
Dla `REJECTED` wynosi `0`.

## Kto płaci za przesyłkę

Z polityki: `shipping.paidBy.<powód>`. Jeśli pozycje mają różne powody,
wygrywa ten korzystniejszy dla klienta (`MERCHANT` bije `CUSTOMER`).
Dla `REJECTED` pole jest pomijane.

## abuseScore

Liczony przez `scoring-worker`, asynchronicznie — patrz `docs/adr/003-szew-asynchroniczny.md`.
Gdy wskaźnika jeszcze nie ma, serwis Javy używa `0`. To założenie jest zapisane
wprost w specyfikacji, a nie ukryte w kodzie.

```
abuseScore = returnsInWindow / max(ordersInWindow, 1)
```

gdzie okno to `abuse.windowDays` z polityki. Wartość z zakresu `[0, 1]`.
