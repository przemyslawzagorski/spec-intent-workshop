# CONTEXT.md — słownik domeny petclinica

Wspólny język projektu. Kod, testy i rozmowy z agentem używają **tych samych
słów**. Jeśli musisz użyć innego — najpierw dopisz je tutaj.

## Pojęcia

**Właściciel** (`Owner`, `owner/Owner.java`) — osoba, która przyprowadza
zwierzęta. Ma imię, nazwisko, adres, miasto i telefon (dokładnie 10 cyfr,
walidacja regexem). Powstaje przez formularz `/owners/new`. Trzyma listę swoich
zwierząt.

**Zwierzę** (`Pet`, `owner/Pet.java`) — należy do dokładnie jednego właściciela.
Ma imię, datę urodzenia i rodzaj. Nie istnieje samodzielnie: kluczem obcym wisi
przy właścicielu (`owner_id`).

**Rodzaj zwierzęcia** (`PetType`, `owner/PetType.java`) — słownik: pies, kot,
ptak. Tabela `types`. Klasa nie ma żadnych własnych pól poza nazwą.

**Wizyta** (`Visit`, `owner/Visit.java`) — wpis w historii zwierzęcia: data
i opis. Należy do zwierzęcia (`pet_id`). Nowa wizyta domyślnie dostaje datę
**jutrzejszą**, nie dzisiejszą.

**Weterynarz** (`Vet`, `vet/Vet.java`) — imię, nazwisko i zbiór specjalizacji.
Dane są tylko do odczytu; w aplikacji nie ma sposobu, żeby dodać weterynarza.

**Specjalizacja** (`Specialty`, `vet/Specialty.java`) — słownik, powiązany
z weterynarzami relacją wiele-do-wielu przez `vet_specialties`.

## Czego tu nie ma

**Wizyta nie ma weterynarza.** To najważniejsza rzecz w tym dokumencie.
Tabela `visits` ma dokładnie trzy kolumny: `pet_id`, `visit_date`, `description`
(`src/main/resources/db/h2/schema.sql`). W klasie `Visit` nie ma pola typu `Vet`.
W pakiecie `owner` nie pada słowo `Vet` ani razu, w pakiecie `vet` nie pada
`Visit` ani `Owner`.

**Aplikacja składa się z dwóch rozłącznych połówek**: właściciele → zwierzęta →
wizyty, oraz weterynarze → specjalizacje. Nie łączy ich nic poza wspólnym menu.

**Nie ma terminarza ani rezerwacji.** Wizyta to wpis w historii, nie umówienie
się na godzinę. Nie ma pola z godziną — tylko data.

**Nie ma pacjenta jako osobnego pojęcia.** Pacjentem jest zwierzę.

**Nie ma płatności, faktur, leków ani kartoteki medycznej.** Opis wizyty to
zwykły `VARCHAR(255)`.

## Niespodzianki

| Co | Gdzie | Dlaczego zaskakuje |
|---|---|---|
| Nowa wizyta ma datę **jutrzejszą** | `owner/Visit.java`, konstruktor bezargumentowy | Wygląda jak wpis historyczny, a domyślnie jest przyszły |
| Zwierzęta właściciela to `List` sortowana po nazwie, wizyty zwierzęcia to `Set` sortowany po dacie | `Owner.java`, `Pet.java` | Dwie różne kolekcje i dwa różne porządki w sąsiednich klasach |
| `Owner.pets` jest `final` i ładowane `EAGER` | `owner/Owner.java` | Każde pobranie właściciela ciągnie wszystkie zwierzęta i wszystkie ich wizyty |
| Telefon to `String` z regexem `\d{10}` | `owner/Owner.java` | Bez kierunkowego, bez formatowania — dokładnie dziesięć cyfr albo błąd walidacji |
| Kontrolery wołają repozytoria Spring Data wprost | `owner/OwnerController.java` | Nie ma warstwy serwisów. To nie jest niedopatrzenie, tylko styl tego projektu |
