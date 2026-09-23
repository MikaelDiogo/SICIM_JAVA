package br.gov.crateus.bcm.sicim.api.dto;

import br.gov.crateus.bcm.sicim.application.command.PropertyHistoryFilter;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;

public record ListPropertyHistoryQuery(
		@Schema(description = "Filtra por imóvel (equivale a entityId no NestJS)") UUID propertyId,
		@Schema(description = "Filtra por autor (sub do JWT)") String userId,
		PropertyHistoryAction action,
		@Min(1) Integer page,
		@Min(1) @Max(100) Integer pageSize
) {

	public PropertyHistoryFilter toFilter() {
		return new PropertyHistoryFilter(propertyId, userId, action, page == null ? 1 : page,
				pageSize == null ? 20 : pageSize);
	}
}
