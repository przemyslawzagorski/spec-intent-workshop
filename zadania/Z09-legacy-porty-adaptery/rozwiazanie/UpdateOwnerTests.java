package org.springframework.samples.petclinic.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy reguly aktualizacji wlasciciela.
 *
 * Zwroc uwage, czego tu NIE MA: adnotacji @SpringBootTest, @WebMvcTest, @DataJpaTest.
 * Zadnego kontekstu, zadnej bazy, zadnego mocka frameworka.
 *
 * Przed refaktorem ta sama regula dala sie sprawdzic tylko przez warstwe webowa -
 * OwnerControllerTests startuje kontekst i trwa okolo 5,6 sekundy na 15 testow. Ten plik
 * wykonuje sie w milisekundach.
 *
 * To jest cala oplacalnosc portow i adapterow, wyrazona w jednostce, ktora czujesz
 * codziennie: dlugosci petli zwrotnej.
 */
class UpdateOwnerTests {

	/** Adapter testowy. Piec linii zamiast bazy danych. */
	static class OwnersInMemory implements Owners {

		final List<Owner> zapisani = new ArrayList<>();

		@Override
		public Optional<Owner> findById(Integer id) {
			return this.zapisani.stream().filter(o -> id.equals(o.getId())).findFirst();
		}

		@Override
		public void save(Owner owner) {
			this.zapisani.add(owner);
		}

	}

	private final OwnersInMemory owners = new OwnersInMemory();

	private final UpdateOwner updateOwner = new UpdateOwner(this.owners);

	@Test
	void zapisujeGdyIdentyfikatorySieZgadzaja() {
		Owner owner = new Owner();
		owner.setId(7);

		UpdateOwner.Result wynik = this.updateOwner.handle(owner, 7);

		assertThat(wynik).isInstanceOf(UpdateOwner.Result.Updated.class);
		assertThat(this.owners.zapisani).containsExactly(owner);
	}

	@Test
	void odmawiaGdyIdentyfikatorSieNieZgadza() {
		Owner owner = new Owner();
		owner.setId(7);

		UpdateOwner.Result wynik = this.updateOwner.handle(owner, 9);

		assertThat(wynik).isEqualTo(new UpdateOwner.Result.IdMismatch(7, 9));
		assertThat(this.owners.zapisani).isEmpty();
	}

	@Test
	void odmawiaGdyFormularzNieMaIdentyfikatora() {
		Owner owner = new Owner();

		UpdateOwner.Result wynik = this.updateOwner.handle(owner, 9);

		assertThat(wynik).isEqualTo(new UpdateOwner.Result.IdMismatch(null, 9));
		assertThat(this.owners.zapisani).isEmpty();
	}

}
