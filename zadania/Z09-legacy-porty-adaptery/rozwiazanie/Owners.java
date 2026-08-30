package org.springframework.samples.petclinic.owner;

/**
 * Port: czego logika potrzebuje od skladowania wlascicieli.
 *
 * To jest interfejs zdefiniowany przez STRONE, KTORA GO UZYWA, a nie przez bibliotece,
 * ktora go implementuje. Dlatego ma JEDNA metode, a nie czterdziesci, ktore dziedziczy
 * JpaRepository.
 *
 * Jedna, bo tyle wola UpdateOwner. Przy pisaniu tego portu mielismy tu tez findById -
 * przy slowie "aktualizacja" wydaje sie oczywiste, ze trzeba najpierw pobrac. Nie trzeba:
 * obiekt przychodzi z formularza, a ten przypadek uzycia go tylko zapisuje. Metoda nie
 * miala ani jednego wolajacego i wylecila. Port ma tyle metod, ile logika UZYWA, nie
 * tyle, ile brzmi sensownie.
 *
 * Dzieki temu UpdateOwner da sie przetestowac bez Springa, bez bazy i bez kontekstu
 * aplikacji - patrz UpdateOwnerTests.
 */
public interface Owners {

	void save(Owner owner);

}
