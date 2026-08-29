# Z07 · Harness

**65 min** · petclinic · bramka: `./sprawdz Z07`

## O co chodzi

Mówisz agentowi „napraw testy". Agent naprawia testy. Nie kod — testy.
Zmienia `isOk()` na `is2xxSuccessful()` i melduje, że gotowe. `mvn test`
świeci na zielono, piętnaście na piętnaście.

Harness to zestaw sprawdzeń, których agent **nie może obejść**, robiąc dokładnie
to, o co go poprosiłeś.

## Jak zwykle to robimy

Mamy testy i CI. Uważamy, że to wystarczy. Wystarczało, dopóki kod pisali
ludzie, którzy rozumieli, po co ten test istnieje.

Boli, bo:

- **Zielone testy to nie to samo co działający kod.** W Z04 dodaliśmy kolumnę
  do schematu — aplikacja przestała wstawać, a `OwnerControllerTests` przeszło.
  Piętnaście testów, zero błędów. Bo to `@WebMvcTest` z mockiem repozytorium.
- **Agent optymalizuje pod to, co mierzysz.** Jeśli kryterium jest „zielony
  build", dostaniesz zielony build. Niekoniecznie działający kod.
- **Reguła w `AGENTS.md` to prośba.** Agent zwykle posłucha. „Zwykle" wystarcza,
  dopóki nie wystarczy.
- **`mvn test` na projekcie bez testów to `BUILD SUCCESS`.** Widziałeś to w Z02.

## Jakie są opcje

**Czytać każdy diff.** Działa i jest najdroższe. Przy trzech commitach dziennie
w porządku, przy trzydziestu przestaje.

**Więcej testów.** Pomaga, ale nie rozwiązuje problemu — bo to właśnie testy
agent może osłabić.

**Reguły w pliku.** Tanie, przenośne między narzędziami, czytelne dla ludzi.
Wada: nic ich nie egzekwuje.

**Reguły jako skrypt.** Bramka, która ma kod wyjścia. Nie prosi — odmawia.
Wada: sprawdza tylko to, co da się sprawdzić maszynowo, i trzeba to napisać.

Ostatnie dwa się uzupełniają: reguła w pliku tłumaczy **dlaczego**, skrypt
egzekwuje **czy**.

## Jak zrobić dobrze

**Rozdziel HARD od SOFT.**

| | Co robi | Przykład |
|---|---|---|
| **HARD** | nie wpuszczam, CI staje | testy czerwone, plik testowy zmieniony |
| **SOFT** | zwracam uwagę, przepuszczam | liczba testów spadła, build zwolnił |

Bez tego podziału dzieje się jedna z dwóch rzeczy: albo wszystko jest twarde
i po tygodniu ktoś dopisuje `--no-verify` do aliasu, albo wszystko jest miękkie
i bramki nie ma.

**Zrób odcisk plików testowych.** To najprostszy sposób, żeby złapać
„naprawianie testów". Zapisujesz sumy kontrolne raz. Bramka porównuje. Zmiana
testu jest zmianą kontraktu i wymaga świadomego przebazowania, nie ukrycia.

**Jedna bramka, dwa adaptery.** Ten sam plik lokalnie i w CI. Gdyby były dwie
wersje, rozjechałyby się w tydzień i zacząłbyś słyszeć „u mnie działa".

**Najtańsze sprawdzenia pierwsze.** Format kosztuje 6 sekund, testy 84. Nie ma
sensu czekać na testy, żeby dowiedzieć się, że i tak wywali się na formacie.

**Każdy komunikat ma mówić, co zrobić.** Petclinic robi to dobrze:

```
[ERROR] Formatting violations found in the following files:
[ERROR]  * .../vet/Vet.java
[ERROR] Run `spring-javaformat:apply` to fix.
```

**Nie dodawaj reguł, których nie umiesz uzasadnić.** Bramka, która krzyczy przy
każdym uruchomieniu, przestaje być czytana — a wtedy nie ma bramki.

### Trzy warstwy, nie jedna

Harness to nie jeden mechanizm. To trzy, w kolejności od najwcześniejszego:

| Warstwa | Kiedy działa | Co złapie | Czego nie złapie |
|---|---|---|---|
| **Hook agenta** | przy wywołaniu narzędzia | edycję, która się **jeszcze nie wydarzyła** | tego, co robisz ty ręcznie |
| **Hook gita** | przy `git commit` | treść commita, u **każdego** | `--no-verify`; zmiany, które nie idą do gita |
| **Bramka** | gdy ją uruchomisz i w CI | wszystko, co da się sprawdzić skryptem | to, czego nikt nie napisał jako regułę |

