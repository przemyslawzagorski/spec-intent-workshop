package org.springframework.samples.petclinic.owner;

import java.util.Objects;

import org.springframework.stereotype.Service;

/**
 * Regula aktualizacji wlasciciela - bez wiedzy o HTTP.
 *
 * Przed refaktorem ta regula siedziala w OwnerController.processUpdateOwnerForm,
 * przemieszana z BindingResult, RedirectAttributes i nazwami widokow. Zeby ja
 * przetestowac, trzeba bylo postawic warstwe webowa.
 *
 * Teraz jest to zwykla klasa, ktora zwraca WYNIK, a nie nazwe widoku. Co z tym wynikiem
 * zrobic - przekierowac, pokazac blad, zwrocic JSON - decyduje ten, kto ja wola.
 */
@Service
public class UpdateOwner {

	/** Co sie stalo. Kontroler tlumaczy to na HTTP, logika o HTTP nie wie. */
	public sealed interface Result {

		/** Zapisano. */
		record Updated(Integer ownerId) implements Result {
		}

		/** Identyfikator w formularzu nie zgadza sie z tym w adresie. */
		record IdMismatch(Integer wFormularzu, Integer wAdresie) implements Result {
		}

	}

	private final Owners owners;

	public UpdateOwner(Owners owners) {
		this.owners = owners;
	}

	public Result handle(Owner owner, int ownerId) {
		if (!Objects.equals(owner.getId(), ownerId)) {
			return new Result.IdMismatch(owner.getId(), ownerId);
		}
		owner.setId(ownerId);
		this.owners.save(owner);
		return new Result.Updated(ownerId);
	}

}
