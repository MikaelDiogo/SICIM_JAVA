package br.gov.crateus.bcm.sicim.application.fake;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitDirectory;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Permissivo por padrão; {@link #forget(UUID)} simula um órgão gestor inexistente. */
public class InMemoryManagingUnitDirectory implements ManagingUnitDirectory {

	private final Set<UUID> unknown = new HashSet<>();

	@Override
	public boolean exists(UUID managingUnitId) {
		return !unknown.contains(managingUnitId);
	}

	public void forget(UUID managingUnitId) {
		unknown.add(managingUnitId);
	}
}
