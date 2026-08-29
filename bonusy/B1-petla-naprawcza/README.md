# B1 · Pętla naprawcza i warunki stopu

**40 min** · dowolne repo z buildem · bonus

## O co chodzi

„Agent sam poprawił błędy kompilacji" brzmi jak magia. Nie jest. To pętla:
uruchom build, przeczytaj błąd, popraw, powtórz. Cała sztuka jest w tym,
**kiedy ta pętla ma się zatrzymać** — bo sama z siebie się nie zatrzyma.

## Jak zwykle to robimy

„Napraw, żeby przechodziło." Agent iteruje. Po dziesięciu minutach testy są
zielone. Patrzysz w diff i widzisz, że w czwartej iteracji dodał zależność,
w szóstej złagodził asercję, a w ósmej dopisał `@Disabled`.

Nie oszukał cię. **Dostał kryterium „ma przechodzić" i je spełnił.**

Boli, bo:

- **Kryterium było źle postawione.** „Ma przechodzić" jest spełnialne na wiele
  sposobów i tylko jeden z nich to ten, o który ci chodziło.
- **Nie widzisz przebiegu.** Widzisz wynik po dziesięciu iteracjach.
- **Nie ma warunku stopu**, więc pętla kończy się dopiero, gdy przypadkiem trafi.

## Jak działa pętla naprawcza

Trzy rzeczy i nic więcej:

1. **Komenda**, która daje jednoznaczny wynik — `mvn test`, `cargo check`,
   `./bramka`. Kod wyjścia i tekst błędu.
2. **Odczyt błędu** i zmiana w kodzie.
3. **Powtórzenie**, aż komenda przejdzie albo skończy się limit.

Punkt pierwszy decyduje o wszystkim. **Im precyzyjniejszy komunikat błędu,
tym krótsza pętla.** Kompilator, który mówi „oczekiwano `&str`, dostałem
`String` w linii 42", zamyka pętlę w jednej iteracji. Test, który mówi
„expected true but was false", może nie zamknąć jej wcale.

> **„Compiler errors are a better feedback loop than a style guide."**
> — z opisu przepisania Buna z Ziga na Rusta

Przy tym przepisaniu jedna faza wygenerowała **około 16 000 błędów kompilacji**.
Dla człowieka to katastrofa. Dla pętli to po prostu 16 000 precyzyjnych,
maszynowo czytelnych wskazówek — i o to chodziło w wyborze Rusta.

## Warunki stopu, które trzeba znać

| Warunek | Po co | Jak go postawić |
|---|---|---|
| **Limit iteracji** | żeby pętla się skończyła | „masz trzy podejścia, potem się zatrzymaj i powiedz mi, co jest nie tak" |
| **Zatrzymaj się i zapytaj** | decyzje, których nie wolno podejmować po cichu | „jeśli poprawka wymaga zmiany kontraktu — zapytaj mnie" |
| **Zakaz zmiany testów** | najczęstsza droga na skróty | reguła w pliku **plus** odcisk z Z07 |
| **Zakaz nowych zależności** | druga najczęstsza | reguła plus sprawdzenie w bramce |
| **Zakaz wyłączania** | `@Disabled`, `skip`, `xfail` | reguła plus grep w bramce |

Zwróć uwagę: **przy każdym warunku są dwie kolumny — prośba i egzekucja.**
Prośba wystarcza w większości przypadków. Egzekucja jest po to, żeby nie
polegać na „w większości".

## Zrób to

Weź dowolne repo z buildem — petclinic z Z07 nadaje się doskonale, bo ma
już odcisk i bramkę.

**1 · Zepsuj coś naprawdę** (10 min). Zmień sygnaturę metody używanej w kilku
miejscach. Poproś agenta: *„napraw, żeby `mvn test` przechodziło"* — bez
żadnych dodatkowych warunków.

**Obserwuj każdą iterację.** Nie odchodź od ekranu. Notuj, co zrobił w każdej.

**2 · Zepsuj tak, żeby nie dało się naprawić** (10 min). Zmień test tak, żeby
wymagał zachowania sprzecznego z innym testem. Powtórz polecenie.

Zobacz, jak wygląda pętla bez wyjścia: agent zaczyna krążyć, cofać własne
zmiany, próbować obejść. **To jest moment, w którym najczęściej zmienia test.**

**3 · Teraz z warunkami stopu** (10 min). To samo zepsucie, ale prompt:

> Napraw tak, żeby `./bramka` przechodziła.
>
> Warunki: **maksymalnie trzy podejścia**. Nie zmieniaj plików testowych.
> Nie dodawaj zależności. Nie wyłączaj testów. Jeśli po trzecim podejściu
> nie działa — **zatrzymaj się** i napisz mi, na czym stanąłeś i czego
> twoim zdaniem brakuje.

**4 · Porównaj** (10 min). Który przebieg dał lepszy kod? Który był krótszy?
Co agent powiedział, gdy pozwoliłeś mu się poddać?

**Skill, który tu pasuje:** `diagnosing-bugs` z zestawu Pococka — pętla
diagnostyczna do trudnych błędów i regresji wydajności. Działa bez konfiguracji.
Warto porównać jego strukturę z tym, co sam napisałeś w warunkach stopu.

## Do omówienia

- **Że „zatrzymaj się i zapytaj" to najbardziej niedoceniana instrukcja.**
  Model bez niej zawsze coś zrobi, bo do tego jest dostrojony. Jawne pozwolenie
  na powiedzenie „nie wiem" zmienia jakość odpowiedzi bardziej niż większość
  sztuczek promptowych.
- **Że limit iteracji to nie oszczędność tokenów.** To ochrona przed
  rozwiązaniem, które spełnia kryterium i nie rozwiązuje problemu.
- **Że bramka z Z07 jest tu twardym warunkiem stopu.** Reguły w prompcie
  wystarczają, dopóki agentowi nie zabraknie pomysłów.
- **Że jakość komunikatu błędu jest twoją dźwignią.** Jeśli twoje testy mówią
  „expected true but was false", pętla będzie długa niezależnie od modelu.
  To jest argument za asercjami, które opisują, co dokładnie się nie zgadza.
