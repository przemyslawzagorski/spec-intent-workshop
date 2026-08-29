# Z02 · Agenty kochają przykłady

**35 min** · archetyp Maven · bramka: `./sprawdz Z02`

## O co chodzi

Agent dostaje polecenie „dodaj komponent zgodnie z naszymi konwencjami". Skąd ma
wiedzieć, jakie one są? Możesz mu je **opisać** albo **pokazać**.

To zadanie robi trzy rzeczy, w tej kolejności:

1. **Generuje szkielet projektu z archetypu** — sześć sekund, identycznie u każdego.
   Teraz masz przykład, na który da się wskazać.
2. **Każe agentowi dołożyć drugi komponent na dwa sposoby** — raz opisując
   konwencje prozą, raz wskazując na pierwszy komponent.
3. **Porównuje wyniki liczbami**, nie wrażeniem.

Wniosek, do którego zmierzamy: **nie płać modelem za rzecz, którą robi skrypt** —
ani pieniędzmi, ani niedeterminizmem. Szkielet to kopiowanie plików. Myślenie
zostaw agentowi.

## Jak zwykle to robimy

Puste repo, prompt w stylu *„zrób mi serwis w Quarkusie z endpointem do zwrotów"*
i nadzieja. Dostajesz coś, co się buduje. Za drugim razem dostajesz coś innego,
co też się buduje. Kolega dostaje trzecią wersję.

Boli, bo:

- **Za każdym razem inaczej.** Struktura pakietów, nazewnictwo, sposób obsługi
  błędów — wszystko zależy od tego, co model akurat wylosował.
- **Konwencje trzeba opisywać słowami.** A opis prozą zawsze coś pominie.
- **Nie ma się do czego odwołać.** „Zrób jak w tamtym projekcie" nie działa,
  bo tamten projekt jest w innym repo, którego agent nie widzi.

## Jakie są opcje

**Opisz konwencje prozą** w `AGENTS.md` albo w promptcie. Działa, ale opis nigdy
nie jest kompletny, a każde jego słowo siedzi w kontekście przy każdym zapytaniu.

**Wskaż wzorcowy komponent.** Jedna linia zamiast dwudziestu. Agent czyta kod
i widzi rzeczy, których byś nie opisał — kolejność importów, sposób nazywania
zmiennych, to, że wyjątki nie lecą w górę tylko wracają jako `Problem`. Wada:
musisz mieć ten wzorcowy komponent. W pustym repo go nie ma.

**Wygeneruj szkielet deterministycznie** — archetyp Mavena, `copier` w Pythonie,
`create-*-app` w Node. Ta sama komenda daje ten sam wynik za każdym razem.
Wada: archetyp trzeba raz zrobić i potem utrzymywać.

Te trzy się nie wykluczają. Archetyp daje pierwszy przykład, przykład zastępuje
prozę, proza zostaje tylko na to, czego z kodu nie widać.

## Jak zrobić dobrze

**Zacznij od czegoś, co już działa.** Nie od pustego katalogu. Cokolwiek — stary
projekt, archetyp, `git clone` szablonu. Agent, który ma przykład, przestaje
zgadywać.

**Determinizm tam, gdzie jest za darmo.** Szkielet projektu nie wymaga inteligencji.
To jest kopiowanie plików z podmianą nazw. Robi to `mvn archetype:generate`
w sześć sekund, identycznie u każdego. **Nie płać modelem za rzecz, którą robi
skrypt** — ani pieniędzmi, ani niedeterminizmem.

**Zostaw agentowi to, co wymaga myślenia.** Logikę, przypadki brzegowe, decyzje.
Nie układ katalogów.

## Zrób to

```bash
./przygotuj Z02
cd praca/Z02
source ../../.tooling/env.sh
```

### Skąd jest ten archetyp

Zanim go uruchomisz — żeby nie było wątpliwości, co odpalasz.

**Ten archetyp jest nasz.** Leży w [`archetype/`](../../archetype/) w tym repo.
Powstał z projektu, który tam jest, jedną komendą:

```bash
mvn archetype:create-from-project
```

To jest cała robota. Bierzesz projekt, który już masz, i Maven robi z niego
generator: zamienia nazwy pakietów i artefaktu na zmienne, resztę zostawia.

