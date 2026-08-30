# Legacy: co znalazłem i co z tym zrobiłem

## Znalezisko

`ReturnEligibilityService.check()`, warunek okna zwrotu:

```java
if (days >= window) {
    reasons.add("WINDOW_EXPIRED");
}
```

Kontrakt (`harness-template/docs/contract/decision-procedure.md`, sekcja
*Granice — czytaj uważnie*) wymaga `>`, i mówi wprost, że `>=` to najczęstszy
błąd graniczny w tym miejscu.

**Skutek:** zwrot zgłoszony **dokładnie w ostatnim dniu okna** jest odrzucany.
Klient, który zdążył w czternastym dniu, dostaje odmowę.

To ta sama klasa błędu co fragment B z M6 — przesunięcie o jeden na granicy.

## Trzy pytania

**1. Czy to na pewno błąd, a nie decyzja sprzed pięciu lat?**
Nie ma ADR, nie ma komentarza, nie ma testu. `git log` na tej linii pokazuje
commit „fix window check" bez opisu. **Przyjmuję, że to błąd.**

**2. Ilu klientów dotknęło i w którą stronę?**
Zawsze na niekorzyść klienta. Skala do sprawdzenia w danych — poza zakresem
warsztatu, ale **w prawdziwej pracy to jest pytanie przed naprawą, nie po**.

**3. Czy naprawa jest zmianą kontraktu?**
**Tak.** Zachowanie widoczne dla klienta się zmienia. Wymaga uzgodnienia
z biznesem, nie decyzji programisty.

## Decyzja

**Zamrażam, nie naprawiam w tym kroku.**

Złoty wzorzec `L02` zostaje **z zachowaniem błędnym** i z jawnym komentarzem,
że utrwala znany defekt. Refaktor przeprowadzam za tą siatką —
zachowanie się nie zmienia.

Naprawa idzie osobnym zgłoszeniem, z decyzją biznesu i zmianą złotego wzorca
jako **świadomą zmianą kontraktu**.

## Obserwacje do sprawdzenia z człowiekiem

Prompt każe wypisać osobno to, co wygląda podejrzanie, ale czego **nie
rozstrzygam sam**. Z nagrania wyszły trzy takie rzeczy i żadnej nie ruszam:

| Wzorzec | Co robi kod | Pytanie do zadania |
|---|---|---|
| `L16` | zgłoszenie **bez żadnej pozycji** → `AUTO_APPROVED`, zwrot `0` | czy pusty wniosek ma być w ogóle przyjmowany? |
| `L17` | SKU, **którego nie ma w zamówieniu** → `AUTO_APPROVED`, zwrot `0` | czy ktoś może zgłosić zwrot rzeczy, której nie kupił? |
| `L14`, `L15` | brak danych → `REJECTED`, ale kwota zwrotu to **`null`**, a nie `0` | czy coś w dole systemu robi na tym arytmetykę? |

Żadna z nich nie jest „błędem", dopóki ktoś nie powie, jak ma być. Wszystkie
trzy są **nagrane takie, jakie są** — bo gdyby nie były, refaktor mógłby je
po cichu zmienić i nikt by się nie dowiedział.

`L17` jest tu najciekawszy: to nie wygląda na przeoczenie w jednej linii, tylko
na skutek uboczny pętli, która nie ma czego dopasować i po prostu nic nie robi.
**Takich rzeczy nie znajduje się czytaniem kodu — znajduje się je nagrywaniem.**

> Agent wykrył to w trzydzieści sekund i naprawiłby w kolejne trzydzieści.
> Nie wiedziałby, że dział prawny oparł na tym zachowaniu zapis w regulaminie.
> **Wykrycie jest maszynowe. Decyzja nie jest.**
