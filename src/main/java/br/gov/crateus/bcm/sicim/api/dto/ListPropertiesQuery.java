package br.gov.crateus.bcm.sicim.api.dto;

import br.gov.crateus.bcm.sicim.application.command.PropertyFilter;
import br.gov.crateus.bcm.sicim.domain.PropertyStatus;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;

public record ListPropertiesQuery(
		PropertyStatus status,
		UsageCategory usageCategory,
		UUID managingUnitId,
		@Min(1800) Integer acquisitionYearFrom,
		@Min(1800) Integer acquisitionYearTo,
		@Min(1) Integer page,
		@Min(1) @Max(100) Integer pageSize
) {

	public PropertyFilter toFilter() {
		return new PropertyFilter(status, usageCategory, managingUnitId, acquisitionYearFrom, acquisitionYearTo,
				page == null ? 1 : page, pageSize == null ? 20 : pageSize);
	}
}
