package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PropertyHistoryJpaRepository extends JpaRepository<PropertyHistoryEntity, UUID>,
		JpaSpecificationExecutor<PropertyHistoryEntity> {
}
