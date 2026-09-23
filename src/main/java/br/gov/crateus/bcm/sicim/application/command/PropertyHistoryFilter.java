package br.gov.crateus.bcm.sicim.application.command;

import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import java.util.UUID;

public record PropertyHistoryFilter(UUID propertyId, String userId, PropertyHistoryAction action,
		int page, int pageSize) {
}
