package br.gov.crateus.bcm.sicim.domain;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/** Entrada append-only do histórico. {@code id}, {@code author} e {@code recordedAt} vêm da persistência. */
public record PropertyHistoryEntry(
		UUID id,
		UUID propertyId,
		PropertyHistoryAction action,
		Map<String, Object> dataBefore,
		Map<String, Object> dataAfter,
		String correlationId,
		String author,
		OffsetDateTime recordedAt
) {

	public static PropertyHistoryEntry of(UUID propertyId, PropertyHistoryAction action,
			Map<String, Object> dataBefore, Map<String, Object> dataAfter, String correlationId) {
		return new PropertyHistoryEntry(null, propertyId, action, dataBefore, dataAfter, correlationId, null, null);
	}
}
