package br.gov.crateus.bcm.sicim.domain;

import java.math.BigDecimal;
import java.util.UUID;

/** Dados de cadastro já convertidos em value objects. */
public record NewProperty(
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
		String publicPurpose
) {
}
