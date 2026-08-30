# SPEC · Przypomnienia o wizytach

Wersja z przesłuchania intencji (to samo, co robimy w Z05). Pięć wymagań,
każde da się sprawdzić.

## Kontekst

Klinika chce przypominać właścicielom o zbliżających się wizytach.
Zadanie uruchamia się raz na dobę, rano.

## Wymagania

**W1** · Kiedy do terminu wizyty zostaje **dokładnie 7 dni**, system wysyła
przypomnienie.

**W2** · Kiedy do terminu wizyty zostaje **dokładnie 1 dzień**, system wysyła
przypomnienie.

**W3** · Jeżeli właściciel nie ma zapisanego adresu e-mail, system **nie wysyła
nic** i zapisuje to zdarzenie w logu.

**W4** · Jeżeli wizyta została **odwołana**, system nie wysyła przypomnienia —
niezależnie od tego, ile dni zostało do terminu.

**W5** · Dla jednej wizyty system wysyła **najwyżej jedno przypomnienie na dobę**,
nawet jeśli zadanie uruchomi się kilka razy.

## Poza zakresem

Powiadomienia SMS. Przypomnienia po terminie wizyty. Zmiana terminu z poziomu
przypomnienia.
