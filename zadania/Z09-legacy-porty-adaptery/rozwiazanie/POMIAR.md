# Z09 · Pomiar — ile naprawdę kosztuje pętla zwrotna

Ten moduł twierdzi, że port i adapter zwracają się **skróceniem pętli zwrotnej**.
To jest twierdzenie liczbowe, więc tutaj jest liczba, warunki, w jakich powstała,
i uczciwe ostrzeżenie, jak bardzo ta liczba skacze.

## Protokół

| | |
|---|---|
| **Kiedy** | 30 sierpnia 2026 |
| **Co** | petclinic na przypiętym commicie `818c4136`, z zastosowanym `rozwiazanie/` |
| **Komenda** | `mvn -B -s docs/setup/settings-central.xml -Dmaven.repo.local=.tooling/m2 test` |
| **JDK** | 25 z `.tooling/`, Maven 3.9.11 |
| **Maszyna** | Windows 11, `~/.m2` rozgrzane, **pierwszy** przebieg po starcie |
| **Skąd liczby** | `target/surefire-reports/TEST-*.xml`, atrybut `time` |

## Wynik

| | Testów | Czas | Co startuje |
|---|---:|---:|---|
| `UpdateOwnerTests` | 3 | **0,021 s** | nic — zwykłe `new` i atrapa portu |
| `OwnerControllerTests` | 15 | **12,028 s** | `@WebMvcTest`, kontekst Springa |
| **cały zestaw** | 77 uruchomionych, 2 pominięte | **1 min 14 s** | `BUILD SUCCESS` |

Stosunek na tym przebiegu: **około 570 razy**.

## I teraz najważniejsze: ta liczba nie jest stabilna

Wcześniejsze wersje tego materiału podawały dla `OwnerControllerTests`
**1,451 s** w jednym miejscu i **5,6 s** w drugim. Mój przebieg dał **12,0 s**.
Wszystkie trzy pomiary są prawdziwe — i wszystkie trzy dotyczą tego samego pliku.

Różnica bierze się z tego, co dokładnie liczysz:

- **czy kontekst Springa wstaje pierwszy raz** w tym przebiegu, czy jest już
  zbudowany dla innej klasy testowej i zostaje odzyskany z cache'a,
- **czy JVM zdążyła się rozgrzać** — pierwsza klasa testowa płaci za JIT,
- maszyna, dysk, antywirus, stan `~/.m2`.

`UpdateOwnerTests` nie zależy od żadnej z tych rzeczy, bo **nic nie startuje**.
Dlatego jego czas jest za każdym razem taki sam z dokładnością do milisekund,
a czas testu webowego skacze o rząd wielkości.

**Wniosek, który wolno wyciągnąć:** różnica jest między **milisekundami
a sekundami** — dwa do trzech rzędów wielkości. Konkretny mnożnik (130? 570?)
zależy od maszyny i nie jest wielkością, którą warto cytować.

**Wniosek, którego wyciągnąć nie wolno:** że port „przyspiesza testy X razy".
Nie przyspiesza żadnego testu. Sprawia, że regułę da się sprawdzić **bez** tego,
co jest wolne.

## Zmierz to u siebie

To jest dosłownie jedna komenda i twoja liczba jest lepsza niż nasza:

```bash
cd praca/Z09/spring-petclinic
mvn -B test
grep -h 'time=' target/surefire-reports/TEST-*owner.UpdateOwnerTests.xml \
                target/surefire-reports/TEST-*owner.OwnerControllerTests.xml
```