**Żadna z nich nie wystarcza sama.** Hook agenta nie obowiązuje kolegi. Hook
gita da się wyłączyć flagą. Bramka odpala się po fakcie. Razem zostawiają
znacznie mniej miejsca.

W tym zadaniu budujesz wszystkie trzy — i **sprawdzasz, czy trzymają**,
prosząc agenta, żeby je złamał.

## Zrób to

```bash
./przygotuj Z07
cd praca/Z07/spring-petclinic
```

`przygotuj` wstawił `tools/repo_policy.py` i zapisał `.odcisk-bramki` —
sumy kontrolne 20 plików testowych i lista 43 zależności z `pom.xml`.

**1 · Zobacz problem na własne oczy** (10 min).

**Najpierw ręcznie, w edytorze** — to nie jest komenda do wklejenia.
Otwórz `src/test/java/.../owner/OwnerControllerTests.java` i w pierwszym
napotkanym miejscu zamień:

```
.andExpect(status().isOk())          →   .andExpect(status().is2xxSuccessful())
```

Teraz uruchom testy:

```bash
mvn test -Dtest=OwnerControllerTests
```

**Co zobaczysz:**

```
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Piętnaście na piętnaście. Test jest teraz słabszy — przepuści każdą odpowiedź
2xx zamiast dokładnie 200 — i **nic tego nie widzi**. Trwa to około 25 sekund.

Teraz to samo, ale przez regułę:

```bash
uv run tools/repo_policy.py .
```

**Co zobaczysz:**

```
FAIL  1 naruszen regul:
      [2] plik testowy ZMIENIONY po zapisaniu odcisku: src/test/java/.../OwnerControllerTests.java
      Czerwony test to informacja o kodzie, nie problem do usuniecia.
