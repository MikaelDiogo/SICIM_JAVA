package br.gov.crateus.bcm.sicim.api.dto;

import br.gov.crateus.bcm.sicim.application.command.RegisterPropertyCommand;
import br.gov.crateus.bcm.sicim.domain.PossessionType;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record RegisterPropertyRequest(
		@Schema(example = "MAT-2024-00001") @NotBlank String registrationNumber,
		@NotBlank @Size(max = 255) String notaryOffice,
		@NotBlank String notarialDescription,
		@NotNull @Valid AddressRequest address,
		@NotNull @Positive BigDecimal totalArea,
		@NotNull @Positive BigDecimal builtArea,
		@NotNull @DecimalMin("-5.65") @DecimalMax("-4.70") BigDecimal latitude,
		@NotNull @DecimalMin("-41.20") @DecimalMax("-40.10") BigDecimal longitude,
		@Schema(description = "UUID do órgão gestor na plataforma (organization)") @NotNull UUID managingUnitId,
		@Size(max = 100) String budgetUnit,
		@NotNull UsageCategory usageCategory,
		@Size(max = 100) String customCategoryName,
		@NotNull PossessionType possessionType,
		@Schema(description = "Obrigatório quando possessionType != OWNED") @Valid PossessionContractRequest possessionContract,
		@NotNull @Min(1800) Integer acquisitionYear,
		@NotNull @Positive BigDecimal originalValue,
		@NotBlank String publicPurpose
) {

	public RegisterPropertyCommand toCommand() {
		return new RegisterPropertyCommand(registrationNumber, notaryOffice, notarialDescription, address.toInput(),
				totalArea, builtArea, latitude, longitude, managingUnitId, budgetUnit, usageCategory,
				customCategoryName, possessionType, possessionContract == null ? null : possessionContract.toInput(),
				acquisitionYear, originalValue, publicPurpose);
	}
}
