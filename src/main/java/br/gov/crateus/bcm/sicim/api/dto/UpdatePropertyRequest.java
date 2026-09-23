package br.gov.crateus.bcm.sicim.api.dto;

import br.gov.crateus.bcm.sicim.application.command.UpdatePropertyCommand;
import br.gov.crateus.bcm.sicim.domain.PossessionType;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

/** PATCH parcial — campos ausentes não são alterados. */
public record UpdatePropertyRequest(
		@Size(min = 1, max = 255) String notaryOffice,
		@Size(min = 1) String notarialDescription,
		@Valid AddressRequest address,
		@Positive BigDecimal totalArea,
		@Positive BigDecimal builtArea,
		@DecimalMin("-5.65") @DecimalMax("-4.70") BigDecimal latitude,
		@DecimalMin("-41.20") @DecimalMax("-40.10") BigDecimal longitude,
		UUID managingUnitId,
		@Size(max = 100) String budgetUnit,
		UsageCategory usageCategory,
		@Size(max = 100) String customCategoryName,
		PossessionType possessionType,
		@Valid PossessionContractRequest possessionContract,
		@Min(1800) Integer acquisitionYear,
		@Positive BigDecimal originalValue,
		@Size(min = 1) String publicPurpose
) {

	public UpdatePropertyCommand toCommand() {
		return new UpdatePropertyCommand(notaryOffice, notarialDescription,
				address == null ? null : address.toInput(), totalArea, builtArea, latitude, longitude,
				managingUnitId, budgetUnit, usageCategory, customCategoryName, possessionType,
				possessionContract == null ? null : possessionContract.toInput(), acquisitionYear, originalValue,
				publicPurpose);
	}
}
