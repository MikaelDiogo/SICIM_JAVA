package br.gov.crateus.bcm.sicim.application.result;

import br.gov.crateus.bcm.sicim.domain.ManagingUnit;
import br.gov.crateus.bcm.sicim.domain.ManagingUnitType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ManagingUnitResult(UUID id, String name, String acronym, ManagingUnitType type,
		String lifecycleStatus, OffsetDateTime createdAt) {

	public static ManagingUnitResult from(ManagingUnit unit) {
		return new ManagingUnitResult(unit.id(), unit.name(), unit.acronym(), unit.type(),
				unit.lifecycleStatus().name(), unit.createdAt());
	}
}
