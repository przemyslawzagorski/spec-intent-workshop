# harness-template

Działający system z kompletnym harnessem. Bierzesz to i zaczynasz od czegoś, co
już się buduje, ma testy, bramkę i CI — zamiast od pustego katalogu.

Dwa serwisy, dwa języki, broker między nimi. Nie dlatego, że lubimy komplikacje,
tylko dlatego, że dopiero na granicy dwóch serwisów widać, czy kontrakt jest
naprawdę kontraktem, czy tylko wspólną klasą w tym samym repo.

```
returns-service/    Quarkus 3.35.1, Java 25, układ BCE, Postgres przez JDBC
scoring-worker/     FastAPI, Python 3.12, liczy wskaźnik nadużyć + asystent LLM
return-policy.yaml  wartości: okna, progi, wyłączone kategorie
docs/contract/      openapi.yaml, procedura decyzyjna, schematy zdarzeń
docs/adr/           trzy decyzje architektoniczne z uzasadnieniem
tools/              generatory tabel testowych i walidator polityki
bramka              jedna komenda, która mówi, czy repo jest zdrowe
compose.yaml        pełne uruchomienie end-to-end
```

## Sprawdź, że działa

```bash
./bramka
```

Zajmuje około dwóch minut. Uruchamia **50 testów Javy** i **23 Pythona**, waliduje
politykę i sprawdza cztery reguły architektoniczne. Ten sam plik uruchamia CI —
patrz `.github/workflows/ci.yml` i `.gitlab-ci.yml`. To nie przypadek: bramka,
która ma dwie wersje, rozjedzie się w tydzień.

Czego potrzebujesz: **JDK 25**, **Maven**, **uv**, **Docker**. Docker jest
niezbędny — Quarkus Dev Services stawia na nim Postgresa i Redpandę do testów,
sam, bez konfiguracji.

Za firmowym proxy: `export MAVEN_ARGS="-s /ścieżka/do/settings.xml"`.

## Trzy rzeczy, dla których to repo w ogóle powstało

### 1 · Testy generowane ze specyfikacji, nie pisane ręcznie

`return-policy.yaml` mówi, że okno dla elektroniki to 30 dni. Nigdzie w kodzie
testów nie ma liczby 30.

```bash
uv run tools/policy_cases.py return-policy.yaml
```

Wypada tabela TSV: przypadek, wejście, oczekiwana decyzja. Powstaje z polityki
przepuszczonej przez wspólną procedurę z `docs/contract/decision-procedure.md`.
Testy Javy czytają tę tabelę i wykonują wiersz po wierszu.

Zmień `30` na `21` w polityce i puść `./bramka`. **Wszystko zostaje zielone** —
bo tabela i aplikacja czytają ten sam plik. Wygeneruj tabelę ponownie i zobacz,
że przypadki graniczne przesunęły się z 29/30/31 na 20/21/22.

**Wartości biznesowe są danymi, nie kodem.** Nigdzie nie ma zapisanej liczby 30,
więc nie ma czego zapomnieć zmienić.

Ten sam wzorzec dla drugiego serwisu i drugiego języka:
`uv run tools/score_cases.py return-policy.yaml`.

### 2 · Podział na HARD i SOFT

`./bramka` rozróżnia dwie rzeczy, które zwykle się myli:

| | Co robi | Przykład stąd |
|---|---|---|
| **HARD** | nie wpuszczam, CI staje | testy czerwone, `control` importuje `jakarta.ws.rs` |
| **SOFT** | zwracam uwagę, przepuszczam | nowa zależność bez ADR |

Bez tego podziału dzieje się jedna z dwóch rzeczy: albo wszystko jest twarde
i ludzie zaczynają obchodzić bramkę, albo wszystko jest miękkie i bramki nie ma.

Reguła „każda zależność ma ADR" jest tu SOFT celowo. Zobacz, jak jest napisana
w `bramka` — listę zależności czyta z `pom.xml`, nie z własnej pamięci. Reguła,
którą trzeba ręcznie aktualizować, przestaje działać w drugim tygodniu.

### 3 · LLM za portem

`scoring-worker` ma asystenta, który woła model. Interfejs `LlmPort`
(`src/scoring/control/llm_port.py`) oddziela go od dostawcy. W testach wchodzi
odtwarzacz nagranych odpowiedzi — stąd 23 testy, które przechodzą offline,
w sekundy i za darmo.

Do tego dwie rzeczy, o których łatwo zapomnieć:

- **Ograniczona lista narzędzi.** Asystent widzi tylko to, co mu damy.
- **Hook blokujący w kodzie** (`src/scoring/control/tools.py`). Sprawdza wywołanie
  przed wykonaniem i potrafi odmówić.

To drugie jest istotne. **Prompt nie jest mechanizmem bezpieczeństwa — jest prośbą.**
Jeśli model nie ma czegoś zrobić, to musi tego nie móc zrobić, a nie zostać
poproszony, żeby nie robił.

## Zobacz to w całości

```bash
docker compose up --build
curl -s localhost:8080/q/health/ready
curl -s localhost:8000/q/health/ready
```

Postgres, Redpanda i oba serwisy. Do bramki to **nie jest** potrzebne — testy
stawiają sobie infrastrukturę same. Jeśli twoja bramka wymaga postawienia całego
systemu, to nie jest bramka, tylko środowisko.

## Co przeczytać najpierw

| Plik | Dlaczego |
|---|---|
| `bramka` | 80 linii, a definiuje, co znaczy „gotowe" w tym repo |
| `docs/contract/decision-procedure.md` | reguła biznesowa napisana tak, że da się z niej wygenerować testy |
| `returns-service/.../returns/package-info.java` | specyfikacja komponentu w EARS, przy kodzie |
| `docs/adr/001-postgres-bez-orm.md` | jak wygląda decyzja z uzasadnieniem, a nie z opinią |
| `scoring-worker/src/scoring/control/tools.py` | hook, który mówi „nie" |

## Licencja i pochodzenie

Kod napisany na potrzeby warsztatu.

Układ BCE — `boundary` wie o HTTP, `control` o logice, `entity` o niczym —
i część konwencji wzorowane na
[AdamBien/quarkus-microprofile](https://github.com/AdamBien/quarkus-microprofile)
(MIT), commit `4186f2c6d5b01f76fbc8385ad4581bfe01d6c580`. Polecam oryginał —
jest czystszy i mniejszy, bo nie musi niczego tłumaczyć.