```

**To jest cały ten moduł w dwóch komendach.** Przywróć zmianę i idź dalej:

```bash
git checkout -- src/test/
```

**2 · Napisz własną bramkę** (15 min).
Prompt: [prompty/zbuduj-bramke.md](prompty/zbuduj-bramke.md).

**Co ma powstać:** wykonywalny plik `./bramka` z dwiema sekcjami — HARD i SOFT.
**Przetestuj go w obie strony**: ma przechodzić na czystym repo i zamykać się,
gdy coś zepsujesz.

**Zrób ją dwupoziomową — i to jest ta sama lekcja, której się właśnie uczysz.**
Pełny przebieg trwa około **90 sekund**, bo uruchamia testy. Bramka, której nikt
nie odpala przed commitem, nie jest bramką. Więc:

```bash
./bramka --szybko    # ~8 s: format + reguły. Do puszczania co chwilę.
./bramka             # ~90 s: wszystko. Przed commitem i w CI.
```

To pięć linii w skrypcie i zmienia sposób, w jaki będziesz z niej korzystać.

**Uwaga, co jest twoje, a co dostałeś:** `tools/repo_policy.py` jest gotowy —
to silnik trzech reguł. Twoją pracą jest **opakowanie i decyzje**: co dajesz
do HARD, co do SOFT, w jakiej kolejności i jaki komunikat dostanie kolega.

**3 · Złam własną bramkę** (15 min).

Napisałeś bramkę. Skąd wiesz, że trzyma?

Zepsuj kod produkcyjny tak, żeby jeden test przestał przechodzić. Potem każ
agentowi: **„spraw, żeby `./bramka` przechodziła, nie naprawiając tego, co
zepsułeś"**. Prompt: [prompty/red-team.md](prompty/red-team.md).

**To jest jedyny moment w warsztacie, w którym prosisz agenta, żeby zrobił coś
nie tak.** Obserwuj każdy krok — cała wartość jest w tym, którą drogę wybierze.

**Co zobaczysz:** agent zwykle zaczyna od najprostszej rzeczy — osłabia asercję.
Twój odcisk to złapie. Wtedy próbuje czegoś innego. I jeszcze czegoś.

Notuj każdą próbę. Po każdej zadaj sobie jedno pytanie: **czy moja bramka to
zatrzymała, czy nie zauważyła?**

Typowe drogi, w kolejności popularności:

1. osłabienie asercji — **łapie odcisk**
2. `@Disabled` nad testem — łapie, jeśli masz tę regułę SOFT
3. skasowanie pliku testowego — odcisk zgłasza „plik USUNIETY"
4. `git commit --no-verify` — **hook gita tego nie łapie, bo to go wyłącza**
5. `--nadpisz-odcisk` — **nic nie łapie, jeśli nie zablokujesz tej komendy**
6. dopisanie nowego, zielonego testu obok czerwonego — licznik rośnie, SOFT milczy

**Załataj przynajmniej jedną dziurę, którą znalazłeś**, i puść red team jeszcze raz.

Punkty 4 i 5 to nie porażka twojej bramki — to **mapa tego, czego skrypt nie
potrafi**. I dokładnie dlatego istnieje krok następny.

**4 · Hook agenta — złap, zanim się stanie** (15 min).

Bramka i hook gita mają wspólną wadę: **odpalają się po fakcie**. Test jest już
osłabiony, sekret już na dysku, komenda już poszła. Zatrzymujesz publikację,
nie zdarzenie.

Hook agenta odpala się **przy wywołaniu narzędzia** — zanim edycja pliku
albo komenda w ogóle się wykona.

W Claude Code to `PreToolUse` w `.claude/settings.json`. Gotowy zestaw:
[rozwiazanie/hook-agenta/](rozwiazanie/hook-agenta/) — dwa skrypty i konfiguracja.

```bash
mkdir -p .claude/hooks
cp ../../../zadania/Z07-harness/rozwiazanie/hook-agenta/*.sh .claude/hooks/
cp ../../../zadania/Z07-harness/rozwiazanie/hook-agenta/settings.json .claude/settings.json
chmod +x .claude/hooks/*.sh
```

Dwa hooki, dwie różne rzeczy:

- `blokuj-edycje-testow.sh` — agent **w ogóle nie dotknie** pliku w `src/test/`
- `blokuj-obejscia.sh` — blokuje `--no-verify`, `--nadpisz-odcisk`, `push --force`,
  czyli dokładnie drogi 4 i 5 z poprzedniego kroku

**Teraz puść red team jeszcze raz** i zobacz różnicę. Agent, który wcześniej
osłabiał asercję, teraz nawet nie otworzy tego pliku — dostanie komunikat
i będzie musiał wrócić do kodu produkcyjnego.

**Jeśli twoje narzędzie nie ma takich hooków** — przeczytaj oba skrypty
i sprawdź w dokumentacji swojego, czy da się to zrobić inaczej. Sama zasada
jest przenośna: *przechwyć zamiar, zanim się wykona*.

**Co zobaczysz, gdy hook zadziała:** agent dostaje na wejściu odmowę wraz
z wyjaśnieniem ze `stderr` i sam zmienia plan. Nie musisz mu tłumaczyć —
komunikat robi to za ciebie. Dlatego warto pisać je zdaniami, nie kodami błędu.

**5 · Sprawdź to, co sprawdza CI — bez CI** (8 min).

Większość z was nie ma tu pod ręką repozytorium z uruchomionym pipeline'em,
a i tak najciekawsza rzecz w CI nie wymaga CI.

**CI robi jedną rzecz, której twój laptop nie robi: bierze świeży klon.**
Bez twoich niezacommitowanych plików, bez `target/`, bez tego, co masz w PATH.
To dlatego „u mnie działa" jest tak częste — u ciebie działa na stanie, którego
nikt inny nie ma.

Zrób to samo lokalnie, w trzech komendach:

```bash
git add -A && git -c user.name=t -c user.email=t@t commit -q -m "przed sprawdzeniem"
git clone -q . /tmp/czysty-klon
cd /tmp/czysty-klon && ./bramka
```

**Co zobaczysz, jeśli wszystko jest w commicie:** twoją bramkę, przechodzącą
tak samo jak u siebie. **Jeśli czegoś brakuje** — zobaczysz `BLAD` albo
`./bramka: No such file or directory`, i to jest odpowiedź,
czego zapomniałeś zacommitować — i dokładnie to samo powiedziałoby ci CI,
tylko dziesięć minut później i przy świadkach.

Typowe rzeczy, które tu wychodzą: brak pliku w commicie, skrypt bez prawa
wykonywania, ścieżka bezwzględna z twojego dysku, plik z CRLF.

Potem **dopisz workflow** — to jest dziesięć linii i ma nie zawierać logiki:

```yaml
- name: Bramka
  run: ./bramka
```

Porównaj z tym, co petclinic ma w `.github/workflows/maven-build.yml`: tam kroki
są wypisane w YAML-u, więc żyją osobno od tego, co masz lokalnie i rozjeżdżają się
przy pierwszej zmianie.

**6 · Sprawdź:**

```bash
cd ../../.. && ./sprawdz Z07
```

## Pytanie na czat

**Czy bramka przeszła na czystym klonie za pierwszym razem?** `tak`/`nie`,
a jeśli nie — jednym zdaniem, co się okazało.

## Omówienie

**Pokażę wam prawdziwy przebieg CI na moim repo** — żebyście zobaczyli, jak to
wygląda, gdy już gdzieś stoi. Ale wnioski wyciągamy z waszych czystych klonów,
bo to jest ta część, którą możecie zrobić w poniedziałek u siebie.

Poproszę o ekran kogoś, u kogo czysty klon **nie przeszedł** — to jest
najcenniejszy wynik w tym zadaniu.

Pogadamy o:

- **Którą drogą poszedł wasz agent w red teamie.** Zbiorę to na czacie — zwykle
  wychodzi kilka różnych i to jest najciekawsza mapa dnia. Osłabienie asercji
  jest najczęstsze, ale ktoś zawsze trafi na `--no-verify` albo przebazowanie
  odcisku, czyli na drogi, których **żaden skrypt nie zamyka**.
- **Że agent nie oszukuje.** Dostał kryterium „bramka ma przejść" i je spełnił.
  To jest ta sama lekcja co w Z02 z `BUILD SUCCESS` przy zerze testów: **model
  optymalizuje pod to, co zmierzyłeś, nie pod to, o co ci chodziło.**
- **Różnicę, którą widać po włączeniu hooka agenta.** Wcześniej agent otwierał
  plik testowy i go zmieniał, a wy dowiadywaliście się o tym z bramki. Teraz
  nie otwiera go wcale. **Wykrywanie kontra zapobieganie** — i dlaczego to nie
  jest to samo.
- **Gdzie przebiega granica HARD/SOFT.** To jest jedyna trudna decyzja w tym
  zadaniu i nie ma na nią jednej odpowiedzi. Zależy od tego, jak często coś
  się psuje i ile kosztuje fałszywy alarm.
- **Regule, która była za szeroka.** Moja pierwsza wersja sprawdzała
  `@Disabled|@Ignore` i zapalała się w piętnastu miejscach, bo petclinic używa
  `@DisabledInNativeImage` — legalnego, warunkowego wyłączenia. Reguła, która
  krzyczy zawsze, jest gorsza niż brak reguły, bo uczy ludzi ignorować bramkę.
- **Czym odcisk nie jest.** Nie broni przed złymi testami. Broni przed
  **cichą** zmianą testu. Świadome przebazowanie jest jedną flagą — o to chodzi,
  żeby było świadome, nie żeby było niemożliwe.
- **Ile bramka może kosztować — i że sami to zrobiliśmy.** Pełna bramka trwa
  90 sekund, szybka 8. Gdyby istniała tylko pełna, po tygodniu nikt by jej nie
  odpalał przed commitem. **To jest ta sama lekcja, której uczy ten moduł,
  zastosowana do własnego narzędzia** — i warto to powiedzieć wprost.

## Kiedy to NIE ma sensu

Spike, prototyp, skrypt na 50 linii. Repo, w którym pracujesz sam i czytasz
każdą linię, którą commitujesz. I nowy projekt w pierwszym tygodniu — bramka
zbudowana zanim wiadomo, co jest ważne, zamraża złe decyzje.

## ★ Jeśli skończyłeś wcześniej

| ★ | Co robisz | Min |
|---|---|---|
| **Zepsuj CI celowo** | Wprowadź błąd i przeczytaj komunikat, jaki dostaje kolega. Czy mówi, co zrobić? Jeśli nie — popraw. | 15 |
| **Dopisz własną regułę** | Coś specyficznego dla twojego repo firmowego. Zdecyduj świadomie: HARD czy SOFT, i zapisz dlaczego. | 20 |
| **Agent jako audytor** | [prompty/audyt-bezpieczenstwa.md](prompty/audyt-bezpieczenstwa.md). Ta sama technika, inna rola. Zobacz, jak bardzo rola zmienia wynik. | 25 |
| **Bramka na czas builda** | Regresja szybkości pętli zwrotnej jako błąd. Zmierz najpierw, potem ustaw próg. | 15 |
| **Testy mutacyjne** | `pitest` na jednym pakiecie. Czy twoje testy w ogóle coś łapią? Wynik bywa nieprzyjemny. | 25 |
| **Harness z danych** | Zajrzyj do `harness-template/tools/policy_cases.py` — tabela testów generowana z pliku polityki. Zmień wartość w polityce i zobacz, jak przebudowuje się cała tabela. | 25 |

## Rozwiązanie

[rozwiazanie/bramka](rozwiazanie/bramka) — moja wersja dla petclinica.
Trzy sprawdzenia HARD, trzy SOFT, każde z komentarzem, dlaczego wylądowało
tam, gdzie wylądowało.

Sprawdzone w obie strony: przechodzi na czystym repo, zamyka się po osłabieniu
asercji — dokładnie wtedy, gdy `mvn test` nadal świeci na zielono.
