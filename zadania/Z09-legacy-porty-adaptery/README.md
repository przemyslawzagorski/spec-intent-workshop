# Z09 · Legacy za siatką → porty i adaptery

**55 min** · kata + petclinic · bramka: `./sprawdz Z09`

## O co chodzi

Refaktor to zmiana struktury **bez zmiany zachowania**. Problem w tym, że
w legacy nikt nie wie, jakie dokładnie jest zachowanie — bo nie ma testów,
a jedyną specyfikacją jest kod.

Dwie części. Najpierw kata: jak zbudować siatkę pod kodem, którego nie rozumiesz.
Potem prawdziwy refaktor na petclinicu.

## Jak zwykle to robimy

„Zrefaktoryzuj mi tę klasę." Agent przepisuje. Wygląda ładnie, kompiluje się,
testy przechodzą — bo nie było testów. Trzy tygodnie później okazuje się, że
zwroty w ostatnim dniu okna przestały być przyjmowane.

Boli, bo:

- **Nie wiesz, co się zmieniło.** Diff pokazuje, że zmieniło się wszystko.
- **Nie masz jak sprawdzić.** Testów nie ma, a napisanie ich do starego kodu
  wymaga zrozumienia starego kodu — czyli tego, po co robisz refaktor.
- **Agent „poprawia" przy okazji.** Zobaczy `>=` tam, gdzie „powinno być" `>`,
  i naprawi. Może miał rację. Może to była reguła biznesowa.

## Jakie są opcje

**Napisz testy jednostkowe najpierw.** Podręcznikowo poprawne. Wymaga
zrozumienia kodu, czyli tego, czego jeszcze nie masz.

**Refaktoruj małymi krokami i patrz.** Działa dla człowieka, który zna kod.
Przy agencie, który robi trzysta linii w minutę, „małymi krokami" przestaje
być prawdą.

**Złote wzorce.** Uruchamiasz stary kod na wielu wejściach, **zapisujesz wyjścia
takie, jakie są** — razem z błędami. To jest twoja siatka. Potem refaktorujesz
i odtwarzasz. Rozjazd znaczy: zmieniłeś zachowanie.

Trzecia opcja jest tania i nie wymaga rozumienia kodu. Dlatego zaczynamy od niej.

## Jak zrobić dobrze

**Nagraj zachowanie, nie oczekiwania.** To jest cała sztuczka. Nie zapisujesz,
co kod **powinien** robić — zapisujesz, co **robi**. Razem z błędami. Jeśli
zaraz po nagraniu poprawisz nagranie, żeby było „poprawne", siatka przestaje
działać, zanim ją założysz.

**Rozjazd zatrzymuje refaktor.** Nie „przeanalizuj rozjazd" — zatrzymaj się.
Potem, jako człowiek, zdecyduj: to był błąd, który naprawiamy świadomie i osobno,
czy reguła biznesowa, którą właśnie zepsułeś.

**Nagrywaj brzegi.** Dzień przed końcem okna, ostatni dzień, pierwszy po.
Zero, wartość ujemna, pusta lista. Rozjazd pojawia się na brzegach.

**Do prawdziwego refaktoru: wydzielaj po jednym przypadku użycia.** Nie „przenieś
wszystko do serwisów". Jeden przypadek, port, adapter, test bez frameworka.
Potem następny.

**Skille, które tu pasują:**

- **`zlote-wzorce`** — nasz, spakowana część 1 tego zadania:
  [rozwiazanie/skill-zlote-wzorce/SKILL.md](rozwiazanie/skill-zlote-wzorce/SKILL.md).
  Przydaje się za każdym razem, gdy trzeba ruszyć kod bez testów.
- **`codebase-design`** — z zestawu Matta Pococka, o projektowaniu głębokich
  modułów i granic. Do części 2. Działa od ręki.
