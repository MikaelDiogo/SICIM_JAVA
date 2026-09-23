package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PropertyJpaRepository extends JpaRepository<PropertyEntity, UUID>,
		JpaSpecificationExecutor<PropertyEntity> {

	boolean existsByRegistrationNumber(String registrationNumber);

	@Query("""
			select distinct p.customCategoryName from PropertyEntity p
			where p.customCategoryName is not null and p.lifecycleStatus <> 'DELETED'
			order by p.customCategoryName
			""")
	List<String> findDistinctCustomCategoryNames();
}
