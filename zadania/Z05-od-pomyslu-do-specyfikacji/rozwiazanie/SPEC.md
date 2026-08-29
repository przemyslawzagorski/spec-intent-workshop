# Specyfikacja: umawianie wizyty do weterynarza

Wynik przesłuchania intencji z `INTENCJA.md`. Twoja będzie inna — to nie jest
klucz odpowiedzi, tylko punkt odniesienia.

## Zdolność

Właściciel może umówić wizytę swojego zwierzęcia do wskazanego weterynarza
na konkretny termin. Dziś wizyta jest wyłącznie wpisem w historii zwierzęcia
i nie ma żadnego powiązania z weterynarzem.

## Wymagania

### Umawianie

- **R1.1** WHEN an owner books a visit for their pet with a chosen vet and a chosen slot, the system shall store the visit with the vet reference and the slot start time.
- **R1.2** IF the chosen slot start time is not aligned to a full or half hour, THEN the system shall reject the booking.
- **R1.3** IF the chosen slot start time is earlier than the current time, THEN the system shall reject the booking.
- **R1.4** IF the chosen vet already has a visit whose slot starts at the same time, THEN the system shall reject the booking and shall not store it.
- **R1.5** WHERE the pet does not belong to the requesting owner, the system shall reject the booking.

### Widok weterynarza

- **R1.6** WHEN a vet's day is requested for a given date, the system shall return that vet's visits for that date, ordered by slot start time ascending.
- **R1.7** WHERE a vet has no visits on the requested date, the system shall return an empty list, not an error.

### Wizyty istniejące

- **R1.8** WHERE a visit has no vet assigned, the system shall keep it readable and shall keep showing it in the pet's history.
- **R1.9** IF a visit has no vet assigned, THEN the system shall not include it in any vet's day view.

## Założenia

Rzeczy, których nikt mi nie powiedział, a musiałem rozstrzygnąć.
**Jeśli któreś z nich jest fałszywe, wymagania wyżej trzeba przepisać.**

| Założenie | Co, jeśli fałszywe |
|---|---|
| Slot trwa **30 minut** i zaczyna się o pełnej lub połowie godziny | R1.2 do przepisania; przy dowolnych godzinach konflikt trzeba liczyć jako nakładanie przedziałów, nie równość |
| Konflikt to **równość momentu startu**, nie nakładanie się | przy zmiennej długości wizyty R1.4 przestaje wystarczać |
| Wszystkie godziny są w **jednej strefie czasowej** lecznicy | przy wielu placówkach trzeba dodać strefę do slotu |
| Weterynarz **nie ma grafiku** — można umówić na dowolną wolną godzinę | trzeba dołożyć pojęcie dostępności, którego w kodzie nie ma w ogóle |
| Wizyta **nie ma statusu** — istnieje albo nie | odwołanie wizyty wymaga statusu, dziś byłoby usunięciem rekordu |
| Umawia **właściciel zwierzęcia**, nie recepcja | R1.5 do wyrzucenia albo do rozbudowy o role |

## Poza zakresem

- **Odwoływanie i przekładanie wizyt.** Wymaga statusu wizyty, którego w modelu
  nie ma. Osobna zdolność.
- **Grafik i urlopy weterynarza.** W repo nie istnieje żadne pojęcie
  dostępności. Dołożenie go to większa zmiana niż cała ta funkcja.
- **Powiadomienia.** Brak jakiejkolwiek infrastruktury do wysyłki.
- **Konflikty po stronie zwierzęcia** (dwie wizyty tego samego zwierzaka o tej
  samej porze u różnych lekarzy). Rzadkie i nikt o to nie prosił.

## Zmiany w istniejącym kodzie

| Plik | Co się zmienia |
|---|---|
| `owner/Visit.java` | dodanie referencji do weterynarza i godziny startu |
| `db/h2/schema.sql`, `db/mysql/schema.sql`, `db/postgres/schema.sql` | kolumny `vet_id` i `slot_start` w tabeli `visits`, klucz obcy do `vets` |
| `db/h2/data.sql`, `db/mysql/data.sql`, `db/postgres/data.sql` | wstawki do `visits` są pozycyjne — dodanie kolumn je psuje |
| `owner/VisitController.java` | wybór weterynarza i terminu w formularzu |
| `templates/pets/createOrUpdateVisitForm.html` | pola wyboru |
| `vet/VetController.java` | nowy widok „mój dzień" |
| nowy szablon w `templates/vets/` | lista wizyt weterynarza na dany dzień |
| `owner/VisitRepository` lub zapytanie w `OwnerRepository` | wyszukanie kolizji terminu |

## Czego ta specyfikacja świadomie nie rozstrzyga

Nie mówi, czy `Visit` ma trzymać `Vet`, czy odwrotnie, ani czy kolizję sprawdzać
zapytaniem, czy ograniczeniem unikalności w bazie. To są decyzje projektowe
i należą do implementacji, nie do specyfikacji.
