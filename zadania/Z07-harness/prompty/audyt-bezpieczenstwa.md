# Prompt: agent jako audytor bezpieczeństwa (★)

To ta sama technika co krytyk z Z08, tylko z **inną rolą**. Rola decyduje o tym,
co agent znajdzie — i to jest cała lekcja tego promptu.

Wklej w katalogu `praca/Z07/spring-petclinic`.

---

Jesteś audytorem bezpieczeństwa. Przejrzyj ten kod i znajdź **konkretne**
podatności. Nie oceniaj stylu, nie proponuj refaktoru, nie chwal.

Sprawdź w tej kolejności:

1. **Wstrzyknięcie SQL i HQL.** Czy każde zapytanie jest parametryzowane?
   Zajrzyj do repozytoriów Spring Data i do wszystkich adnotacji `@Query`.
2. **Wiązanie parametrów formularza.** Co dokładnie robi
   `OwnerController.setAllowedFields`? Które pola encji może nadpisać ktoś,
   kto wyśle spreparowany POST? Sprawdź to, zamiast wnioskować z nazwy metody.
3. **Brak limitów.** Co się stanie przy nazwisku o długości megabajta?
   Przy stronicowaniu ze stroną numer 2 000 000 000? Sprawdź, czy jest
   ograniczenie rozmiaru strony.
4. **Ujawnianie informacji w błędach.** Zajrzyj do `system/CrashController.java`
   i do obsługi wyjątków. Czy komunikat błędu mówi użytkownikowi coś, czego
   nie powinien wiedzieć?
5. **Dane osobowe.** Właściciele mają adres i telefon. Czy trafiają do logów?
   Co jest w `data.sql`?

Dla każdego znaleziska podaj:

- **plik i numer linii**,
- **konkretny scenariusz ataku** — co dokładnie wysyła atakujący i co dostaje,
- **czy to jest realne w tej aplikacji**, czy tylko teoretyczne.

Ostatni punkt jest obowiązkowy. Chcę wiedzieć, co naprawić dziś, a co jest
podręcznikowym zagrożeniem, które w tym kontekście nie ma znaczenia.

Nie pisz „należy stosować dobre praktyki". Nie wymieniaj OWASP Top 10.
Jeśli nie znalazłeś nic w którymś punkcie — napisz „nic nie znalazłem"
i przejdź dalej. To też jest wynik.
