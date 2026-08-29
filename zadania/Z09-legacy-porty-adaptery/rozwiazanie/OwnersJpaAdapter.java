package org.springframework.samples.petclinic.owner;

import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * Adapter: laczy port Owners ze Spring Data.
 *
 * Cala wiedza o tym, ze pod spodem jest JPA, konczy sie w tej klasie. Podmiana
 * skladowania na cokolwiek innego to napisanie drugiego adaptera - logika sie nie dowie.
 */
@Component
class OwnersJpaAdapter implements Owners {

	private final OwnerRepository repository;

	OwnersJpaAdapter(OwnerRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Owner> findById(Integer id) {
		return this.repository.findById(id);
	}

	@Override
	public void save(Owner owner) {
		this.repository.save(owner);
	}

}
