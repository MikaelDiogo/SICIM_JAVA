package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sicim.domain.LifecycleStatus;
import br.gov.crateus.bcm.sicim.domain.ManagingUnit;

final class ManagingUnitPersistenceMapper {

	private ManagingUnitPersistenceMapper() {
	}

	static ManagingUnit toDomain(ManagingUnitEntity e) {
		return new ManagingUnit(e.getId(), e.getName(), e.getAcronym(), e.getType(),
				LifecycleStatus.valueOf(e.getLifecycleStatus()), e.getCreatedAt());
	}

	static void copyToEntity(ManagingUnit unit, ManagingUnitEntity entity) {
		entity.setName(unit.name());
		entity.setAcronym(unit.acronym());
		entity.setType(unit.type());
		entity.setLifecycleStatus(unit.lifecycleStatus().name());
	}
}
