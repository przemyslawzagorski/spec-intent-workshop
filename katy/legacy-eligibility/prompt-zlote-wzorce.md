# Prompt 8.1 — złote wzorce dla legacy

> Dla tych bez skilla `characterization-tests`. Efekt ma być identyczny.

---

W pliku `legacy/ReturnEligibilityService.java` jest
klasa bez testów, której zachowania **nie znam** i której **nie zamierzam czytać
linijka po linijce**.

Zbuduj dla niej siatkę bezpieczeństwa metodą złotych wzorców.

## Krok 1 — powierzchnia

Wypisz **publiczną powierzchnię** tej klasy: metody, ich parametry i to, co
zwracają. Nie opisuj implementacji. Nie proponuj jeszcze refaktoru.

## Krok 2 — bodźce

Zaproponuj zestaw wywołań pokrywający zachowanie, **nie linie kodu**. Musisz
uwzględnić:

- przypadek typowy, który powinien przejść bez uwag,
- **każdą granicę, jaką znajdziesz w kodzie** — dla każdej wartości progowej
  daj trzy wywołania: tuż poniżej, dokładnie na granicy, tuż powyżej,
- kombinacje, w których zadziała więcej niż jedna reguła naraz,
- dane niekompletne: `null`, pusta lista, brak dopasowania SKU.

Przy każdym bodźcu napisz **jednym zdaniem, co sprawdzasz** — nie „test 7",
tylko „zgłoszenie dokładnie w ostatnim dniu okna dla elektroniki".

## Krok 3 — nagranie

Uruchom każdy bodziec i **zapisz to, co faktycznie wyszło**, jako złoty wzorzec.

**Zasada, której nie wolno złamać:** nie poprawiaj wyniku, nawet jeśli wygląda
na błędny. Nie dopisuj komentarza „to chyba bug". Nagrywasz zachowanie
**faktyczne**, nie oczekiwane. Ocena, czy jest poprawne, to osobny krok i należy
do mnie.

Jeśli jakiś wynik wydaje ci się podejrzany — zapisz go tak, jak jest, a osobno,
**pod tabelą**, wypisz listę „obserwacje do sprawdzenia z człowiekiem".

## Krok 4 — test odtwarzający

Napisz jeden test parametryzowany, który odtwarza wszystkie złote wzorce
i porównuje z nagraniem. Ma być zielony **teraz**, przed jakąkolwiek zmianą kodu.

Jeśli nie jest zielony od pierwszego uruchomienia — nagranie jest złe, nie kod.

## Czego NIE robić

- Nie refaktoruj niczego na tym etapie.
- Nie poprawiaj nazw, formatowania ani `TODO`.
- Nie usuwaj `getLastResult()`, nawet jeśli wygląda na martwe. Nie wiesz, kto go używa.
