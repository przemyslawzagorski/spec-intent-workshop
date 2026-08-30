# Z08 · Pomiar — cztery przebiegi, protokół i liczby

Ten moduł twierdzi, że coś zmierzyliśmy. **Twierdzenie bez dowodu jest dokładnie
tym, przed czym to zadanie ostrzega**, więc tutaj jest komplet: co dokładnie
zrobiliśmy, na czym, i czego z tego nie wolno wywnioskować.

Jeśli po przeczytaniu uznasz, że pomiar jest za słaby, żeby coś na nim opierać —
masz rację co do połowy wniosków. Która połowa, jest niżej.

## Po co to w ogóle mierzyliśmy

Pierwsza wersja tego modułu głosiła tezę:

> Recenzent, który zna specyfikację, przestaje szukać błędów — zaczyna bronić
> intencji. Dlatego osie trzeba rozdzielić: krytyk dostaje **sam kod**.

Brzmi rozsądnie, pasuje do zasady z Buna (*„its context: only the diff"*)
i **jest fałszywa** dla tego modelu i tego kodu. Sprawdziliśmy, bo materiał
warsztatowy nie może żyć z niesprawdzonej tezy.

## Protokół

| | |
|---|---|
| **Kiedy** | 30 sierpnia 2026 |
| **Model** | `claude-opus-5`, ustawienia domyślne, **ten sam we wszystkich czterech ramionach** |
| **Wejście** | `handouts/3-przyklad-do-roastu/PrzypomnienieOWizycie.java` (65 linii) oraz `SPEC.md` (5 wymagań) — pliki bez zmian, dokładnie te, które dostajesz w `praca/Z08` |
| **Sesje** | cztery niezależne, każda w osobnym oknie kontekstu, bez historii i bez dostępu do reszty repo |
| **Powtórzenia** | **jedno na ramię.** Nie cztery, nie dziesięć — jedno |
| **Liczenie** | znalezisko liczymy jako trafione, gdy raport wskazuje miejsce **i** podaje warunek, przy którym to pęka. Sama wzmianka („obsługa błędów mogłaby być lepsza") nie liczy się |
| **Odniesienie** | lista znalezisk z [KLUCZ.md](KLUCZ.md), część 3. **Przed przebiegami miała sześć pozycji, po nich dziewięć** — patrz niżej, to jest najpoważniejsze ograniczenie tego pomiaru |

### Cztery ramiona

| | Prompt | Kontekst | Co to izoluje |
|---|---|---|---|
| **A** | `prompty/krytyk.md` | sam kod | punkt odniesienia |
| **B** | `prompty/roast-obietnicy.md` | kod **+** SPEC | „jak ma być" — druga oś w pełnej postaci |
| **C** | „przejrzyj ten kod" | kod **+** SPEC | co daje sama specyfikacja bez ramy adwersaryjnej |
| **D** | `prompty/krytyk.md` | kod **+** SPEC | **czy dodanie specyfikacji psuje krytyka** |

## Wyniki

| Znalezisko | A | B | C | D |
|---|---|---|---|---|
| W4 nie istnieje | ✔ | ✔ | ✔ | ✔ (poz. 1) |
| reguła weekendowa | ✔ | ✔ | ✔ | ✔ (poz. 2) |
| `catch` znaczy jako wysłane | ✔ | ✔ | ✔ | ✔ |
| W5 nietrwałe po restarcie | ✔ | ✔ | ✔ | ✔ |
| `wyslane` rośnie / wątki | ✔ | ✔ | ✔ | ✔ |
| pusty e-mail omija W3 | — | ✔ | ✔ | ✔ |
| strefa czasowa na sztywno | — | ✔ | ✔ | — |
| „za 1 dni" | ✔ | ✔ | ✔ | — |
| NPE wywraca cały przebieg | ✔ | — | — | ✔ |
| **sekcja „poza zakresem" potwierdzona** | — | **✔** | — | — |
| **tabela pokrycia wymagań** | — | **✔** | — | — |
| **razem z dziewięciu wierszy** | 7 | 8 | 8 | 7 |

**Dwie rzeczy do tej tabeli, żeby nikt jej nie przecenił:**

Wiersze **nie odpowiadają jeden do jednego** dziewięciu znaleziskom z `KLUCZ.md`.
Znalezisko 4 (stan w pamięci) rozpada się tu na dwa wiersze — nietrwałość po
restarcie i nieograniczony wzrost zbioru — bo raporty traktowały je osobno.

**Znaleziska 7 z klucza — braku nadrabiania po przestoju — nie zgłosiło żadne
ramię.** Nie ma go w tabeli, bo wszędzie stałaby kreska. Warto o tym pamiętać,
zanim uzna się tabelę pokrycia za komplet: **cztery przeglądy, w tym jeden
przechodzący wymaganie po wymaganiu, i wszystkie cztery to przeoczyły.**

Czas jednego przebiegu: A 108 s, B 104 s, C 92 s, D 124 s. Podajemy dla porządku —
**przy jednym powtórzeniu to nie jest porównanie wydajności**, tylko rząd wielkości.

## Co z tego wolno wnioskować

Dwie pary różnią się **jedną** rzeczą i tylko z nich wyciągamy wnioski
przyczynowe.

**A kontra D — ten sam prompt, jedyna różnica to dodana specyfikacja.**
D znalazło tyle samo co A i ustawiło pominięte wymaganie oraz dopisaną regułę
na **pozycjach 1 i 2**. Nie zaczęło bronić intencji, nie przestało szukać błędów.

> **Teza „specyfikacja psuje krytyka" jest obalona** dla tego modelu i tego kodu.
> Wyleciała z materiału.

**B kontra C — ten sam kontekst, jedyna różnica to prompt.**
Liczba trafień identyczna (8). Różni się **kształt wyjścia**: B oddało tabelę
z wierszem na każde wymaganie, numerem linii i werdyktem, plus osobną tabelę
zachowań niezamówionych, i jako **jedyne ramię potwierdziło sekcję „poza
zakresem"** (brak SMS-ów, brak przypomnień po terminie). C oddało prozę
uporządkowaną po wadze.

> **W tej parze prompt nie zmienił, ile model znalazł. Zmienił, czy da się
> sprawdzić, czego nie sprawdził.**

To jest wynik, który warto zapamiętać, bo jest niewygodny: **naiwne „przejrzyj
ten kod" ze specyfikacją wypadło w liczbie znalezisk tak samo jak nasz starannie
napisany roast.** Jeśli szukasz uzasadnienia dla lepszych promptów w postaci
„znajdą więcej błędów" — tutaj go nie ma. Uzasadnieniem jest kształt wyniku,
nie jego rozmiar.

**Czego z tego pomiaru NIE wolno wyciągnąć: że oś B jest lepsza od osi A.**
A i B różnią się jednocześnie promptem, celem, kontekstem i formatem odpowiedzi —
cztery zmienne naraz. To jest **demonstracja dwóch rodzajów raportu**, nie dowód
przewagi. Krok 3 w zadaniu jest właśnie taką demonstracją i tak jest opisany.

## Czego ten pomiar nie dowodzi

Uczciwie, punkt po punkcie:

- **Lista odniesienia rosła w trakcie — i to jest najpoważniejsza wada.**
  Zanim puściliśmy przebiegi, mieliśmy w kluczu **sześć** znalezisk. Agenty
  pokazały trzy dalsze (NPE wywracający cały przebieg, brak nadrabiania po
  przestoju, strefa czasowa na sztywno). Sprawdziliśmy je w kodzie, uznaliśmy
  za słuszne i **dopisaliśmy do listy — już po pomiarze**.

  Konsekwencja jest konkretna: mianownik „z dziewięciu" częściowo pochodzi
  z samych raportów, więc **liczniki są zawyżone względem uczciwego testu
  ślepego**. Gdyby ramię wykryło coś, czego nie ma w kluczu, mogłoby to
  wyglądać na jego przewagę, a nie na naszą lukę. Wniosek jakościowy
  (tabela kontra proza) to nie dotyka. Każde porównanie liczb — dotyka.
- **Jedno powtórzenie na ramię.** Modele są niedeterministyczne. Różnica jednego
  znaleziska między ramionami mieści się w szumie i **nie należy jej czytać**.
  Trzymamy się dwóch rodzajów wniosków: jakościowych (tabela kontra proza)
  i takich, gdzie spodziewaliśmy się dużej różnicy, a nie było jej wcale
  (ramię A kontra D).
- **Jeden model.** Nie wiemy, czy słabszy model zachowa się tak samo.
  Podejrzewamy, że nie — i to jest dobry ★ do zrobienia u siebie.
- **Jeden przykład, i to zasadzony.** Dziewięć błędów w 65 liniach.
  W prawdziwym kodzie gęstość jest o rząd wielkości niższa, a wtedy różnica
  między „znalazł 7" a „znalazł 8" znaczy co innego.
- **Jeden język i jedna dziedzina.**
- **Liczyliśmy sami, nie na ślepo.** Znaliśmy listę odniesienia, przypisując
  trafienia. Przy jednoznacznych znaleziskach to mało zmienia, ale to nie jest
  ocena zaślepiona.

**Co to znaczy w praktyce:** wniosek „specyfikacja nie psuje krytyka" jest mocny
(różnica byłaby duża, gdyby teza była prawdziwa — nie było jej wcale). Wniosek
o kształcie raportu jest mocny, bo to różnica jakościowa, nie liczbowa. Wszystko,
co wymagałoby porównywania liczb 7 kontra 8, jest **poza zasięgiem tego pomiaru**.

## Fałszywe alarmy — dwa, oba warte pokazania

- **Ramię B wyprodukowało wymaganie, którego nie ma.** Wpisało do tabeli
  „K1 — zadanie uruchamia się raz na dobę, rano" i orzekło BRAK. Zdanie pochodzi
  z sekcji *Kontekst* w `SPEC.md`, a harmonogramu w tym pliku nie ma **z definicji**.
  To jest szum wygenerowany przez samą dyscyplinę tabeli — cena za kompletność.

  > **Po tym pomiarze dopisaliśmy do promptu zabezpieczenie** („tło i kontekst
  > nie są wymaganiami; przy wątpliwości wymień osobno pod tabelą"). Znaczy to,
  > że **ramię B mierzyliśmy wersją bez tej reguły** — dzisiejszy
  > `prompty/roast-obietnicy.md` nie jest już dokładnie tym, co puszczaliśmy.
  > Piszemy to, bo pomiar bez takiej informacji jest wart mniej, niż się wydaje.
  > Czy zabezpieczenie działa — nie sprawdziliśmy ponownie.
- **Ramię B złamało własny zakaz.** Skill mówi wprost „nie zgłaszaj błędów",
  a raport wsadził połknięty `catch` do tabeli „zachowania niezamówione".
  **Skill to prośba, nie mechanizm** — ta sama lekcja co w Z01 i Z07.

## Jak to powtórzyć u siebie

Cztery sesje, ten sam plik, protokół powyżej. Najtańsza wersja to dwie sesje:
**A i D** — ten sam prompt krytyka, raz bez `SPEC.md`, raz z nim. To jedyna para,
która testuje tezę tego modułu, i dlatego jest w ★ jako „Powtórz nasz pomiar".

Jeśli u ciebie wyjdzie inaczej — to jest wynik i chcemy o nim usłyszeć.
