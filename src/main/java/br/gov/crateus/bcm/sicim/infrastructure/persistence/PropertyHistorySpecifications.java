package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class PropertyHistorySpecifications {

	private PropertyHistorySpecifications() {
	}

	public static Specification<PropertyHistoryEntity> forProperty(UUID propertyId) {
		return propertyId == null ? null : (root, q, cb) -> cb.equal(root.get("propertyId"), propertyId);
	}

	public static Specification<PropertyHistoryEntity> byAuthor(String author) {
		return author == null || author.isBlank() ? null : (root, q, cb) -> cb.equal(root.get("createdBy"), author);
	}

	public static Specification<PropertyHistoryEntity> withAction(PropertyHistoryAction action) {
		return action == null ? null : (root, q, cb) -> cb.equal(root.get("action"), action);
	}
}
