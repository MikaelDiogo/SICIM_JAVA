package br.gov.crateus.bcm.sicim.application;

import br.gov.crateus.bcm.sicim.application.command.PropertyHistoryFilter;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.application.result.PropertyHistoryResult;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyHistoryEntity;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyHistoryJpaRepository;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyHistorySpecifications;
import java.util.Arrays;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta do histórico de imóveis (substitui o módulo audit-log do NestJS). Somente leitura. */
@Service
public class PropertyHistoryService {

	private final PropertyHistoryJpaRepository history;

	public PropertyHistoryService(PropertyHistoryJpaRepository history) {
		this.history = history;
	}

	@Transactional(readOnly = true)
	public PageResult<PropertyHistoryResult> list(PropertyHistoryFilter f) {
		Specification<PropertyHistoryEntity> spec = Specification.allOf(Arrays.asList(
				PropertyHistorySpecifications.forProperty(f.propertyId()),
				PropertyHistorySpecifications.byAuthor(f.userId()),
				PropertyHistorySpecifications.withAction(f.action())));
		PageRequest pageable = PageRequest.of(f.page() - 1, f.pageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<PropertyHistoryEntity> page = history.findAll(spec, pageable);
		return new PageResult<>(page.map(PropertyHistoryResult::from).getContent(), page.getTotalElements(),
				f.page(), f.pageSize());
	}
}
