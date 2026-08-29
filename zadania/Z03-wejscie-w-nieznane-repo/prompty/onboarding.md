# Prompt: wejście w nieznane repo

Wklej agentowi w katalogu `praca/Z03/spring-petclinic`.

Prompt jest szczegółowy celowo. Ma działać także na modelu średniej klasy —
i ma wymuszać, żeby agent **przyznał się, czego nie wie**, zamiast zgadywać.

---

Przeczytaj `src/main/java` w tym repozytorium i zbuduj słownik domeny.

Nie chcę streszczenia architektury ani opisu warstw. Chcę **wspólny język** —
listę pojęć, których będziemy używać w rozmowach, specyfikacji i testach.

Zanim zaczniesz pisać plik, wypisz mi:

1. listę pojęć, które znalazłeś, w kolejności od najważniejszego,
2. **listę powiązań między pojęciami, których szukałeś i NIE znalazłeś** —
   to jest dla mnie ważniejsze niż to, co znalazłeś.

Potem napisz `CONTEXT.md` w takim układzie:

**1 · Pojęcia.** Każde jednym–dwoma zdaniami, po polsku, z nazwą klasy w kodzie
w nawiasie. Przy każdym napisz, gdzie te dane powstają i kto je zmienia.

**2 · Czego tu nie ma.** Pojęcia, których ktoś znający tę domenę spodziewałby się
w takim systemie, a w kodzie ich nie ma. To jest osobna sekcja i ma być
konkretna: nazwa pojęcia plus zdanie, dlaczego uważasz, że go brakuje.

**3 · Niespodzianki.** Rzeczy, które w kodzie są inaczej, niż podpowiada intuicja.
Przy każdej podaj plik i numer linii.

Twarde zasady:

- **Każde pojęcie musi mieć oparcie w konkretnym pliku.** Podaj ścieżkę.
- **Nie wymyślaj powiązań między klasami.** Jeśli piszesz, że A wiąże się z B,
  to musisz umieć wskazać pole, adnotację albo kolumnę, która to realizuje.
  Jeśli nie umiesz — pisz o tym w sekcji „Czego tu nie ma".
- Nie opisuj wzorców architektonicznych. To słownik, nie dokumentacja.
- Maksymalnie 70 linii.

Na koniec, osobno pod plikiem, wypisz mi: **co napisałeś na podstawie kodu,
a co na podstawie tego, że tak zwykle bywa w takich systemach.**
