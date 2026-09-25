package br.gov.crateus.bcm.sicim.application.fake;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitRepository;
import br.gov.crateus.bcm.sicim.domain.LifecycleStatus;
import br.gov.crateus.bcm.sicim.domain.ManagingUnit;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryManagingUnitRepository implements ManagingUnitRepository {

	private final Map<UUID, ManagingUnit> store = new LinkedHashMap<>();

	@Override
	public Optional<ManagingUnit> findById(UUID id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public List<ManagingUnit> findAllActive() {
		return store.values().stream().filter(u -> u.lifecycleStatus() == LifecycleStatus.ACTIVE).toList();
	}

	@Override
	public ManagingUnit save(ManagingUnit unit) {
		ManagingUnit persisted = unit.createdAt() == null
				? new ManagingUnit(unit.id(), unit.name(), unit.acronym(), unit.type(), unit.lifecycleStatus(),
						OffsetDateTime.now())
				: unit;
		store.put(persisted.id(), persisted);
		return persisted;
	}
}
