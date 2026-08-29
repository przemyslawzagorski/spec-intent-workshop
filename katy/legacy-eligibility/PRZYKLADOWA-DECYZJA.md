# Legacy: co znalazłem i co z tym zrobiłem

## Znalezisko

`ReturnEligibilityService.check()`, warunek okna zwrotu:

```java
if (days >= window) {
    reasons.add("WINDOW_EXPIRED");
}
```

Kontrakt (`docs/contract/decision-procedure.md`, sekcja *Granice*) wymaga `>`.

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

Złoty wzorzec `W_boundary_exact` zostaje **z zachowaniem błędnym** i z jawnym
komentarzem, że utrwala znany defekt. Refaktor przeprowadzam za tą siatką —
zachowanie się nie zmienia.

Naprawa idzie osobnym zgłoszeniem, z decyzją biznesu i zmianą złotego wzorca
jako **świadomą zmianą kontraktu**.

> Agent wykrył to w trzydzieści sekund i naprawiłby w kolejne trzydzieści.
> Nie wiedziałby, że dział prawny oparł na tym zachowaniu zapis w regulaminie.
> **Wykrycie jest maszynowe. Decyzja nie jest.**
