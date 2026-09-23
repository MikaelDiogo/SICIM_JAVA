package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sicim.application.command.PropertyFilter;
import br.gov.crateus.bcm.sicim.application.port.PropertyRepository;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.domain.Property;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
class JpaPropertyRepositoryAdapter implements PropertyRepository {

	private static final String SOURCE_WEB = "WEB";

	private final PropertyJpaRepository jpa;

	JpaPropertyRepositoryAdapter(PropertyJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public Optional<Property> findById(UUID id) {
		return jpa.findById(id).map(PropertyPersistenceMapper::toDomain);
	}

	@Override
	public boolean existsByRegistrationNumber(String registrationNumber) {
		return jpa.existsByRegistrationNumber(registrationNumber);
	}

	@Override
	public Property save(Property property) {
		PropertyEntity entity = jpa.findById(property.id()).orElseGet(() -> newEntity(property.id()));
		PropertyPersistenceMapper.copyToEntity(property, entity);
		// flush para devolver version/updated_at reais (lock otimista → 409 no mesmo request)
		return PropertyPersistenceMapper.toDomain(jpa.saveAndFlush(entity));
	}

	@Override
	public PageResult<Property> findPage(PropertyFilter f) {
		Specification<PropertyEntity> spec = Specification.allOf(Arrays.asList(
				PropertySpecifications.notDeleted(),
				PropertySpecifications.hasStatus(f.status()),
				PropertySpecifications.hasUsageCategory(f.usageCategory()),
				PropertySpecifications.hasManagingUnit(f.managingUnitId()),
				PropertySpecifications.acquiredFrom(f.acquisitionYearFrom()),
				PropertySpecifications.acquiredTo(f.acquisitionYearTo())));
		Page<PropertyEntity> page = jpa.findAll(spec,
				PageRequest.of(f.page() - 1, f.pageSize(), Sort.by(Sort.Direction.DESC, "createdAt")));
		return new PageResult<>(page.map(PropertyPersistenceMapper::toDomain).getContent(),
				page.getTotalElements(), f.page(), f.pageSize());
	}

	@Override
	public List<String> findCustomCategoryNames() {
		return jpa.findDistinctCustomCategoryNames();
	}

	private static PropertyEntity newEntity(UUID id) {
		PropertyEntity entity = new PropertyEntity();
		entity.assignId(id);
		entity.setSource(SOURCE_WEB);
		return entity;
	}
}