**Konwencje układu** — podział na `boundary` / `control` / `entity` — są wzorowane
na [AdamBien/quarkus-microprofile](https://github.com/AdamBien/quarkus-microprofile).
Kod jest nasz, pomysł na układ nie.

`bootstrap.sh` zainstalował ten archetyp do lokalnego repozytorium w `.tooling/m2`,
więc generowanie działa offline i u wszystkich identycznie.

**To ważne dla wniosku z tego zadania:** archetypu nie trzeba szukać ani
instalować z internetu. Robisz go **ze swojego własnego projektu**, w minutę.
Wracamy do tego w ★ na końcu.

**1 · Wygeneruj szkielet z archetypu** (1 min):

```bash
wsmvn archetype:generate \
  -DarchetypeGroupId=workshop \
  -DarchetypeArtifactId=returns-service-archetype \
  -DarchetypeVersion=2026 \
  -DgroupId=warsztat -DartifactId=returns-service \
  -Dversion=1.0.0 -Dpackage=warsztat.rma
```

**Co zobaczysz:** sporo linii od Mavena, w tym ostrzeżenia o `sun.misc.Unsafe`
(**to normalne**, Maven 3.9 na JDK 25 tak gada), a na końcu `BUILD SUCCESS`.
Powstanie katalog `returns-service` z **27 plikami**.

Sześć sekund. **U wszystkich identycznie.**

Zajrzyj do `returns-service/src/main/java/warsztat/rma/returns/` — masz teraz
wzorcowy komponent, na który można wskazywać.

**2 · Zwróć uwagę na jedną rzecz:**

```bash
cd returns-service && wsmvn test
```

**Co zobaczysz:** `BUILD SUCCESS`. Pierwszy raz trwa to około **85 sekund**,
bo Quarkus ściąga zależności.

**A teraz przyjrzyj się, czego NIE zobaczysz:** nigdzie nie ma linii
`Tests run: ...`. Bo klas testowych jest **zero**. Archetyp daje pomocniki
(`Fixtures`, `PolicyCase`), ale ani jednego testu.

Zielony build, który nie znaczy nic. Zapamiętaj to — wrócimy do tego w Z07.

**3 · Podejście A — proza** (10 min). Prompt: [prompty/A-proza.md](prompty/A-proza.md).

**Co zobaczysz:** agent napisze komponent `shipments` i spróbuje go skompilować.
**Notuj dwie liczby:** ile razy musiał poprawiać, żeby się skompilowało,
i ile rzeczy poprawiłeś ręcznie po nim.

**4 · Wróć do czystego stanu** i **podejście B — wskaźnik**:

```bash
cd ../../..                  # do korzenia warsztatu
./przygotuj Z02 --od-nowa    # czysty katalog
cd praca/Z02
```

I powtórz `wsmvn archetype:generate` z kroku 1.

Prompt: [prompty/B-wzorzec.md](prompty/B-wzorzec.md). Notuj to samo.

**5 · Zapisz pomiar** — z korzenia warsztatu:

```bash
cd ../..                     # z praca/Z02 do korzenia
uv run tools/bench.py record --etykieta "Z02 proza"   --model <twój> --iteracje N --kto <imię>
uv run tools/bench.py record --etykieta "Z02 wzorzec" --model <twój> --iteracje N --kto <imię>
uv run tools/bench.py report
./sprawdz Z02
```

## Pytanie na czat

**Ile iteracji w A, ile w B?** Format: `A=3 B=1`.

## Omówienie

Poproszę o pokazanie ekranu kogoś, u kogo **B wypadło gorzej niż A** — jeśli taka
osoba jest. To ciekawsze niż potwierdzenie tezy.

Rzeczy, o których pogadamy:

- **Różnica jest głównie w wierności, nie w rozmiarze.** Sam opis konwencji
  z podejścia A to ~1280 bajtów, wskaźniki z B ~590. Dwa razy mniej, nie sto.
  Ale kod z B trafia w konwencje, których w prozie w ogóle nie opisałem —
  bo bym o nich nie pomyślał.
- **Ten opis żyje w każdym promptcie.** Jednorazowy prompt to nie problem.
  Problem to 300 tokenów doklejanych do każdego zapytania przez pół roku.
- **Kiedy A wygrywa.** Gdy nowy komponent ma być inny niż istniejące. Wtedy
  wskazanie wzorca aktywnie szkodzi — agent skopiuje kształt, którego nie chcesz.
- **Skąd wziąć archetyp.** `mvn archetype:create-from-project` na projekcie,
  który już masz. Nie trzeba pisać od zera.
- **Pułapka, w którą wpadłem, robiąc ten archetyp.** `create-from-project`
  sparametryzował literał `2026` w dacie `Instant.parse("2026-06-15T12:00:00Z")`
  i zamienił go na `${version}`. Projekt się kompilował — bo to string — i wywalał
  się dopiero w runtime. **Archetyp trzeba testować uruchomieniem, nie kompilacją.**

## Kiedy to NIE ma sensu

Jeden projekt na rok — archetyp się nie zwróci. Zespół, który celowo próbuje
różnych układów, bo jeszcze nie wie, który jest dobry — determinizm zamrozi wam
wybór za wcześnie. I sytuacja, gdy szablon jest dostępny na wyciągnięcie ręki
(`quarkus create`, `spring init`) — wtedy nie rób własnego.

## ★ Jeśli skończyłeś wcześniej

| ★ | Co robisz | Min |
|---|---|---|
| **Archetyp z własnego projektu** | `mvn archetype:create-from-project` na czymkolwiek swoim. Wygeneruj z niego projekt i **uruchom testy**, nie tylko skompiluj. Poszukaj sparametryzowanych literałów. | 20 |
| **To samo w Pythonie** | Szablon `copier`, uruchamiany przez `uvx copier copy`. Porównaj, ile pracy kosztuje w stosunku do archetypu Mavena. | 20 |
| **Znajdź pułapkę** | W archetypie w `archetype/` siedzi opisana wyżej pułapka z `${version}`. Znajdź ją i napisz test, który by ją złapał. | 10 |
| **Archetyp kontra „skopiuj poprzedni"** | Zrób oba i policz, po ilu projektach kopiowanie zaczyna się rozjeżdżać. | 15 |
| **Trzeci wariant promptu** | A i B plus C: wskaźnik na wzorzec **i** trzy zdania o tym, czym nowy komponent ma się od niego różnić. Zmierz. | 15 |

## Rozwiązanie

Rozwiązaniem jest sam archetyp: [../../archetype/](../../archetype/).
Wygenerowany projekt to punkt odniesienia — zajrzyj do `praca/Z02/returns-service`
po wykonaniu kroku 1.