- **`characterization-tests`** z [airails](https://github.com/AdamBien/airails)
  Adama Biena — **to jest dokładnie ta sama technika co nasza część 1**, tylko
  cudzą ręką. Warto porównać, co ktoś inny uznał za istotne.

## Co się tu dzieje — przeczytaj, zanim zaczniesz

To zadanie ma **dwie części i one uczą dwóch różnych rzeczy**. Łatwo się w tym
pogubić, więc po kolei:

| | Część 1: kata | Część 2: petclinic |
|---|---|---|
| **Czego uczy** | jak **zabezpieczyć się** przed zmianą zachowania | jak **zmienić strukturę**, gdy już jesteś zabezpieczony |
| **Na czym** | 161 linii, zero testów, czysty `javac` | prawdziwy Spring, 79 testów |
| **Co robisz** | nagrywasz wyjścia, „poprawiasz" błąd, oglądasz rozjazd | wydzielasz port, adapter i przypadek użycia |
| **Ile trwa** | 25 min, pętla dwie sekundy | 30 min, pętla 23 sekundy |
| **Co zostaje** | `DECYZJA.md` | cztery pliki i szybki test |

**Kolejność ma znaczenie.** Część 1 pokazuje, że bez siatki nie wiesz, czy coś
zmieniłeś. Część 2 zakłada, że już to rozumiesz, i pokazuje samą technikę.

Jeśli masz czas tylko na jedną — **rób część 1**. Siatka jest ważniejsza
od architektury.

## Zrób to — część 1: kata (25 min)

```bash
./przygotuj Z09
cd praca/Z09
source ../../.tooling/env.sh
```

> **Windows:** uruchamiaj w **Git Bash** albo WSL, nie w PowerShellu.
> W PowerShellu `./przygotuj` kończy się bez komunikatu i bez efektu, a `source`
> nie istnieje. Sam `javac` i `java` działają wszędzie — chodzi o skrypty wokół.

`ReturnEligibilityService` — 161 linii, zero testów, kilka reguł biznesowych
i przynajmniej jeden błąd. Czysty `javac`, bez Mavena, pętla dwie sekundy.

**1 · Nagraj** (5 min):

```bash
javac -d . legacy/*.java Bodzce.java Nagraj.java
java Nagraj > wzorce/return-eligibility.tsv
head -3 wzorce/return-eligibility.tsv
```

**Co zobaczysz:** trzy pierwsze wiersze pliku TSV — nagłówek i dwa przypadki
z identyfikatorami `L01`, `L02`. Cały plik ma **siedemnaście przypadków**.

**Nie czytaj ich krytycznie.** To jest zdjęcie stanu obecnego, nie lista życzeń.

Zerknij jednak na cztery ostatnie wiersze, bo są tam z konkretnego powodu:

| | Co się dzieje |
|---|---|
| `L14`, `L15` | brak zamówienia albo zgłoszenia → `REJECTED / INVALID_INPUT`, ale **kwota zwrotu to `null`**, bo ta gałąź nigdy jej nie ustawia |
| `L16` | zgłoszenie **bez pozycji** → `AUTO_APPROVED` |
| `L17` | SKU, którego **nie ma w zamówieniu** → `AUTO_APPROVED` |

Zwrot pustego zgłoszenia jest zatwierdzany automatycznie. Nikt tego nie
zaprojektował — tak po prostu wychodzi z pętli, która nie ma czego dopasować.
**Nagrywasz to bez poprawiania.** Czy to błąd, rozstrzygasz w kroku 4.

**2 · Sprawdź, że siatka trzyma** (2 min):

```bash
javac -d . legacy/*.java Bodzce.java OdtworzWzorce.java
java OdtworzWzorce wzorce/return-eligibility.tsv
```

**Co zobaczysz:**

```
OK    17 zlotych wzorcow odtworzonych bez rozjazdu
```

Cała pętla — kompilacja i odtworzenie — trwa **około dwóch sekund**. To celowe:
przy takiej pętli możesz eksperymentować, nie planować.

**3 · Teraz „popraw błąd"** (8 min). W `legacy/ReturnEligibilityService.java`,
linia 101, jest:

```java
if (days >= window) {
```

Wygląda źle, prawda? Okno trzydziestodniowe powinno obejmować trzydziesty dzień.
Zmień na `>` i odtwórz wzorce.

```bash
javac -d . legacy/*.java Bodzce.java OdtworzWzorce.java && java OdtworzWzorce wzorce/return-eligibility.tsv
```

Dostaniesz:

```
ROZJAZD L02  (default: OSTATNI dzien okna)
  nagrane:   REJECTED / WINDOW_EXPIRED / zwrot 0
  faktyczne: AUTO_APPROVED / WITHIN_POLICY / zwrot 100.00
FAIL  1 z 17 wzorcow sie rozjechalo.
```

**Jeden rozjazd na siedemnaście — dokładnie ten, który zmieniłeś.** Siatka nie
mówi, że zrobiłeś źle. Mówi, że zrobiłeś **coś**, i pokazuje co.

**4 · Zdecyduj jako człowiek** (10 min). Zapisz decyzję w `DECYZJA.md`:
czy to był błąd, czy reguła? Skąd wiesz? Co robisz dalej i w jakiej kolejności?

Potem cofnij zmianę.

## Zrób to — część 2: petclinic (30 min)

`OwnerController.processUpdateOwnerForm` miesza regułę biznesową z HTTP:

```java
if (!Objects.equals(owner.getId(), ownerId)) {
    result.rejectValue("id", "mismatch", "...");
    redirectAttributes.addFlashAttribute("error", "...");
    return "redirect:/owners/{ownerId}/edit";
}
```

Reguła „identyfikator w formularzu musi zgadzać się z tym w adresie" siedzi
w tej samej metodzie co `BindingResult`, `RedirectAttributes` i nazwy widoków.
Żeby ją sprawdzić, trzeba postawić warstwę webową.

**Wydziel z tego port, adapter i przypadek użycia.**
Prompt: [prompty/refaktor.md](prompty/refaktor.md).

Cel:

- **port** — interfejs opisujący, czego logika potrzebuje od składowania.
  Tyle metod, ile ta logika naprawdę woła — nie czterdzieści dziedziczone
  po `JpaRepository`.
- **adapter** — klasa łącząca port ze Spring Data. Cała wiedza o JPA kończy się tutaj.
- **przypadek użycia** — klasa z regułą, zwracająca **wynik**, nie nazwę widoku.
- **test bez Springa** — z atrapą portu na kilka linii.

```bash
cd ../../.. && ./sprawdz Z09
```

**Co zobaczysz:** dwie sekcje — `kata:` i `petclinic:` — razem siedem sprawdzeń.
Ostatnie (`pelne testy przechodza`) uruchamia cały zestaw petclinica
i trwa **około 75 sekund** (zmierzone, patrz [rozwiazanie/POMIAR.md](rozwiazanie/POMIAR.md)),
więc nie przejmuj się, jeśli konsola milczy.

### Jak poznać, że zrobiłeś to dobrze

Cztery rzeczy, wszystkie sprawdzalne:

1. **Port ma tyle metod, ile logika naprawdę używa.** U mnie wyszła **jedna** —
   i to jest ciekawsze, niż wygląda. Pisząc rozwiązanie, dołożyłem tam
   `findById`, bo przy słowie „aktualizacja" wydaje się oczywiste, że trzeba
   najpierw pobrać. Nie trzeba: obiekt przychodzi z formularza, a ten przypadek
   użycia go tylko zapisuje. Metoda nie miała **ani jednego wołającego** —
   i przez jakiś czas stała w materiale jako wzór. Jeśli przepisałeś
   `JpaRepository` albo dołożyłeś metodę „bo się przyda" — to nie jest port,
   to jest ta sama zależność pod nową nazwą.

   Sprawdź to u siebie mechanicznie: wyszukaj każdą metodę portu i policz
   wywołania poza adapterem i atrapą testową. Zero wywołań to zero metody.
2. **Klasa z regułą nie importuje niczego z `org.springframework.web`.**
   Zero `BindingResult`, zero `RedirectAttributes`, zero nazw widoków.
3. **Test reguły nie ma żadnej adnotacji Springa** i wykonuje się w milisekundach.
   Jeśli musisz postawić kontekst — wydzielenie się nie udało.
4. **Zachowanie się nie zmieniło.** Te same przekierowania, te same komunikaty.
   Pełne `mvn test` zielone.

## Pytanie na czat

**Część 1: czy `>=` to był błąd, czy reguła? Jedno zdanie uzasadnienia.**

## Omówienie

Poproszę o ekran kogoś, kto uznał, że to błąd, i kogoś, kto uznał, że reguła.

Pogadamy o:

- **Że nie da się tego rozstrzygnąć z kodu.** I to jest sedno. Odpowiedź jest
  u kogoś w firmie, nie w repozytorium. Siatka nie mówi ci, co jest dobre —
  mówi, **co się zmieniło**. Decyzja zostaje przy tobie i to jest właściwy podział pracy.
- **Dlaczego nie wolno poprawić nagrania.** Bo wtedy dostaniesz zielony refaktor
  i zmienione zachowanie, czyli najgorszy możliwy wynik: pewność bez pokrycia.
- **Siatka chroni dokładnie to, co porównuje — ani grama więcej.** Nasz
  odtwarzacz przez pewien czas czytał z nagrania decyzję i powody, ale **nie
  kwotę zwrotu**, choć kwota stała w pliku, w osobnej kolumnie. Efekt: refaktor
  ścinający każdy zwrot o 10% przechodził na zielono. Wyjście zapisane, ale
  nieporównywane, nie jest chronione — jest tylko dekoracją.

  **Zrób to na sali, trwa dziesięć sekund:** zmień w `legacy` ostatnią linię
  `check` tak, żeby zwrot był `amount.multiply(new BigDecimal("0.90"))`,
  i odtwórz wzorce. Dostaniesz rozjazd na `L01`, `L07`, `L08`. Potem zapytaj,
  ile takich niepilnowanych wyjść ma wasza siatka w pracy.
- **I druga strona tego samego.** Usuń z `check` cały strażnik `null` na początku
  metody. Rozjazd na `L14` i `L15` — dlatego, że ktoś **zadał sobie trud
  nagrania przypadków z brakującymi danymi**. Gdyby siatka miała tylko
  „normalne" wejścia, wycięcie całej gałęzi obsługi błędu przeszłoby bez słowa.
  Siatka jest tak dobra jak zbiór bodźców, nie jak narzędzie.
- **Liczbach z części drugiej.** Po refaktorze `UpdateOwnerTests` — trzy testy
  reguły, bez Springa, bez bazy — wykonuje się w **0,021 s**.
  `OwnerControllerTests`, które sprawdzają to samo przez warstwę webową, trwały
  w tym samym przebiegu **12,0 s**. **To jest waluta, w której opłaca się liczyć
  porty i adaptery: długość pętli zwrotnej, nie czystość diagramu.**

  **Podaj rząd wielkości, nie mnożnik.** Wcześniejsze wersje tego materiału
  miały tu 1,451 s, a komentarz w kodzie 5,6 s — obie liczby prawdziwe, obie
  z innej maszyny i innego momentu. Czas testu webowego zależy od tego, czy
  kontekst Springa wstaje pierwszy raz i czy JVM jest rozgrzana; czas testu
  reguły nie zależy od niczego, bo nic tam nie startuje. **Stabilny jest tylko
  rząd wielkości: milisekundy kontra sekundy.** Protokół i warunki pomiaru:
  [rozwiazanie/POMIAR.md](rozwiazanie/POMIAR.md).
- **Że refaktor zmusił do zmiany testu.** `@WebMvcTest(OwnerController.class)`
  przestał się składać, bo kontroler dostał nowego współpracownika. Trzeba było
  dopisać `@Import(UpdateOwner.class)`. **To jest legalna zmiana testu** — zmienił
  się kontrakt klasy, a nie wygoda. Reguła z Z07 nie brzmi „nigdy nie dotykaj
  testów", tylko „rób to świadomie i umiej powiedzieć dlaczego". To jest właśnie
  ten przypadek.

## Kiedy to NIE ma sensu

Kod, który za miesiąc kasujesz. Klasa, która ma trzy linie i jedną ścieżkę.
I — najważniejsze — **cały projekt naraz**. Porty i adaptery wszędzie, gdzie
się da, to nie architektura, tylko ceremonia. Wydzielaj tam, gdzie boli:
gdzie testy są wolne albo gdzie reguła jest zaklinowana we frameworku.

## ★ Jeśli skończyłeś wcześniej

| ★ | Co robisz | Min |
|---|---|---|
| **Drugi przypadek użycia** | To samo dla `processCreationForm`. Zobacz, czy port da się użyć ponownie, czy potrzebujesz drugiego. | 30 |
| **Podmień adapter** | Napisz drugi adapter portu — w pamięci — i uruchom na nim całą aplikację. Ile rzeczy przestanie działać? | 25 |
| **Refaktor bez siatki** | Wróć do katy, skasuj wzorce i zrefaktoruj „na oko". Policz, po ilu krokach tracisz pewność, że nic nie zepsułeś. | 20 |
| **`enterprisifier`** | Skill z airails: zdemoluj czysty kod nadmiarem warstw, potem odkręć drugim agentem. Bardzo pouczające w obie strony. | 20 |
| **Porównaj z sąsiadem** | Twój port kontra jego. Ile decyzji było wspólnych? Rozbieżności pokazują, co naprawdę wynikało z kodu, a co z gustu. | 15 |
| **Wzorce dla petclinica** | Nagraj złote wzorce dla `OwnerController` przez MockMvc, zanim zaczniesz refaktor. Porównaj koszt z katą. | 25 |

## Rozwiązanie

**Kata:** [../../katy/legacy-eligibility/PRZYKLADOWA-DECYZJA.md](../../katy/legacy-eligibility/PRZYKLADOWA-DECYZJA.md)

**Pomiar:** [rozwiazanie/POMIAR.md](rozwiazanie/POMIAR.md) — ile naprawdę trwa
pętla zwrotna, w jakich warunkach zmierzone i dlaczego ta liczba skacze.

**Petclinic:** [rozwiazanie/](rozwiazanie/) — cztery nowe pliki i dwa diffy.
Sprawdzone na przypiętym commicie: **77 testów uruchomionych, 2 pominięte,
zero błędów, `BUILD SUCCESS`**.

| Plik | Co to |
|---|---|
| `Owners.java` | port — **jedna** metoda, bo tyle woła logika. Przeczytaj komentarz: przez chwilę były dwie |
| `OwnersJpaAdapter.java` | adapter — jedyne miejsce, które wie o Spring Data |
| `UpdateOwner.java` | reguła, zwraca wynik zamiast nazwy widoku |
| `UpdateOwnerTests.java` | trzy testy, atrapa portu na cztery linie, zero Springa |
| `OwnerController.diff` | kontroler schudł do tłumaczenia wyniku na HTTP |
| `OwnerControllerTests.diff` | wymuszona zmiana plastra testowego, z uzasadnieniem |
