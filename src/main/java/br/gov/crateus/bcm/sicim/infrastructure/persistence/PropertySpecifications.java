package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sicim.domain.PropertyStatus;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Filtros de listagem (equivalentes ao QueryBuilder do TypeORM). Todos opcionais. */
public final class PropertySpecifications {

	private PropertySpecifications() {
	}

	public static Specification<PropertyEntity> notDeleted() {
		return (root, q, cb) -> cb.notEqual(root.get("lifecycleStatus"), "DELETED");
	}

	public static Specification<PropertyEntity> hasStatus(PropertyStatus status) {
		return status == null ? null : (root, q, cb) -> cb.equal(root.get("status"), status);
	}

	public static Specification<PropertyEntity> hasUsageCategory(UsageCategory category) {
		return category == null ? null : (root, q, cb) -> cb.equal(root.get("usageCategory"), category);
	}

	public static Specification<PropertyEntity> hasManagingUnit(UUID managingUnitId) {
		return managingUnitId == null ? null : (root, q, cb) -> cb.equal(root.get("managingUnitId"), managingUnitId);
	}

	public static Specification<PropertyEntity> acquiredFrom(Integer year) {
		return year == null ? null : (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("acquisitionYear"), year);
	}

	public static Specification<PropertyEntity> acquiredTo(Integer year) {
		return year == null ? null : (root, q, cb) -> cb.lessThanOrEqualTo(root.get("acquisitionYear"), year);
	}
}
