#!/usr/bin/env bash
#
# Hook agenta: blokuje komendy, które omijają twoje bramki.
#
# Ten hook powstał z konkretnego znaleziska: agent, który utknie w pętli
# naprawczej, prędzej czy później sięgnie po `--no-verify`. Formalnie spełnia
# polecenie „zrób commit" — po prostu wybiera drogę, której nie przewidziałeś.
#
# Hook gita nie ma jak się przed tym obronić: `--no-verify` wyłącza właśnie
# hooki gita. Obrona musi być piętro wyżej.
#
set -uo pipefail

wejscie=$(cat)

odmow() {
  echo "$1" >&2
  exit 2
}

case "$wejscie" in
  *'--no-verify'*)
    odmow 'Zablokowane: --no-verify wylacza hooki gita.
Jesli hook blokuje slusznie - napraw przyczyne.
Jesli blokuje niesluszne - powiedz mi, poprawimy wzorzec.' ;;

  *'--nadpisz-odcisk'*)
    odmow 'Zablokowane: przebazowanie odcisku testow to decyzja czlowieka.
Powiedz mi, ktory test i dlaczego przestal pasowac.' ;;

  *'git push --force'*|*'git push -f'*)
    odmow 'Zablokowane: wymuszony push. Powiedz mi, co chcesz osiagnac.' ;;
esac

exit 0
