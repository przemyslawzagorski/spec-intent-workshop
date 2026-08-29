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
| **Na czym** | 161 linii, zero testów, czysty `javac` | prawdziwy Spring, 76 testów |
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

`ReturnEligibilityService` — 161 linii, zero testów, kilka reguł biznesowych
i przynajmniej jeden błąd. Czysty `javac`, bez Mavena, pętla dwie sekundy.

**1 · Nagraj** (5 min):

```bash
javac -d . legacy/*.java Nagraj.java
java Nagraj > wzorce/return-eligibility.tsv
head -3 wzorce/return-eligibility.tsv
```

**Co zobaczysz:** trzy pierwsze wiersze pliku TSV — nagłówek i dwa przypadki
z identyfikatorami `L01`, `L02`. Cały plik ma **dwanaście przypadków**.

**Nie czytaj ich krytycznie.** To jest zdjęcie stanu obecnego, nie lista życzeń.

**2 · Sprawdź, że siatka trzyma** (2 min):

```bash
javac -d . legacy/*.java OdtworzWzorce.java
java OdtworzWzorce wzorce/return-eligibility.tsv
```

**Co zobaczysz:**

```
OK    12 zlotych wzorcow odtworzonych bez rozjazdu
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
javac -d . legacy/*.java OdtworzWzorce.java && java OdtworzWzorce wzorce/return-eligibility.tsv
```

Dostaniesz:

```
  nagrane:   REJECTED / WINDOW_EXPIRED
  faktyczne: AUTO_APPROVED / WITHIN_POLICY
FAIL  1 z 12 wzorcow sie rozjechalo.
```

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
  Dwie metody, nie czterdzieści dziedziczone po `JpaRepository`.
- **adapter** — klasa łącząca port ze Spring Data. Cała wiedza o JPA kończy się tutaj.
- **przypadek użycia** — klasa z regułą, zwracająca **wynik**, nie nazwę widoku.
- **test bez Springa** — z atrapą portu na pięć linii.

```bash
cd ../../.. && ./sprawdz Z09
```

**Co zobaczysz:** dwie sekcje — `kata:` i `petclinic:` — razem siedem sprawdzeń.
Ostatnie (`pelne testy przechodza`) uruchamia cały zestaw petclinica
i trwa **około 80 sekund**, więc nie przejmuj się, jeśli konsola milczy.

### Jak poznać, że zrobiłeś to dobrze

Cztery rzeczy, wszystkie sprawdzalne:

1. **Port ma tyle metod, ile logika naprawdę używa.** U mnie dwie. Jeśli
   przepisałeś `JpaRepository` — to nie jest port, to jest ta sama zależność
   pod nową nazwą.
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
- **Liczbach z części drugiej.** Po refaktorze `UpdateOwnerTests` — trzy testy
  reguły, bez Springa, bez bazy — wykonuje się w **0,011 s**.
  `OwnerControllerTests`, które sprawdzają to samo przez warstwę webową, trwają
  **1,451 s**. Sto trzydzieści razy dłużej. **To jest waluta, w której opłaca się
  liczyć porty i adaptery: długość pętli zwrotnej, nie czystość diagramu.**
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

**Petclinic:** [rozwiazanie/](rozwiazanie/) — cztery nowe pliki i dwa diffy.
Sprawdzone: pełny zestaw **79 testów, zero błędów**.

| Plik | Co to |
|---|---|
| `Owners.java` | port — dwie metody, zdefiniowane przez tego, kto ich używa |
| `OwnersJpaAdapter.java` | adapter — jedyne miejsce, które wie o Spring Data |
| `UpdateOwner.java` | reguła, zwraca wynik zamiast nazwy widoku |
| `UpdateOwnerTests.java` | trzy testy, atrapa portu na pięć linii, zero Springa |
| `OwnerController.diff` | kontroler schudł do tłumaczenia wyniku na HTTP |
| `OwnerControllerTests.diff` | wymuszona zmiana plastra testowego, z uzasadnieniem |
