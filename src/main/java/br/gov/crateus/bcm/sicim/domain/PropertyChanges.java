package br.gov.crateus.bcm.sicim.domain;

import java.math.BigDecimal;
import java.util.UUID;

/** Alteração parcial: {@code null} = manter o valor atual. Matrícula e status não são editáveis. */
public record PropertyChanges(
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
