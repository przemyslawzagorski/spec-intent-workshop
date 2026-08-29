---
name: dokumentacja-repo
description: Pisze dokumentację techniczną repozytorium na podstawie kodu, bez
             zmyślania. Użyj, gdy ktoś prosi o dokumentację projektu, README dla
             nowych osób, opis domeny albo opis decyzji architektonicznych.
---

Piszesz dokumentację dla kogoś, kto ma **ten kod zmieniać** — nie uruchomić
i nie ocenić. Zakładaj, że czytelnik jest dobrym inżynierem i nie zna tego repo.

## Zasada nadrzędna

**Nie wymyślaj. Cytuj plik.**

Każde zdanie o tym, jak system działa, musi mieć w nawiasie ścieżkę do pliku,
z którego to wynika. Nie umiesz podać pliku — nie piszesz tego zdania.

Zanim napiszesz, że A łączy się z B, znajdź pole, adnotację albo kolumnę, która
to realizuje. Sprawdź też schemat bazy — model w kodzie i model w bazie potrafią
się różnić.

## Cztery rodzaje tekstu, których nie wolno mieszać

To jest podział z [Diátaxis](https://diataxis.fr). Większość złej dokumentacji
bierze się z tego, że jeden dokument próbuje robić wszystkie cztery rzeczy naraz.

| Rodzaj | Odpowiada na pytanie | Czytelnik |
|---|---|---|
| **Tutorial** | „jak zacząć?" | nie zna niczego, chce sukcesu w 15 minut |
| **How-to** | „jak zrobić X?" | zna projekt, ma konkretne zadanie |
| **Reference** | „jakie są parametry Y?" | szuka faktu, nie chce narracji |
| **Explanation** | „dlaczego tak?" | rozumie jak, chce wiedzieć czemu |

Przy każdej stronie, którą piszesz, **nazwij sobie, który to rodzaj**, i trzymaj
się go. Strona „Uruchomienie" jest how-to i nie tłumaczy, czym jest Spring.
Strona „Decyzje" jest explanation i nie zawiera komend.

## Co ma powstać

1. **`index.md`** — po co ten projekt istnieje i co przeczytać najpierw.
   Maksymalnie jeden ekran. Tabela: plik → po co tam patrzeć.
2. **`domena.md`** — pojęcia i powiązania. Przy każdym pojęciu: nazwa klasy
   w kodzie, skąd biorą się dane, kto je zmienia.
   **Obowiązkowa sekcja „Czego tu nie ma"** — pojęcia, których ktoś znający tę
   domenę by się spodziewał, a w kodzie ich nie ma.
3. **`uruchomienie.md`** — komendy, które naprawdę działają, z czasem wykonania
   przy każdej. Sprawdź je w pliku budowania, nie zgaduj.
4. **`decyzje.md`** — rzeczy, które ktoś kiedyś rozstrzygnął i nigdzie nie zapisał.
   Przy każdej: **co z tego wynika dla kogoś, kto będzie ten kod zmieniał.**

## Czego nie robić

- **Nie opisuj warstw i wzorców.** „Aplikacja stosuje MVC" nie pomaga nikomu
  zmienić kodu.
- **Nie tłumacz frameworka.** Od tego jest dokumentacja frameworka.
- **Nie kopiuj README upstreamu.**
- **Nie pisz „powinien", „można", „warto".** Albo jest, albo nie ma.
- **Nie linkuj do stron, których nie tworzysz.** Zepsuty link to zepsuty build.

## Na koniec — obowiązkowo

Osobny plik `ZMYSLONE.md`:

**a)** zdania, które napisałeś, mimo że nie znalazłeś dla nich pliku,
**b)** rzeczy, których szukałeś i nie znalazłeś, a spodziewałeś się ich.

**Ta lista nie może być pusta.** Jeśli twierdzisz, że jest — nie sprawdzałeś.

## Sprawdzenie

Dokumentacja ma przechodzić `mkdocs build --strict`. Ostrzeżenia są błędami:
link do nieistniejącej strony przerywa build.
