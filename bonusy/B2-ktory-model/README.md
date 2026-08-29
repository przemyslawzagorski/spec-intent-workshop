# B2 · Który model do czego

**35 min** · dowolne zadanie z bramką · bonus

## O co chodzi

Modele różnią się ceną o rząd wielkości. Pytanie brzmi, czy różnią się jakością
tak samo — i **czy to zależy od tego, jak przygotowałeś zadanie.**

To jedyne ćwiczenie w tym warsztacie, w którym możesz wyjść z wynikiem
przeciwnym do tezy. Tak ma być.

## Jak zwykle to robimy

Wybieramy najmocniejszy model, jaki mamy, i używamy go do wszystkiego —
od generowania szkieletu projektu po nazwanie zmiennej. Albo odwrotnie:
wybieramy najtańszy, bo „i tak wszystko trzeba poprawiać".

Boli, bo:

- **Nie wiemy, ile płacimy za nic.** Wygenerowanie `Dockerfile` z gotowego
  wzorca nie wymaga rozumowania.
- **Nie wiemy, gdzie oszczędność boli.** Tańszy model na zadaniu bez bramki
  potrafi kosztować godzinę debugowania.
- **Wrażenie zastępuje pomiar.** „Ten model jest lepszy" jest nieweryfikowalne.

## Teza do sprawdzenia

> **Im lepszy harness i lepsza specyfikacja, tym słabszy model wystarcza.**

Sformułowanie jest ostrożne celowo: **„wystarcza", nie „jest lepszy"**.

Intuicja za nią: model bez bramki musi zgadnąć, co znaczy „gotowe". Model
z bramką dostaje kryterium i pętlę zwrotną — może być słabszy, bo mniej rzeczy
musi trafić za pierwszym razem.

**Jeśli liczby jej nie potwierdzą, to też jest wynik.** Omówimy go uczciwie.

## Zrób to

Potrzebujesz dostępu do co najmniej dwóch modeli. Jeśli masz jeden — zrób
ćwiczenie z sąsiadem i porównajcie.

**1 · Wybierz zadanie z twardym kryterium** (5 min). Najlepiej takie,
które już robiłeś, więc wiesz, jak wygląda dobry wynik:

- **Z02** — komponent z archetypu, kryterium: kompiluje się i trzyma konwencje
- **Z10** — Dockerfile, kryterium: buduje się, ma healthcheck, poniżej 500 MB
- **Z11** — endpoint, kryterium: `./bramka` otwarta

**2 · Przebieg bez harnessu** (10 min). Ten sam prompt, każdy model,
**bez** podawania komendy weryfikującej. Mierz:

- ile iteracji do wyniku, który sam uznasz za dobry,
- ile rzeczy poprawiłeś ręcznie,
- czas.

**3 · Przebieg z harnessem** (10 min). To samo, ale w prompcie:
*„sprawdzaj się komendą `X`, popraw i powtórz, maksymalnie trzy podejścia"*.

**4 · Wspólna tabela** (10 min):

```bash
uv run tools/bench.py record --etykieta "B2 bez bramki" --model <nazwa> \
    --iteracje N --sekundy S --kto <imię>
uv run tools/bench.py record --etykieta "B2 z bramka"   --model <nazwa> \
    --iteracje N --sekundy S --kto <imię>
uv run tools/bench.py report
```

## Do omówienia

Patrzymy na tabelę całej sali, nie na pojedynczy wynik — przy jednej próbie
na model rozrzut jest większy niż różnica, której szukamy.

- **Czy różnica między modelami zmalała, gdy było kryterium?** To jest właściwe
  pytanie tego ćwiczenia.
- **Gdzie słabszy model nie wystarczył mimo bramki.** Zwykle tam, gdzie trzeba
  było coś **wymyślić**, a nie sprawdzić — projekt interfejsu, nazwanie pojęć,
  wybór między dwoma podejściami.
- **Ile z tego to była loteria.** Jedna próba na model to anegdota. Cała sala
  razem to już coś.
- **Że koszt to nie tylko cena za token.** Model, który potrzebuje pięciu
  iteracji zamiast dwóch, zjada też twoje pięć minut — i to zwykle droższe.

## Skala, na której to widać

Przy przepisaniu Buna z Ziga na Rusta autor podaje: **5,9 mld tokenów wejścia
niecache'owanego, 690 mln wyjścia, 72 mld odczytów z cache'u** — około
165 000 dolarów. Użyto jednego, mocnego modelu.

Ale to samo źródło opisuje, dlaczego było to wykonalne: **język, w którym
kompilator łapie klasę błędów popełnianych przez model**, i **zestaw testów
z ponad milionem asercji, napisany w innym języku niż przepisywany kod**.

Innymi słowy: zainwestowali w harness tak mocno, że pytanie „który model"
przestało być najważniejsze. To jest ta sama teza, tylko w skali, której
nie zmierzysz w trzydzieści minut.
