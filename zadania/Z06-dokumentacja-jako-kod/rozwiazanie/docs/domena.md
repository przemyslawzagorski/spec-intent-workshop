# Domena

Wszystkie klasy w `src/main/java/org/springframework/samples/petclinic/`.

## Pojęcia

**Właściciel** (`owner/Owner.java`) — imię, nazwisko, adres, miasto, telefon.
Telefon musi mieć **dokładnie dziesięć cyfr** — walidacja regexem w tej klasie.
Trzyma listę swoich zwierząt.

**Zwierzę** (`owner/Pet.java`) — imię, data urodzenia, rodzaj. Należy do jednego
właściciela, przez klucz obcy `owner_id` (`db/h2/schema.sql`).

**Rodzaj zwierzęcia** (`owner/PetType.java`) — słownik w tabeli `types`.
Klasa nie ma własnych pól poza odziedziczoną nazwą.

**Wizyta** (`owner/Visit.java`) — data i opis. Należy do zwierzęcia (`pet_id`).

**Weterynarz** (`vet/Vet.java`) — imię, nazwisko, zbiór specjalizacji.

**Specjalizacja** (`vet/Specialty.java`) — słownik, powiązany z weterynarzami
przez tabelę `vet_specialties` (`db/h2/schema.sql`).

## Czego w tym modelu NIE ma

!!! warning "Wizyta nie ma weterynarza"
    Tabela `visits` ma dokładnie trzy kolumny poza kluczem głównym: `pet_id`,
    `visit_date`, `description` (`db/h2/schema.sql`). W `owner/Visit.java`
    nie ma pola typu `Vet`.

    W całym pakiecie `owner` słowo `Vet` nie pada ani razu. W pakiecie `vet`
    nie pada `Visit` ani `Owner`.

**Aplikacja to dwie rozłączne połówki**: właściciele → zwierzęta → wizyty
oraz weterynarze → specjalizacje. Łączy je tylko wspólne menu.

Nie ma też: terminarza, godzin wizyt (jest sama data), pojęcia pacjenta
odrębnego od zwierzęcia, płatności ani kartoteki medycznej.
