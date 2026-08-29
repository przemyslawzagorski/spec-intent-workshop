# Decyzje widoczne w kodzie

Rzeczy, które ktoś kiedyś rozstrzygnął i nigdzie nie zapisał. Wyczytane z kodu.

## Kontrolery sięgają wprost do repozytoriów

`owner/OwnerController.java` trzyma `OwnerRepository` i woła go bezpośrednio.
Nie ma warstwy serwisów — w całym projekcie nie istnieje klasa `*Service`.

**Co z tego wynika:** reguła biznesowa z `processUpdateOwnerForm` — sprawdzenie,
czy identyfikator w formularzu zgadza się z tym w adresie — siedzi w kontrolerze,
razem z `BindingResult` i nazwami widoków. Nie da się jej przetestować bez
warstwy webowej.

To nie jest niedopatrzenie. Tak wygląda większość aplikacji Springowych.

## Weterynarze są tylko do odczytu

`vet/VetRepository.java` rozszerza `Repository`, nie `JpaRepository`, i ma
dokładnie dwie metody — obie `findAll`, obie oznaczone
`@Transactional(readOnly = true)` i `@Cacheable("vets")`. Nigdzie w kodzie
nie ma zapisu weterynarza.

**Co z tego wynika:** żeby dodać weterynarza, trzeba zmienić `data.sql`.

## Nowa wizyta ma datę jutrzejszą

Konstruktor bezargumentowy `owner/Visit.java` ustawia datę na jutro:

```java
public Visit() {
    this.date = LocalDate.now().plusDays(1);
}
```

**Co z tego wynika:** formularz nowej wizyty otwiera się z jutrzejszą datą.
Wygląda jak wpis historyczny, a domyślnie jest przyszły.

## Dwie kolekcje, dwa porządki

`Owner.pets` to `List` sortowana po nazwie. `Pet.visits` to `LinkedHashSet`
sortowany po dacie rosnąco. Obie ładowane `EAGER`, obie zadeklarowane `final`.

**Co z tego wynika:** pobranie właściciela ciągnie wszystkie jego zwierzęta
i wszystkie ich wizyty.

## Lista pól formularza jest listą zakazanych

`OwnerController.setAllowedFields` — mimo nazwy — woła
`dataBinder.setDisallowedFields("id", "*.id")`.

**Co z tego wynika:** nowe pola encji są dopuszczone domyślnie. Nie trzeba ich
nigdzie dopisywać. Nazwa metody sugeruje dokładnie coś przeciwnego niż jej treść
— to najczęstsze miejsce, w którym agent zgaduje po nazwie zamiast przeczytać
dwie linijki niżej.
