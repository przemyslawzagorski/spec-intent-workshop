# Intencja — punkt startu dla wszystkich

To jest **celowo niedopracowany** opis funkcji. Dokładnie taki, jaki dostajesz
na co dzień: dwa zdania od kogoś, kto wie, o co mu chodzi, i zakłada, że ty
też wiesz.

Nie poprawiaj go. Twoim zadaniem jest wydobyć z niego specyfikację.

---

## Umawianie wizyty do weterynarza

> Chcemy, żeby właściciel mógł umówić wizytę swojego zwierzaka do konkretnego
> weterynarza, a nie tylko wpisać ją do historii. Weterynarz powinien widzieć
> swoje wizyty na dany dzień. Nie chcemy, żeby dało się umówić dwie wizyty
> na tę samą godzinę do tego samego lekarza.

---

Tyle. Zanim zaczniesz cokolwiek pisać, zwróć uwagę, że w petclinicu:

- `Visit` **nie ma** żadnego powiązania z `Vet` — ani w Javie, ani w schemacie,
- `Visit` ma **datę, nie godzinę** (`visit_date DATE`),
- `Vet` jest **tylko do odczytu** — nie ma sposobu, żeby go dodać ani zmienić,
- nowa wizyta domyślnie dostaje datę **jutrzejszą**.

Cztery zdania intencji, cztery zderzenia z rzeczywistością kodu. To jest
normalne i o tym jest to zadanie.
