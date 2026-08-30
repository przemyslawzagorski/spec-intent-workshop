# Z11 · Finał: gotowy template

**50 min** · harness-template · bramka: `./sprawdz Z11`

## O co chodzi

Dostajesz działający system z kompletnym harnessem — Quarkus, FastAPI, Kafka,
Postgres, kontrakt, ADR-y, bramka, CI. **To jest twoje, zabierasz do domu.**

Zadanie: dołóż do niego jedną rzecz, przechodząc pełny cykl. Specyfikacja,
kontrakt, test, implementacja, bramka. Na czymś, co już stoi — bo dokładanie
do działającego systemu wygląda zupełnie inaczej niż budowanie od zera.

## Jak zwykle to robimy

Nowy endpoint powstaje tak: ktoś dopisuje metodę w kontrolerze, potem
zapytanie, potem — jeśli starczy czasu — test. Dokumentacja API zostaje
z poprzedniej wersji. Ktoś inny odkrywa rozbieżność za trzy miesiące.

Boli, bo:

- **Kontrakt i kod rozjeżdżają się po cichu.** Nic tego nie pilnuje.
- **Test powstaje na końcu**, więc sprawdza to, co kod robi, a nie to,
  co miał robić.
- **Decyzje brzegowe zapadają w trakcie pisania.** Pusta lista czy 404?
  Nikt nie pytał, ktoś wybrał.

## Jak zrobić dobrze

Kolejność, w której każdy krok ogranicza następny:

**1 · Powiedz, co ma się dziać.** Trzy–cztery zdania w EARS. Wystarczy.
Nie piszesz dokumentu, tylko rozstrzygasz brzegi.

**2 · Zapisz to w kontrakcie.** `docs/contract/openapi.yaml`. Teraz masz
odniesienie, do którego można porównać implementację.

**3 · Napisz test.** Przez publiczną powierzchnię, nie przez zaglądanie
do środka. Ten sam test ma przejść niezależnie od tego, jak zbudujesz wnętrze.

**4 · Dopiero teraz agent pisze kod.** Ma spec, kontrakt i czerwony test.
Ma też bramkę, która nie pozwoli mu poprawić testu zamiast kodu.

**5 · Bramka.** `./bramka` — jedna komenda, ta sama co w CI.

**Skill, który tu pasuje:** `tdd` z zestawu Matta Pococka — najpierw test,
potem kod. Działa od ręki, bez konfiguracji.

