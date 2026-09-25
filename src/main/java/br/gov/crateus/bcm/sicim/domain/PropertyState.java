package br.gov.crateus.bcm.sicim.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Estado completo e imutável do agregado — usado para reconstituir e para mapear/serializar. */
public record PropertyState(
		UUID id,
		RegistrationNumber registrationNumber,
		String notaryOffice,
		String notarialDescription,
		Address address,
		BigDecimal totalArea,
		BigDecimal builtArea,
		Geolocation geolocation,
		UUID managingUnitId,
		String budgetUnit,
		UsageCategory usageCategory,
		String customCategoryName,
		PossessionType possessionType,
		PossessionContract possessionContract,
		Integer acquisitionYear,
		MonetaryValue originalValue,
		MonetaryValue accumulatedDepreciation,
		String publicPurpose,
		PropertyStatus status,
		String approvedBy,
		OffsetDateTime approvedAt,
		PropertyAudit audit
) {
}
