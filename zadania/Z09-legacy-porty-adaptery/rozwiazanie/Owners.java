package org.springframework.samples.petclinic.owner;

import java.util.Optional;

/**
 * Port: czego logika potrzebuje od skladowania wlascicieli.
 *
 * To jest interfejs zdefiniowany przez STRONE, KTORA GO UZYWA, a nie przez bibliotece,
 * ktora go implementuje. Dlatego ma dwie metody, a nie czterdziesci, ktore dziedziczy
 * JpaRepository.
 *
 * Dzieki temu UpdateOwner da sie przetestowac bez Springa, bez bazy i bez kontekstu
 * aplikacji - patrz UpdateOwnerTests.
 */
public interface Owners {

	Optional<Owner> findById(Integer id);

	void save(Owner owner);

}
