package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sicim.application.command.PropertyHistoryFilter;
import br.gov.crateus.bcm.sicim.application.port.PropertyHistoryRepository;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryEntry;
import java.util.Arrays;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
class JpaPropertyHistoryRepositoryAdapter implements PropertyHistoryRepository {

	private static final String SOURCE_WEB = "WEB";

	private final PropertyHistoryJpaRepository jpa;

	JpaPropertyHistoryRepositoryAdapter(PropertyHistoryJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void append(PropertyHistoryEntry entry) {
		PropertyHistoryEntity entity = new PropertyHistoryEntity(entry.propertyId(), entry.action(),
				entry.dataBefore(), entry.dataAfter(), entry.correlationId());
		entity.setSource(SOURCE_WEB);
		jpa.save(entity);
	}

	@Override
	public PageResult<PropertyHistoryEntry> findPage(PropertyHistoryFilter f) {
		Specification<PropertyHistoryEntity> spec = Specification.allOf(Arrays.asList(
				PropertyHistorySpecifications.forProperty(f.propertyId()),
				PropertyHistorySpecifications.byAuthor(f.userId()),
				PropertyHistorySpecifications.withAction(f.action())));
		Page<PropertyHistoryEntity> page = jpa.findAll(spec,
				PageRequest.of(f.page() - 1, f.pageSize(), Sort.by(Sort.Direction.DESC, "createdAt")));
		return new PageResult<>(page.map(JpaPropertyHistoryRepositoryAdapter::toDomain).getContent(),
				page.getTotalElements(), f.page(), f.pageSize());
	}

	private static PropertyHistoryEntry toDomain(PropertyHistoryEntity e) {
		return new PropertyHistoryEntry(e.getId(), e.getPropertyId(), e.getAction(), e.getDataBefore(),
				e.getDataAfter(), e.getCorrelationId(), e.getCreatedBy(), e.getCreatedAt());
	}
}
