# Prompt: dokumentacja z kodu, nie z wyobraźni

Wklej agentowi w katalogu `praca/Z06`, mając obok klon petclinica.

---

Napisz dokumentację techniczną projektu, który leży w `spring-petclinic/`.
Ma być zbudowana przez `mkdocs` i przechodzić `mkdocs build --strict`.

**Wszystkie pliki twórz w katalogu bieżącym, NIE w `spring-petclinic/`.**
To jest cudze repozytorium — czytasz je, ale nic w nim nie zapisujesz.
Wygenerowana strona zderzyłaby się z jego własnym buildem.

Utwórz:

1. `mkdocs.yml` — konfiguracja z motywem `material`, nawigacją i tytułem.
2. `docs/index.md` — po co jest ta aplikacja i jak ją uruchomić.
3. `docs/domena.md` — pojęcia i powiązania między nimi.
4. `docs/uruchomienie.md` — jak zbudować, jak odpalić testy, jaka jest baza.
5. `docs/decyzje.md` — decyzje projektowe widoczne w kodzie, wraz z tym,
   co z nich wynika dla kogoś, kto będzie ten kod zmieniał.

**Zasada nadrzędna, ważniejsza od wszystkich pozostałych:**

> **Nie wymyślaj. Cytuj plik.**
>
> Każde zdanie o tym, jak ten system działa, musi mieć w nawiasie ścieżkę do
> pliku, z którego to wynika. Jeśli nie umiesz podać pliku — nie pisz tego zdania.

Dalsze zasady:

- **Nie opisuj powiązań, których nie ma w kodzie.** Zanim napiszesz, że A łączy
  się z B, znajdź pole, adnotację albo kolumnę, która to realizuje. Sprawdź
  też `src/main/resources/db/h2/schema.sql`.
- **Nie kopiuj README upstreamu.** Piszesz dla kogoś, kto ma ten kod zmieniać,
  nie uruchomić.
- **Nie wstawiaj linków do stron, których nie tworzysz.** `--strict` wywali build.
- Wszystkie komendy, które podajesz, muszą działać. Sprawdź je w `pom.xml`.
- **Komendy muszą działać dla kogoś, kto ma tylko `spring-petclinic/`** —
  bez naszego warsztatu i bez jego skrótów. Pisz `./mvnw` albo `mvn`,
  nigdy aliasów ani funkcji z otoczenia, w którym akurat siedzisz.
  Jeśli u ciebie `mvn` wymaga dodatkowych flag przez firmowe proxy —
  **to jest twoje środowisko, nie właściwość tego projektu**, i nie ma tego
  w dokumentacji.
- Po polsku. Zwięźle. Żadnej strony na temat tego, czym jest Spring Boot.

Na koniec, w osobnym pliku `ZMYSLONE.md`, wypisz:

**a)** zdania, które napisałeś, mimo że nie znalazłeś dla nich pliku,
**b)** rzeczy, których szukałeś i nie znalazłeś, a spodziewałeś się ich.

Ta lista ma nie być pusta. Jeśli twierdzisz, że jest, to znaczy, że nie
sprawdzałeś.
