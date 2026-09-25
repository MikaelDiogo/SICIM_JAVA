package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ManagingUnitJpaRepository extends JpaRepository<ManagingUnitEntity, UUID> {

	List<ManagingUnitEntity> findByLifecycleStatusOrderByNameAsc(String lifecycleStatus);
}
