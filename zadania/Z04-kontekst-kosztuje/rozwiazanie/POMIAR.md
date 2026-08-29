# Z04 — moje liczby i wzorcowa odpowiedź

## Ile co waży w petclinicu

Zmierzone na przypiętym commicie, przybliżenie 4 znaki na token:

| Co | Plików | Bajtów | ~tokenów |
|---|---:|---:|---:|
| całe repo (bez `.git` i `target`) | 134 | 1 429 724 | **357 400** |
| cały `src/main/java` | 30 | 59 081 | **14 800** |
| cztery celowane pliki | 4 | 14 321 | **3 600** |

Co składa się na całe repo:

| typ | udział |
|---|---:|
| `.svg` (fonty) | 33,0% |
| `.css` | 20,2% |
| `.java` | **9,7%** |
| `.ttf`, `.png`, `.woff`, `.eot` | 20,4% |

Największy pojedynczy plik: `varela_round-webfont.svg`, 378 KB — około
**95 000 tokenów ścieżek wektorowych**. Jeśli „wrzucasz całe repo", to jest
to, za co płacisz.

## Wzorcowa odpowiedź: co naprawdę trzeba zmienić

Sprawdzone w kodzie. Dodanie pola `insurer` do właściciela dotyka **11 plików**:

**Java (1)**
- `owner/Owner.java` — pole, getter, setter

**Schematy baz (3)** — i to jest pierwsza pułapka
- `db/h2/schema.sql`
- `db/mysql/schema.sql`
- `db/postgres/schema.sql`

Trzy osobne pliki, trzy różne konwencje typów (`VARCHAR`, `VARCHAR`, `TEXT`).
**Jeśli dałeś agentowi tylko schemat H2, nie ma szansy wiedzieć o dwóch
pozostałych.**

**Dane startowe (3)** — druga pułapka
- `db/h2/data.sql`, `db/mysql/data.sql`, `db/postgres/data.sql`

Wszystkie trzy mają wstawki pozycyjne: `INSERT INTO owners VALUES (default,
'George', 'Franklin', ...)` — bez nazw kolumn. Dodanie kolumny **wywala je
wszystkie**.

**Szablony (3)**
- `templates/owners/createOrUpdateOwnerForm.html` — pole formularza
- `templates/owners/ownerDetails.html` — wyświetlenie
- `templates/owners/ownersList.html` — kolumna na liście

**Testy (1)**
- `owner/OwnerControllerTests.java` — parametry POST-a w testach formularza

## Odpowiedzi na dwa pytania kontrolne

**1 · Czy coś się zepsuje, jeśli zmienię tylko Javę i schemat?**

Tak. Aplikacja **nie wstanie**. Sprawdziłem to: dodałem kolumnę `insurer`
do `db/h2/schema.sql` i uruchomiłem testy.

```
Failed to execute SQL script statement #21 of file [.../db/h2/data.sql]:
INSERT INTO owners VALUES (default, 'George', 'Franklin', ...)
Caused by: org.h2.jdbc.JdbcSQLSyntaxErrorException: Column count does not match
```

**Ale — i to jest ciekawsze — `OwnerControllerTests` przeszło. Piętnaście testów,
zero błędów.** Bo to jest `@WebMvcTest`, który mockuje repozytorium i nigdy nie
dotyka bazy. Awarię złapał dopiero `ClinicServiceTests`, oznaczony `@DataJpaTest`.

Zapamiętaj to: **zielony zestaw testów nie znaczy, że aplikacja wstanie.**
Znaczy tylko, że przeszły te testy, które napisano. Wrócimy do tego w Z07.

**2 · Czy trzeba dopisać pole do listy dozwolonych pól formularza?**

**Nie.** `OwnerController.setAllowedFields` — mimo nazwy — woła
`dataBinder.setDisallowedFields("id", "*.id")`. To lista **zakazanych**, nie
dozwolonych. Nowe pola są dopuszczone domyślnie.

To dobre pytanie kontrolne, bo nazwa metody sugeruje coś przeciwnego niż jej
treść. Agent, który odpowie „tak, dopisz `insurer` do dozwolonych", zgadywał
po nazwie zamiast przeczytać dwie linijki niżej.

## Co z tego wynika dla porównania A i B

Podejście B (cztery pliki, 3 600 tokenów) **wypadnie gorzej** i to jest
zamierzone. Bez `db/mysql/` i `db/postgres/` agent nie wymieni dwóch schematów
i dwóch plików z danymi.

To jest uczciwy wynik i lepszy dydaktycznie niż potwierdzenie tezy „mniej
kontekstu zawsze wystarczy". Prawda jest taka:

- **Ograniczanie kontekstu ma koszt.** Ten koszt to informacja, której agent
  nie zobaczy.
- **Sztuką jest ograniczyć trafnie, nie mocno.** Gdyby do zestawu B dodać
  `db/**/*.sql` — sześć małych plików, ~1 500 tokenów — odpowiedź byłaby pełna
  przy nadal czterokrotnie mniejszym koszcie niż A.
- **Dlatego robi się to w dwóch fazach.** Pierwszy przebieg szeroki, żeby
  ustalić, gdzie leży sprawa. Drugi wąski, na znalezionym zbiorze.
