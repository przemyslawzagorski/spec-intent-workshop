# ADR-001: Postgres przez JDBC, bez ORM

**Status:** przyjęty · **Data:** 2026-08-27

## Kontekst

`returns-service` potrzebuje trzech tabel: zamówienia, pozycje zamówień, zwroty.
Model jest płaski, bez dziedziczenia, bez leniwych relacji, bez zapytań generowanych
dynamicznie. Standardem w Quarkusie byłoby Hibernate ORM z Panache.

## Decyzja

Używamy `quarkus-jdbc-postgresql` + `quarkus-agroal` z jawnym SQL-em w klasach
`control`. Bez Hibernate, bez Panache. Schemat tworzy `Schema.java` przy starcie.

## Uzasadnienie

**Główny powód jest dydaktyczny i dotyczy pracy z agentem.** Agent czytający
`Orders.java` widzi kontrakt bazy danych wprost — zapytanie stoi obok metody.
Przy ORM musiałby zrekonstruować ten kontrakt z adnotacji rozsianych po encjach,
konfiguracji dialektu i konwencji nazewniczych. Jawny SQL to mniej kontekstu do
wczytania i mniej miejsc, w których agent może zgadnąć źle.

Powód drugi: warsztat uczy SDD/IDD, nie mapowania obiektowo-relacyjnego. Każda
minuta spędzona na `LazyInitializationException` jest minutą straconą.

## Konsekwencje

- Schemat trzeba utrzymywać ręcznie. Przy trzech tabelach to nie jest koszt.
- Na produkcji `Schema.java` zastąpiłaby migracja (Flyway/Liquibase). Zapisane
  w kodzie jako świadome uproszczenie warsztatowe.
- Zapytania nie są sprawdzane przy kompilacji. Łapie je bramka HARD.

## Odrzucone alternatywy

**Hibernate ORM + Panache** — mniej kodu w repozytoriach, ale więcej ukrytego
zachowania. Dla agenta „mniej kodu" nie znaczy „mniej kontekstu".
