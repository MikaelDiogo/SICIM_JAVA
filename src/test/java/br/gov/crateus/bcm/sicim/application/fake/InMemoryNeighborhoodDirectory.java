package br.gov.crateus.bcm.sicim.application.fake;

import br.gov.crateus.bcm.sicim.application.port.NeighborhoodDirectory;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Permissivo por padrão; {@link #forget(UUID)} simula um bairro inexistente. */
public class InMemoryNeighborhoodDirectory implements NeighborhoodDirectory {

	private final Set<UUID> unknown = new HashSet<>();

	@Override
	public boolean exists(UUID neighborhoodId) {
		return !unknown.contains(neighborhoodId);
	}

	public void forget(UUID neighborhoodId) {
		unknown.add(neighborhoodId);
	}
}
