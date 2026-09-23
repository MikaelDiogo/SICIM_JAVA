package br.gov.crateus.bcm.sicim.application.command;

import br.gov.crateus.bcm.sicim.domain.PossessionType;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import java.math.BigDecimal;
import java.util.UUID;

public record RegisterPropertyCommand(
		String registrationNumber,
		String notaryOffice,
		String notarialDescription,
		AddressInput address,
		BigDecimal totalArea,
		BigDecimal builtArea,
		BigDecimal latitude,
		BigDecimal longitude,
		UUID managingUnitId,
		String budgetUnit,
		UsageCategory usageCategory,
		String customCategoryName,
		PossessionType possessionType,
		PossessionContractInput possessionContract,
		Integer acquisitionYear,
		BigDecimal originalValue,
		String publicPurpose
) {
}
