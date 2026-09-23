package br.gov.crateus.bcm.sicim.application.result;

import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryEntry;
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

	public static PropertyHistoryResult from(PropertyHistoryEntry e) {
		return new PropertyHistoryResult(e.id(), e.author(), "Property", e.propertyId(), e.action(),
				e.dataBefore(), e.dataAfter(), e.correlationId(), e.recordedAt());
	}
}
