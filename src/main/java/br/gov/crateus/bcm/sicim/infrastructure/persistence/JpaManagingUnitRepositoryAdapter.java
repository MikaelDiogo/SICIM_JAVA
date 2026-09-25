package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitRepository;
import br.gov.crateus.bcm.sicim.domain.ManagingUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaManagingUnitRepositoryAdapter implements ManagingUnitRepository {

	private static final String SOURCE_WEB = "WEB";
	private static final String ACTIVE = "ACTIVE";

	private final ManagingUnitJpaRepository jpa;

	JpaManagingUnitRepositoryAdapter(ManagingUnitJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public Optional<ManagingUnit> findById(UUID id) {
		return jpa.findById(id).map(ManagingUnitPersistenceMapper::toDomain);
	}

	@Override
	public List<ManagingUnit> findAllActive() {
		return jpa.findByLifecycleStatusOrderByNameAsc(ACTIVE).stream()
				.map(ManagingUnitPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public ManagingUnit save(ManagingUnit unit) {
		ManagingUnitEntity entity = jpa.findById(unit.id()).orElseGet(() -> newEntity(unit.id()));
		ManagingUnitPersistenceMapper.copyToEntity(unit, entity);
		return ManagingUnitPersistenceMapper.toDomain(jpa.saveAndFlush(entity));
	}

	private static ManagingUnitEntity newEntity(UUID id) {
		ManagingUnitEntity entity = new ManagingUnitEntity();
		entity.assignId(id);
		entity.setSource(SOURCE_WEB);
		return entity;
	}
}
