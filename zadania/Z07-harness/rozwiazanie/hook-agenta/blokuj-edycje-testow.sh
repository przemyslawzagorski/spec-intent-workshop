#!/usr/bin/env bash
#
# Hook agenta: nie pozwala agentowi tknąć plików testowych.
#
# Różnica względem hooka gita z Z01 jest zasadnicza:
#
#   hook gita      łapie przy `git commit` — plik jest już zmieniony na dysku
#   hook agenta    łapie przy wywołaniu narzędzia — zmiana się NIE WYDARZY
#
# Ten hook dostaje na wejściu JSON opisujący zamierzone wywołanie narzędzia.
# Kod wyjścia 2 blokuje wywołanie, a to, co wypiszesz na stderr, wraca
# do modelu jako wyjaśnienie — więc pisz komunikaty, które coś tłumaczą.
#
# Podpięcie: patrz settings.json obok.
#
set -uo pipefail

wejscie=$(cat)

# Bez jq, bo nie każdy go ma. To jest dopasowanie tekstowe na surowym JSON-ie
# i wystarcza do tego przykładu — ale nie udawaj przed sobą, że to parser.
# W prawdziwym repo użyj jq:  plik=$(jq -r '.tool_input.file_path // empty')
#
# Backslashe zamieniamy na ukośniki, bo w JSON-ie ścieżka windowsowa
# przychodzi podwojona i inaczej wzorzec by jej nie złapał.
sciezki=${wejscie//\\//}

case "$sciezki" in
  */src/test/*)
    cat >&2 <<'KONIEC'
Zablokowane: pliki testowe sa poza twoim zasiegiem w tym repo.

Czerwony test to informacja o kodzie produkcyjnym, nie usterka testu.
Popraw kod. Jesli uwazasz, ze test jest zly - napisz mi, dlaczego,
i poczekaj na moja decyzje. Nie zmieniaj go sam.
KONIEC
    exit 2
    ;;
esac

exit 0
