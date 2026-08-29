---
name: zlote-wzorce
description: Zakłada siatkę bezpieczeństwa pod kodem bez testów, zanim zaczniesz
             refaktor. Użyj, gdy ktoś chce zmienić strukturę legacy, mówi
             „nie ma testów", albo prosi o refaktor kodu, którego nie rozumie.
---

Zanim cokolwiek zmienisz w tym kodzie, budujemy siatkę: nagrywamy, co kod
**robi teraz**, żeby po refaktorze dało się sprawdzić, czy robi to samo.

To nie są testy jednostkowe. To zdjęcie stanu obecnego.

## Zasada nadrzędna

**Nagrywasz zachowanie, nie oczekiwania.**

Nie zapisujesz, co kod *powinien* robić. Zapisujesz, co *robi* — razem z błędami,
dziwnymi wartościami i wszystkim, co ci się nie podoba. Jeśli poprawisz nagranie,
żeby było „poprawne", siatka przestaje działać, zanim ją założysz.

## Krok 1 — znajdź granicę

Wskaż jedną funkcję albo klasę, która ma **czyste wejście i czyste wyjście**:
dane wchodzą argumentem, wynik wychodzi wartością zwracaną.

Jeśli kod sięga po zegar, bazę albo sieć — **najpierw wydziel te zależności
za parametr**, minimalną zmianą, bez ruszania logiki. To jedyna zmiana, jaką
wolno zrobić przed nagraniem.

## Krok 2 — wygeneruj przypadki

Wypisz zestaw wejść, który pokrywa:

- **każdą gałąź** w kodzie (przeczytaj warunki, nie zgaduj),
- **granice każdego przedziału** — wartość przed, dokładnie na granicy, i za nią,
- **wartości puste i zerowe** — `null`, pusta kolekcja, zero, wartość ujemna,
- **kombinacje, przy których zadziała kilka reguł naraz.**

Rozjazd po refaktorze prawie zawsze pojawia się **na granicy**. Jeśli masz
w kodzie `>=`, nagraj przypadki dla `n-1`, `n` i `n+1`.

## Krok 3 — nagraj

Napisz krótki program, który przepuszcza wszystkie przypadki przez kod
i **zapisuje wyniki do pliku TSV** — jeden wiersz na przypadek, z identyfikatorem.

Format tabelaryczny, nie proza: tabelę da się porównać maszynowo i jest tania
w tokenach.

## Krok 4 — napisz odtwarzacz

Drugi program: czyta plik, uruchamia kod jeszcze raz, porównuje.
Przy rozjeździe **wypisuje: identyfikator, wartość nagraną, wartość faktyczną**
i kończy się kodem błędu.

Sprawdź go w obie strony: ma przechodzić na nietkniętym kodzie i ma się zapalić,
gdy celowo zmienisz jedną wartość graniczną.

## Krok 5 — dopiero teraz refaktor

Po każdej zmianie odtwarzaj wzorce.

**Rozjazd zatrzymuje pracę.** Nie „przeanalizuj rozjazd" — zatrzymaj się
i powiedz człowiekowi:

- który przypadek się rozjechał,
- co było nagrane, a co wyszło,
- która zmiana to spowodowała.

**Nie rozstrzygaj sam, czy to był błąd, czy reguła biznesowa.** Tego nie da się
wyczytać z kodu — odpowiedź jest u kogoś w firmie. Siatka nie mówi, co jest dobre.
Mówi, **co się zmieniło**.

## Czego nie wolno

- **Nie poprawiaj nagrania, żeby refaktor przeszedł.** To jest jedyny sposób,
  żeby całkowicie zmarnować tę technikę.
- **Nie „naprawiaj przy okazji" błędów, które zobaczysz w nagraniach.**
  Zgłoś je osobno. Refaktor to zmiana struktury bez zmiany zachowania —
  naprawa błędu to zmiana zachowania i należy do osobnego commita.
