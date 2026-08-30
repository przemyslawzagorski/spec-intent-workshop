# Z08 · Klucz odpowiedzi

> **Nie otwieraj tego pliku przed ćwiczeniem.** To jedyne zadanie w warsztacie,
> w którym zaglądnięcie wcześniej psuje całą wartość: raz przeczytanego błędu
> nie da się już odszukać samodzielnie po raz drugi. Dziesięć minut z kroku 1
> jest warte więcej niż ten plik.

Trzy części, w kolejności kroków:

| Część | Do czego | Kiedy |
|---|---|---|
| 1 | trzy fragmenty Rusta z `handouts/1-fragmenty-rust.md` | po kroku 1 i 2 |
| 2 | trzy fragmenty Javy z `handouts/2-kandydat-java.md` | po ★, jeśli je robiłeś |
| 3 | `handouts/3-przyklad-do-roastu/` — komplet znalezisk i tabela pokrycia | po kroku 3 |

Liczby z naszych czterech przebiegów są osobno, w [POMIAR.md](POMIAR.md).

---

# Część 1 — fragmenty z Buna

Wszystkie trzy są prawdziwe: pochodzą z artykułu
[bun.com/blog/bun-in-rust](https://bun.com/blog/bun-in-rust), z sekcji
**„Adversarial review"**, podpisanej tam jako *„3 of the many bugs adversarial
review caught before merge"*.

| Fragment | Plik w artykule |
|---|---|
| A | `js_bun_spawn_bindings.rs` |
| B | `node_fs.rs` |
| C | `css/values/color.rs` |

## A · `Box` zwolniony przed asynchronicznym `uv_close`

```rust
pipe.close(Subprocess::on_pipe_close)      // pipe: Box<uv::Pipe>
```

**Co się dzieje.** `Box` to wskaźnik na pamięć, którą Rust zwalnia automatycznie
na końcu zakresu — czyli na końcu tego ramienia `match`. Ale `uv_close` jest
**asynchroniczne**: libuv zapamiętuje surowy wskaźnik i oddzwoni dopiero
w następnym obrocie pętli zdarzeń. Rust już wtedy tę pamięć zwolnił.

Efekt: **use-after-free**, a gdy `on_pipe_close` wykona swoje zadanie i zwolni
alokację po raz drugi — **double-free**.

**Poprawka:** `Box::leak(pipe).close(Subprocess::on_pipe_close)` — świadomie
oddajemy własność libuv, bo to ona zwolni pamięć w callbacku.

**Dlaczego zwykły przegląd tego nie łapie.** Linijka jest idiomatyczna i czyta
się jak „zamknij ten pipe". Błąd nie siedzi w kodzie, tylko w **niezgodności
między modelem czasu życia w Ruście a asynchronicznością biblioteki C**. Żeby go
zobaczyć, trzeba wiedzieć rzecz spoza tego pliku.

## B · `trunc` zamiast `floor` dla ujemnego czasu

```rust
let sec = t.trunc();
nsec: ((t - sec) * 1e9) as i64,
```

**Co się dzieje.** `trunc()` obcina w stronę zera. Dla `t = -1.5` daje `sec = -1`,
więc `nsec = (-1.5 - (-1)) × 1e9 = -500_000_000`. **Ujemne nanosekundy to
nieprawidłowy `timespec`** — pole musi być w `[0, 1e9)`.

`floor()` obcina w dół: `sec = -2`, `nsec = +500_000_000`. Poprawnie.

**Kiedy wybucha.** Tylko dla pliku z datą modyfikacji **sprzed 1970 roku**.
Czyli praktycznie nigdy w testach i regularnie w archiwach, backupach
i systemach plików po migracji.

**Poprawka:** `t.floor()` plus `.round()` przy przeliczaniu nanosekund.

**Dlaczego zwykły przegląd tego nie łapie.** `trunc` wygląda na oczywisty wybór
dla „rozdziel część całkowitą od ułamkowej". Trzeba pomyśleć o wartości ujemnej,
a nikt nie myśli o plikach z 1969 roku.

## C · `unwrap_or` ewaluowane zachłannie

```rust
let p1 = first.percentage.unwrap_or(1.0 - second.percentage.unwrap());
```

**Co się dzieje.** `unwrap_or` to **zwykła funkcja**, więc jej argument jest
obliczany **zawsze** — także wtedy, gdy `first.percentage` istnieje i wynik i tak
zostanie zignorowany. Jeśli to `second.percentage` jest puste, wewnętrzny
`unwrap()` panikuje, **zanim `unwrap_or` zdąży cokolwiek pominąć**.

**Wywołanie, które to wywraca:** `color-mix(in srgb, red 40%, blue)` — pierwsza
strona ma procent, druga nie.

**Poprawka:** `unwrap_or_else(|| 1.0 - second.percentage.unwrap())` — domknięcie
liczone leniwie, tylko gdy trzeba.

**Dlaczego zwykły przegląd tego nie łapie.** Linijka czyta się **dokładnie jak
intencja**: „weź procent pierwszego, a jak go nie ma, policz dopełnienie
drugiego". Błąd siedzi w semantyce ewaluacji argumentów, nie w logice. To jest
najbardziej podstępny z całej trójki.

## Co łączy całą trójkę

Żadnego z tych trzech błędów **nie da się zobaczyć w samym kodzie.** Każdy
wymaga jednego zdania o świecie na zewnątrz pliku: że `uv_close` jest
asynchroniczne, że `nsec` ma zakres, że `color-mix()` pozwala pominąć procent
po jednej stronie.

Dlatego w handoucie dostałeś to zdanie przy każdym fragmencie — a agent
w kroku 2 **nie dostał go wcale**. To jest cała różnica między „krytyk
potrzebuje wiedzy o dziedzinie" (potrzebuje) a „krytyk potrzebuje wiedzy
o intencji autora" (nie — to mu wręcz przeszkadza).

---

# Część 2 — kandydat w Javie

Te trzy fragmenty to **nasze analogie**, nie cytaty z Buna. Ta sama klasa błędu,
język, który znasz.

## 1 · `Optional.orElse` z zachłanną ewaluacją — izomorf **C**

```java
return override.orElse(this.policy.shippingPaidBy(items.getFirst().reason()));
```

`orElse` liczy argument **zawsze**. Jeśli `items` jest puste, `getFirst()` rzuca
`NoSuchElementException` — **nawet gdy `override` jest obecny i argument miał
zostać zignorowany**.

**Poprawka:** `override.orElseGet(() -> this.policy.shippingPaidBy(items.getFirst().reason()))`.

> To jest **dosłownie ten sam błąd co fragment C**, przetłumaczony na Javę.
> Rust jest „wow", ta linijka jest „o cholera, to mam w produkcji". Para
> `orElse`/`orElseGet` jest jednym z najczęstszych realnych błędów w Javie.

## 2 · Obcinanie w stronę zera dla wartości ujemnej — izomorf **B**

```java
var days = hoursSinceDelivery / 24;
var hours = hoursSinceDelivery - days * 24;
```

Dzielenie całkowite w Javie obcina **w stronę zera**. Dla `hoursSinceDelivery = -3`
daje `days = 0`, więc `hours = -3` — **ujemna reszta**, dokładnie jak ujemne
`nsec` w Bunie.

Wartość ujemna pojawia się, gdy `deliveredAt` jest w przyszłości: przesunięcie
zegara, backdatowana dostawa, błąd integracji.

**Poprawka:** `Math.floorDiv(hoursSinceDelivery, 24)` i `Math.floorMod(hoursSinceDelivery, 24)`.

## 3 · Zasób zamknięty, zanim użyje go zadanie asynchroniczne — izomorf **A**

```java
try (InputStream content = Files.newInputStream(auditDocument)) {
    CompletableFuture.runAsync(() -> this.auditSink.store(content));
}
```

`try-with-resources` zamyka `content` na końcu bloku. `runAsync` **nie czeka** —
zadanie startuje na innym wątku i sięga po strumień, który właśnie został
zamknięty.

Efekt: `IOException: Stream Closed` w losowym momencie, zależnie od szeregowania
wątków. **Czasem przechodzi** — jeśli zadanie zdąży, zanim blok się skończy.
Klasyczny heisenbug.

**Poprawka:** nie zamykaj w tym zakresie — przekaż ścieżkę zamiast strumienia
i otwórz go wewnątrz zadania, albo poczekaj na `CompletableFuture` przed
wyjściem z bloku.

## Czwarty błąd, którego tam nie zasadziliśmy

Prawie każdy agent-krytyk zgłosi **dzielenie przez zero** w `shippingPerItem`,
gdy `returnedItems == 0`.

To jest **poprawna uwaga**, ale nie jest jednym z trzech zasadzonych błędów.
I nie znaczy, że krytyk się pomylił — znaczy, że działa. Adversarial review nie
ma znajdować *twojej listy*, tylko powody, dla których kod nie zadziała.

Odsiewanie jego znalezisk jest **twoją** pracą, nie jego.

---

# Część 3 — przykład do roastu

`handouts/3-przyklad-do-roastu/` — `SPEC.md` z pięcioma wymaganiami
i `PrzypomnienieOWizycie.java` (65 linii). Kompiluje się czysto z `-Xlint:all`,
zero ostrzeżeń.

## Komplet znalezisk — dziewięć

| # | Znalezisko | Linia | Kiedy boli |
|---|---|---|---|
| 1 | `catch (Exception e)` łyka wyjątek **i mimo to dopisuje klucz do `wyslane`** | 54–56 | poczta pada na jednej wizycie; przypomnienie nie poszło, ale system zapisał, że poszło — **nie ponowi go nigdy**, a w logu nie ma śladu |
| 2 | **W4 nie istnieje w kodzie.** Pole `odwolana` jest w rekordzie i nie jest czytane ani razu | 14 (deklaracja), nigdzie indziej | właściciel odwołuje wizytę i dzień wcześniej dostaje „przypominamy o wizycie" |
| 3 | **Reguła weekendowa, której nikt nie zamawiał** | 30–33 | wizyta w poniedziałek: jedyny dzień z `dni == 1` to niedziela, wtedy metoda kończy się na `return`. W poniedziałek `dni == 0`. **Przypomnienie nie idzie nigdy** — ginie ok. 2/7 wszystkich |
| 4 | `wyslane` żyje w pamięci instancji i nigdy nie jest czyszczone | 21 | restart procesu kasuje zbiór → wszyscy dostają drugi e-mail; w długo żyjącym procesie zbiór rośnie bez końca |
| 5 | Pusty adres omija bramkę z W3 — sprawdzane jest tylko `null` | 41 | baza trzyma brak adresu jako pusty string (import z CSV, kolumna `NOT NULL` z domyślnym `''`) → `poczta.wyslij("", …)` |
| 6 | Strefa czasowa na sztywno | 28 | test na maszynie w UTC liczy inny „dzisiaj" niż produkcja; nie da się wstrzyknąć zegara, więc W1 i W2 są nietestowalne |
| 7 | Dopasowanie punktowe `dni != 1 && dni != 7`, brak nadrabiania | 36–39 | serwer nie wstał jednego dnia → przypomnienia z tego dnia przepadają bezpowrotnie |
| 8 | `NullPointerException` na jednym rekordzie wywraca cały przebieg | 36 i 41 | jedna wizyta z `termin == null` w środku listy → wizyty za nią nie dostają nic |
| 9 | Treść: „za 1 dni" | 63 | widzi to klient, przy każdym przypomnieniu dobowym |

**1 i 2 są najpoważniejsze.** Dobry sprawdzian, czy twój recenzent układa listę
po wadze, czy po numerze linii.

## Tabela pokrycia — tak powinna wyglądać

| # | Wymaganie | Linia | Werdykt |
|---|---|---|---|
| W1 | 7 dni przed terminem → przypomnienie | 36–39, 52 | **CZĘŚCIOWO** — bramka weekendowa (30–33) kończy metodę, zanim pętla ruszy |
| W2 | 1 dzień przed terminem → przypomnienie | 36–39, 52 | **CZĘŚCIOWO** — jw., a dla wizyt poniedziałkowych oznacza to brak przypomnienia w ogóle |
| W3 | brak e-maila → nie wysyłaj, zapisz w logu | 41–44 | **CZĘŚCIOWO** — łapie tylko `null`, pusty string przechodzi; `System.out.println` to nie jest log |
| W4 | wizyta odwołana → nie wysyłaj | — | **BRAK** |
| W5 | najwyżej jedno przypomnienie na wizytę na dobę | 21, 46–49 | **CZĘŚCIOWO** — klucz jest dobry, magazyn nie: „nawet jeśli zadanie uruchomi się kilka razy" nie działa po restarcie |

| Zachowanie niezamówione | Linia |
|---|---|
| cisza w soboty i niedziele | 30–33 |
| strefa `Europe/Warsaw` wpisana na stałe | 28 |
| treść i format wiadomości | 60–64 |
| `System.out` jako kanał logu | 42 |

## Pointa

**Cztery z pięciu wymagań to CZĘŚCIOWO, jedno to BRAK. Zielonego nie ma ani
jednego.**

Kod kompiluje się bez jednego ostrzeżenia i czyta się gładko. Gdyby ktoś podał
ci listę „znalazłem pięć błędów", uznałbyś, że wiesz, jak jest. Nie wiedziałbyś,
że **żadne wymaganie nie jest spełnione w całości** — bo lista błędów tego nie
mówi. Tabela mówi.

To jest cała różnica między *„znalazłem błędy"* a *„sprawdziłem wymagania i mam
dowód przy każdym"*.

## Uczciwie: ten przykład jest zasadzony

Dziewięć błędów w 65 liniach to nie jest realistyczna gęstość. Klasy błędów są
prawdziwe — pominięte wymaganie, dopisana reguła, połknięty wyjątek, stan
w pamięci — ale ich zagęszczenie zrobiliśmy po to, żeby ćwiczenie zmieściło się
w kwadransie.

Uczciwą próbę robisz na własnym kodzie: ★ „dwie osie na swoim".
