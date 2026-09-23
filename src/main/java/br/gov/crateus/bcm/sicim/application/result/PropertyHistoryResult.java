package br.gov.crateus.bcm.sicim.application.result;

import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyHistoryEntity;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/** Campos compatíveis com o AuditLogPresenter do NestJS (id, userId, affectedEntity, entityId, ...). */
public record PropertyHistoryResult(
		UUID id,
		String userId,
		String affectedEntity,
		UUID entityId,
		PropertyHistoryAction action,
		Map<String, Object> dataBefore,
		Map<String, Object> dataAfter,
		String correlationId,
		OffsetDateTime timestamp
) {

	public static PropertyHistoryResult from(PropertyHistoryEntity e) {
		return new PropertyHistoryResult(e.getId(), e.getCreatedBy(), "Property", e.getPropertyId(), e.getAction(),
				e.getDataBefore(), e.getDataAfter(), e.getCorrelationId(), e.getCreatedAt());
	}
}
