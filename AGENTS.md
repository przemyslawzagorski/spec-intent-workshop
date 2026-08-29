# AGENTS.md

Reguły tego repo. Czytają je Claude Code, Copilot, Augment i inne agenty.
Siedzą w **każdym** promptcie, więc są krótkie.

To jest repo materiałów warsztatowych, nie aplikacja.

## Wzoruj się na plikach, nie na moim opisie

- Opis zadania: `zadania/Z07-harness/README.md`
- Prompt dla uczestnika: `zadania/Z05-od-pomyslu-do-specyfikacji/prompty/grill.md`
- Bramka: `harness-template/bramka`
- Reguła jako skrypt: `tools/repo_policy.py`

Piszesz nowe zadanie? **Odtwórz kształt Z07**, nie wymyślaj własnego układu.

## Twarde reguły

1. **Każda liczba w materiale ma być zmierzona.** Czasy buildów, rozmiary
   obrazów, liczby testów. Jeśli nie uruchomiłeś — nie pisz.
2. **Każde rozwiązanie ma działać.** Nie „powinno zadziałać". Uruchom.
3. **Nie zmieniaj `harness-template/` przy okazji.** To jest prezent dla
   uczestników i punkt startu Z11. Zmiany tam idą osobno i przechodzą `./bramka`.
4. **Nie edytuj `praca/`.** To piaskownice, gitignorowane. Rozwiązania trzymamy
   w `zadania/*/rozwiazanie/`.
5. **Skrypty bez rozszerzenia dopisz do `.gitattributes`** z `eol=lf`.
   Bez tego dostaną CRLF i nie odpalą się na Linuksie. `./sprawdz env` pilnuje.

## Język

Polski, normalny, bez hype'u. Komentarze po polsku, identyfikatory po angielsku.

Nie pisz „w dzisiejszym świecie", „kluczowe znaczenie", „warto podkreślić".
Konkret, przykład, liczba. Jeśli coś ma wadę — napisz jaką.

Każde zadanie ma sekcję **„Kiedy to NIE ma sensu"**. Nie usuwaj jej.

## Zanim powiesz „gotowe”

```bash
./sprawdz env          # środowisko
./sprawdz <ZADANIE>    # zadanie, które ruszałeś
```

W `harness-template/` dodatkowo `./bramka`.

## Czego nie zakładać

- Petclinic celuje w **Javę 17** — bez wzorców w `switch`. Reszta repo używa 25.
- `mvn` z systemu jest zepsuty. Używaj `wsmvn` z `.tooling/env.sh`.
- Firmowy Nexus przechwytuje Central. Stąd `docs/setup/settings-central.xml`.
