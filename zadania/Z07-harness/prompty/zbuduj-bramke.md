# Prompt: zbuduj bramkę dla tego repo

Wklej agentowi w katalogu `praca/Z07/spring-petclinic`.

---

Napisz skrypt `./bramka` — jedną komendę, która mówi, czy to repozytorium jest
zdrowe. Ten sam skrypt ma być wołany lokalnie i przez CI.

Zanim zaczniesz pisać, sprawdź w repo i podaj mi:

1. jakie polecenie uruchamia testy i ile ich jest,
2. czy build ma bramkę formatowania lub statycznej analizy i ile trwa,
3. ile trwa pełny `mvn test` na rozgrzanym `~/.m2`,
4. czy w `src/test` są jakieś testy wyłączone i czym.

**Nie zgaduj żadnej z tych czterech rzeczy.** Sprawdź w plikach i zmierz.

Skrypt ma rozróżniać dwie kategorie:

- **HARD** — nie wpuszczam. Kod wyjścia niezerowy, CI staje.
- **SOFT** — zwracam uwagę, ale przepuszczam.

Do HARD daj rzeczy, których złamanie znaczy „to jest zepsute".
Do SOFT rzeczy, które znaczą „to wygląda podejrzanie, spójrz".

Twarde zasady:

- **Najtańsze sprawdzenia najpierw.** Jeśli formatowanie kosztuje 6 sekund,
  a testy 84, to nie ma sensu czekać na testy, żeby dowiedzieć się, że i tak
  wywali się na formacie.
- **Każdy komunikat błędu ma mówić, co zrobić.** Nie „bramka zamknięta", tylko
  „bramka zamknięta, uruchom `mvn spring-javaformat:apply`".
- **W repo jest `tools/repo_policy.py`.** Uruchom `uv run tools/repo_policy.py .`
  i wepnij to jako sprawdzenie HARD. Przeczytaj wcześniej jego docstring —
  zobaczysz, co pilnuje i dlaczego.
- **Nie dodawaj reguł, których nie umiesz uzasadnić.** Bramka, która krzyczy
  przy każdym uruchomieniu, przestaje być czytana.
- Skrypt ma działać uruchomiony z dowolnego katalogu.

Po napisaniu **przetestuj go w obie strony**: pokaż mi, że przechodzi na czystym
repo, i pokaż, że się zamyka, gdy coś zepsuję.

Na koniec wypisz osobno: **które reguły dałeś do HARD, a które do SOFT i dlaczego**.
Chcę zobaczyć uzasadnienie każdej decyzji, bo to jest właściwa treść tego zadania.
