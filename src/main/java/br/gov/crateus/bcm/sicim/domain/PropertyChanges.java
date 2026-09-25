package br.gov.crateus.bcm.sicim.domain;

import java.math.BigDecimal;
import java.util.UUID;

/** Alteração parcial: {@code null} = manter o valor atual. Status não é editável (RN12/RN11). */
public record PropertyChanges(
		RegistrationNumber registrationNumber,
		String notaryOffice,
		String notarialDescription,
		Address address,
		BigDecimal totalArea,
		BigDecimal builtArea,
		BigDecimal latitude,
		BigDecimal longitude,
		UUID managingUnitId,
		String budgetUnit,
		UsageCategory usageCategory,
		String customCategoryName,
		PossessionType possessionType,
		PossessionContract possessionContract,
		Integer acquisitionYear,
		MonetaryValue originalValue,
		String publicPurpose
) {
}
