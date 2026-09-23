package br.gov.crateus.bcm.sicim.application.command;

import br.gov.crateus.bcm.sicim.domain.PropertyStatus;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import java.util.UUID;

public record PropertyFilter(PropertyStatus status, UsageCategory usageCategory, UUID managingUnitId,
		Integer acquisitionYearFrom, Integer acquisitionYearTo, int page, int pageSize) {
}
