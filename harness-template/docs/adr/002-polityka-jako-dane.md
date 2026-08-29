# ADR-002: Polityka zwrotów jako dane, nie jako konfiguracja aplikacji

**Status:** przyjęty · **Data:** 2026-08-27

## Kontekst

Progi, okna czasowe i wyjątki różnią się między uczestnikami warsztatu (karty
domenowe A–F). Trzeba je skądś wczytać. Quarkus oferuje MicroProfile Config —
naturalne byłoby wsadzić je do `application.properties` albo `application.yaml`.

## Decyzja

Polityka mieszka w osobnym pliku `return-policy.yaml`, wczytywanym jawnie przez
`ReturnPolicy` z użyciem SnakeYAML. **Nie** jest konfiguracją aplikacji.

## Uzasadnienie

Konfiguracja aplikacji to porty, hasła, adresy — rzeczy zależne od środowiska.
Polityka zwrotów jest **częścią specyfikacji produktu**: zmiana progu to zmiana
zachowania biznesowego, a nie przestawienie pokrętła na wdrożeniu.

Rozdzielenie ma konsekwencję, na której stoi cały warsztat: skoro polityka jest
danymi w znanym schemacie, to `tools/policy_cases.py` potrafi **wygenerować
z niej tabelę oczekiwanych decyzji**. Dzięki temu każdy uczestnik buduje
inny system i wszystkie przechodzą tę samą bramkę.

To jest ta sama obserwacja, którą zrobił zespół Bun przy przepisywaniu z Ziga na
Rusta: mapowanie typów trzymali w `LIFETIMES.tsv`, nie w prozie. Tabela jest
tańsza w tokenach i sprawdzalna maszynowo.

## Konsekwencje

- Polityka jest walidowana schematem (`docs/contract/return-policy.schema.json`)
  przez `tools/policy_check.py`, zanim dotknie jej kod.
- Wczytujemy raz, przy starcie. Zmiana wymaga restartu — świadomie, bo to zmiana
  kontraktu, a nie parametru operacyjnego.
- Dochodzi zależność `org.yaml:snakeyaml`.

## Odrzucone alternatywy

**MicroProfile Config / `application.yaml`** — zlewa politykę biznesową
z konfiguracją środowiskową i uniemożliwia walidację schematem przed startem.

**Zahardkodowanie w Javie** — nie da się wtedy wygenerować tabeli testowej,
a cały mechanizm „jedna bramka, wiele polityk" przestaje działać.
