# Podejście B — wskaźnik na wzorzec

Wklej to agentowi w katalogu `praca/Z02/returns-service`, na **czystym** projekcie
(wygeneruj go ponownie z archetypu albo cofnij zmiany z podejścia A).

Zmierz to samo: **iteracje** i **ręczne poprawki**.

---

Dodaj do tego projektu nowy komponent `shipments` — obsługę wysyłek.

Przeczytaj najpierw komponent `returns`:

- `src/main/java/warsztat/rma/returns/boundary/ReturnsResource.java`
- `src/main/java/warsztat/rma/returns/control/Returns.java`
- `src/main/java/warsztat/rma/returns/entity/ReturnRequest.java`
- `src/main/java/warsztat/rma/returns/package-info.java`

**Odtwórz dokładnie ten sam kształt** dla wysyłek: ten sam podział pakietów, to
samo nazewnictwo, ten sam sposób dostępu do bazy, ten sam styl komentarzy, taki
sam `package-info.java`. Jeśli coś w `returns` wygląda na świadomą decyzję,
powtórz ją, nawet jeśli zrobiłbyś inaczej.

Wysyłka ma: identyfikator, numer zamówienia, przewoźnika, numer listu
przewozowego, status (`PREPARING`, `IN_TRANSIT`, `DELIVERED`, `LOST`) i moment
nadania. Potrzebuję endpointu do utworzenia wysyłki i do pobrania jej po
identyfikatorze.

Zanim zaczniesz pisać, wypisz mi w punktach, jakie konwencje wyczytałeś z
`returns`. Chcę zobaczyć, czy przeczytałeś to samo co ja.

Na koniec uruchom `mvn test-compile` i napraw błędy, jeśli będą.