Nie polecam tu `implement` ani `to-spec`, choć nazwy kuszą: obydwa zakładają,
że repo ma skonfigurowany **issue tracker** (`to-spec` mówi wprost: *„The issue
tracker and triage label vocabulary should have been provided to you"*).
W piaskownicy warsztatowej go nie ma, więc skill zacznie od proszenia
o konfigurację zamiast od roboty.

Bez skilli prompt [prompty/pelny-cykl.md](prompty/pelny-cykl.md) robi to samo.

## Zrób to

```bash
./przygotuj Z11
cd praca/Z11/harness-template
```

**1 · Uruchom, zanim cokolwiek zmienisz** (10 min):

```bash
source ../../../.tooling/env.sh    # jeśli jeszcze nie
./bramka
```

**Co zobaczysz:** dwie sekcje — `HARD` i `SOFT` — i na końcu
`Bramka otwarta.` Każda linia zaczyna się od `ok`.

**Ile trwa:** około **90 sekund** za pierwszym razem, **40 sekund** potem.
Docker musi działać — Quarkus Dev Services stawia sobie Postgresa i Redpandę
sam, bez żadnej konfiguracji z twojej strony.

Rozejrzyj się. Cztery pliki warte przeczytania są wypisane na końcu
[README template'u](../../harness-template/README.md).

**2 · Zobacz mechanizm, dla którego to repo powstało** (5 min):

```bash
uv run tools/policy_cases.py return-policy.yaml | head -5
```

**Co zobaczysz:** nagłówek TSV i cztery wiersze — przypadki `W01`–`W04`
z kolumnami `caseId`, `opis`, `category`, `daysSinceDelivery` i oczekiwaną decyzją.

Zwróć uwagę na `W04`–`W06`: to przypadki graniczne dla elektroniki — **29, 30
i 31 dni**. Nikt ich nie wpisał ręcznie; policzył je generator z pliku polityki.

Teraz zmień `elektronika: 30` na `21` w `return-policy.yaml` i puść `./bramka`.

**Wszystko zostanie zielone** — i o to chodzi. Wygeneruj tabelę jeszcze raz
i porównaj: granice przesunęły się na **20, 21 i 22 dni**. Przesunęła się też
aplikacja, bo czyta ten sam plik. **Nigdzie w kodzie ani w testach nie ma
zapisanej liczby 30.**

Chcesz zobaczyć, że bramka ma zęby? Wpisz wartość na sztywno w
`returns-service/src/main/java/workshop/rma/returns/control/EligibilityCheck.java`,
**linie 127 i 129** — obie, bo jedna to kategoria, druga to wartość domyślna:

```java
.mapToInt(l -> this.policy.windowDays(l.category()))   // → .mapToInt(l -> 30)
.orElseGet(() -> this.policy.windowDays("default"));   // → .orElseGet(() -> 30);
```

**Musisz zmienić obie.** Po zmianie tylko jednej bramka zostaje zielona
i wyciągniesz wniosek odwrotny do zamierzonego.

Puść `./bramka` — teraz zobaczysz `BLAD returns-service: testy`
i `Bramka zamknieta`. Potem cofnij wszystko: `git checkout -- .`

**3 · Dołóż endpoint pełnym cyklem** (25 min).

Zadanie: **lista zwrotów klienta.**

```
GET /returns?customerId={uuid}
```

Przejdź kroki w kolejności. Prompt, który tego pilnuje:
[prompty/pelny-cykl.md](prompty/pelny-cykl.md).

Rzeczy, które musisz rozstrzygnąć **zanim** agent zacznie pisać:

- Klient bez żadnych zwrotów — pusta lista czy 404?
- Brak parametru `customerId` — 400 czy lista wszystkiego?
- W jakiej kolejności zwracasz wyniki?
- Skąd w ogóle wiadomo, czyj jest zwrot? (Zajrzyj do `Schema.java` —
  tabela `returns` **nie ma** `customer_id`.)

**4 · Bramka:**

```bash
./bramka
cd ../../.. && ./sprawdz Z11
```

**Co sprawdza `./sprawdz Z11`.** Nie „czy testy są zielone" — template przychodzi
zielony i to nic nie znaczy. Sprawdza **ślady trzech ostatnich kroków cyklu**:
czy `docs/contract/openapi.yaml` zna parametr `customerId`, czy jakiś test go
wspomina, i czy **bramka template'u jest otwarta**. Ta ostatnia robi więcej niż
`mvn test`: waliduje politykę, przegenerowuje tabelę decyzji z aktualnego pliku
i pilnuje, żeby warstwa `control` nie wiedziała o HTTP.

Kolejność sprawdzeń jest kolejnością cyklu. Jeśli zaczniesz od kodu, pierwsze
dwie linie powiedzą ci o tym wprost.

**5 · Najważniejszy krok całego warsztatu** (10 min).

Wszystko powyżej działo się na naszym repo. **Ta rzecz ma się wydarzyć u ciebie.**

Otwórz projekt, przy którym pracujesz na co dzień, i przenieś do niego
**jeden** element:

- `bramka` — nawet w wersji na trzy sprawdzenia,
- albo `tools/repo_policy.py` z odciskiem testów,
- albo sam plik `AGENTS.md` z Z01.

Nie musisz tego dziś dokończyć. **Musisz zacząć, póki masz pod ręką kogoś,
kogo można zapytać.**

Jeśli nie masz przy sobie firmowego repo — napisz w trzech zdaniach, co
konkretnie przeniesiesz i od czego zaczniesz. To też się liczy.

## Pytanie na czat

**Ile testów macie po dołożeniu endpointu?** Format: `53`.
I jednym zdaniem: **pusta lista czy 404 — co wybraliście i dlaczego.**

## Omówienie

Poproszę o ekran kogoś, kto wybrał 404, i kogoś, kto wybrał pustą listę.

Pogadamy o:

- **Że to nie jest kwestia gustu.** 404 znaczy „nie ma takiego zasobu".
  Ale ten serwis nie wie, czy klient istnieje — zna tylko zamówienia.
  Zwracając 404, kłamałby o czymś, czego nie sprawdził. **Pusta lista jest
  jedyną odpowiedzią, którą ten serwis ma prawo dać.** Do takiego wniosku
  dochodzi się przez czytanie schematu, nie przez intuicję.
- **Skąd bierze się `customer_id`.** Tabela `returns` go nie ma. Trzeba
  dołączyć `orders`. Kto tego nie sprawdził, dostał od agenta zapytanie
  do kolumny, która nie istnieje.
- **Co zrobiła bramka.** U mnie od razu wyłapała, że `control` nie może
  importować `jakarta.ws.rs` — reguła, o której się nie pamięta, dopóki
  nie zapali.
- **Że to samo dałoby się zrobić bez niczego z tego.** I zajęłoby dziesięć
  minut zamiast dwudziestu pięciu. Różnica jest widoczna dopiero przy
  trzydziestym endpoincie i przy drugiej osobie w zespole.

## Kiedy to NIE ma sensu

Cały ten aparat — kontrakt, generowane tabele, ADR-y, dwie bramki — jest
przerostem formy dla skryptu, dla prototypu i dla projektu, który robisz sam
przez tydzień. **Powiedz to sobie zanim ktoś inny powie to tobie.**

Sensowny próg: więcej niż jedna osoba, dłużej niż kwartał, i ktoś inny niż ty
będzie to zmieniał.

## ★ Jeśli skończyłeś wcześniej

| ★ | Co robisz | Min |
|---|---|---|
| **Uruchom całość** | `docker compose up --build`, potem pełne E2E przez HTTP. Zobacz, jak zdarzenie przechodzi przez Kafkę do workera. | 20 |
| **Zdarzenie do Kafki** | Dołóż zdarzenie wraz z testem kontraktowym schematu w `docs/contract/events/`. | 30 |
| **Nowa reguła w polityce** | Dodaj pole do `return-policy.yaml`, rozszerz `decision-procedure.md` i generator `policy_cases.py`. Zobacz, jak jedna wartość przebudowuje całą tabelę testów. | 30 |
| **Drugi endpoint** | Tym samym cyklem, ale bez patrzenia w prompt. Zmierz, czy poszło szybciej. | 30 |
| **Zepsuj kontrakt celowo** | Zmień typ pola w `openapi.yaml` i sprawdź, czy cokolwiek to złapie. Jeśli nie — dołóż regułę do `bramka`. | 20 |

## Rozwiązanie

[rozwiazanie/](rozwiazanie/) — cztery diffy. Sprawdzone: **53 testy Javy
i 23 Pythona zielone**, cała `./bramka` otwarta.

| Diff | Co w nim jest |
|---|---|
| `openapi.yaml.diff` | kontrakt: parametr, 200 z listą, 400 bez `customerId` |
| `Returns.java.diff` | zapytanie z joinem do `orders`, wspólne mapowanie wiersza |
| `ReturnsResource.java.diff` | endpoint, 400 przy braku parametru |
| `ReturnsResourceTest.java.diff` | trzy testy: kolejność, pusta lista, brak parametru |

Zwróć uwagę na jedną rzecz w `Returns.java.diff`: dołożenie drugiego odczytu
wymusiło wydzielenie wspólnego mapowania wiersza. **Drugi przypadek użycia
zawsze pokazuje, co w pierwszym było przypadkowe.**
