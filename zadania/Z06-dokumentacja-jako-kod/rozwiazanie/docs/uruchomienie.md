# Uruchomienie

## Wymagania

Java 17 lub nowsza — tak mówi `pom.xml`. Sprawdzone na JDK 25.

## Komendy

```bash
mvn spring-boot:run                    # http://localhost:8080
mvn test                               # 76 testów
mvn test -Dtest=OwnerControllerTests   # 15 testów
mvn spring-javaformat:apply            # naprawia formatowanie
```

## Ile to trwa

Zmierzone na JDK 25 i Mavenie 3.9.11:

| Co | Czas |
|---|---|
| pierwszy build, puste `~/.m2` | ~6 min, ~151 MB pobrania |
| `mvn test` na rozgrzanym | ~84 s |
| jedna klasa testowa | ~23 s |
| `mvn validate` (sam format) | ~6 s |

W pętli zwrotnej używaj pojedynczej klasy testowej. Pełny zestaw przed commitem.

## Baza

Domyślnie **H2 w pamięci** — znika przy restarcie. Schemat i dane startowe
ładują się z `src/main/resources/db/h2/`.

Postgres i MySQL są w `docker-compose.yml` i mają własne schematy
w `db/postgres/` oraz `db/mysql/`.

!!! danger "Schematy są trzy"
    Dodając kolumnę, musisz ruszyć `db/h2/schema.sql`, `db/mysql/schema.sql`
    **i** `db/postgres/schema.sql`. Do tego trzy pliki `data.sql`, bo mają
    wstawki pozycyjne bez nazw kolumn — dodanie kolumny je psuje.

    Sprawdzone: po dodaniu kolumny aplikacja nie wstaje, ale
    `OwnerControllerTests` przechodzi, bo to `@WebMvcTest` z mockiem
    repozytorium i nigdy nie dotyka bazy. Łapie to dopiero `ClinicServiceTests`
    oznaczony `@DataJpaTest`.

## Formatowanie jest bramką

`spring-javaformat-maven-plugin` przerywa build w fazie `validate`, jeśli
formatowanie się nie zgadza. Sprawdzone: 6 sekund od uruchomienia do błędu.

Komunikat mówi wprost, co zrobić — uruchomić `spring-javaformat:apply`.
To dobry wzorzec bramki: szybka, twarda i z instrukcją naprawy.
