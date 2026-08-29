# B3 · MCP: agent sięga po schemat zamiast zgadywać

**40 min** · petclinic · bonus

## O co chodzi

Agent, który nie widzi bazy, wnioskuje jej strukturę z nazw klas. Zwykle
trafia. Przy pierwszej kolumnie, której nie ma w kodzie, przestaje trafiać
— i nie ma jak się o tym dowiedzieć.

MCP (Model Context Protocol) to sposób, żeby dać agentowi **narzędzie**,
którym sam sprawdzi, zamiast zgadywać.

## Jak zwykle to robimy

Wklejamy schemat do promptu. Albo nie wklejamy i liczymy, że agent wywnioskuje
go z encji JPA.

Boli, bo:

- **Schemat w promptcie się starzeje.** Wkleiłeś raz, ktoś dodał kolumnę.
- **Wnioskowanie z encji bywa fałszywe.** Widziałeś to w Z03: `Visit` wygląda
  na powiązaną z `Vet`, bo w prawdziwej lecznicy jest. W schemacie nie ma.
- **Płacisz za cały schemat przy każdym zapytaniu**, choć potrzebujesz
  dwóch tabel.

## Jakie są opcje

**Wklej schemat do promptu.** Działa dziś, jest nieaktualne jutro.
Przy trzech tabelach w porządku.

**Wygeneruj zrzut i trzymaj w repo.** Wersjonowany, w code review, tani.
Nie wymaga żadnej infrastruktury. **Przy większości projektów to wystarcza
i jest właściwym wyborem.**

**MCP do bazy.** Agent dostaje narzędzie i pyta bazę sam, gdy potrzebuje.
Zawsze aktualne, płacisz tylko za to, o co zapytał. Wada: kolejny proces
do skonfigurowania, kwestia uprawnień i dostępu do prawdziwych danych.

## Jak zrobić dobrze

**Zacznij od pliku w repo.** Serio. `pg_dump --schema-only` do
`docs/schema.sql`, odświeżane w CI. Rozwiązuje 80% problemu za 20 minut pracy.

**Sięgaj po MCP, gdy plik przestaje wystarczać** — bo schemat zmienia się
codziennie, bo jest ich pięć w różnych środowiskach, albo bo potrzebujesz
nie schematu, a danych.

**Podłączaj do repliki lub bazy deweloperskiej, nie do produkcji.** Agent
z dostępem do produkcyjnej bazy to agent, który może wykonać na niej zapytanie.

**Dawaj tylko odczyt.** To ta sama zasada, co ograniczona lista narzędzi z Z12.

## Zrób to

**1 · Zobacz problem** (10 min). Nowa sesja, czysty kontekst, petclinic
otwarty **bez** katalogu `src/main/resources/db`:

> Napisz zapytanie SQL, które zwróci właścicieli wraz z liczbą ich zwierząt
> i datą ostatniej wizyty któregokolwiek z nich.

Sprawdź wynik względem `src/main/resources/db/h2/schema.sql`. Szczególnie:
jak nazwał kolumnę z datą wizyty i czy tabela `visits` ma to, czego użył.

**2 · Wariant z plikiem** (10 min). To samo pytanie, ale dołóż `schema.sql`
do kontekstu. Porównaj.

**3 · Wariant z MCP** (20 min). Podłącz serwer MCP do bazy i zadaj to samo
pytanie, pozwalając agentowi sprawdzić schemat samodzielnie.

Najprościej postawić Postgresa z `docker-compose.yml` petclinica i uruchomić
aplikację z profilem `postgres`, żeby schemat i dane się załadowały.

Konfiguracja MCP zależy od narzędzia — w Claude Code to wpis w konfiguracji
serwerów MCP, w innych narzędziach wygląda inaczej. **To jest część ćwiczenia:**
warsztat jest agnostyczny, więc sprawdź w dokumentacji swojego narzędzia,
jak dodaje się serwer MCP i jak ograniczyć go do odczytu.

Jeśli nie uda się podłączyć w rozsądnym czasie — **zatrzymaj się na kroku 2**.
Wniosek z porównania pierwszych dwóch wariantów jest i tak najważniejszy.

## Do omówienia

- **Ile z tego faktycznie potrzebowaliśmy.** Przy petclinicu plik z pięcioma
  tabelami wystarcza w zupełności. MCP zaczyna mieć sens przy schemacie,
  którego nikt nie ogarnia w całości.
- **Co MCP daje naprawdę, a co jest hype'em.** Daje: aktualność, płacenie za
  to, o co pytasz, dostęp do danych, nie tylko struktury. Nie daje: rozumienia
  domeny. Agent, który widzi kolumnę `status`, nadal nie wie, co znaczy jej
  wartość `3`.
- **Że to jest ta sama decyzja co w Z04.** Cały kontekst z góry kontra sięganie
  po to, czego akurat trzeba. MCP to po prostu drugi sposób na to samo.
- **O uprawnieniach.** Agent z narzędziem do bazy ma tyle władzy, ile mu dałeś
  na poziomie połączenia. Nie na poziomie promptu.
