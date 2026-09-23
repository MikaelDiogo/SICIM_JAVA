package br.gov.crateus.bcm.sicim.api.dto;

import br.gov.crateus.bcm.sicim.application.command.PossessionContractInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PossessionContractRequest(
		@NotNull OffsetDateTime startDate,
		OffsetDateTime endDate,
		@Positive BigDecimal monthlyValue,
		@Positive BigDecimal referenceValue,
		@Size(max = 255) String grantor,
		@Size(max = 255) String lessor,
		@NotBlank @Size(max = 100) String administrativeProcessNumber
) {

	public PossessionContractInput toInput() {
		return new PossessionContractInput(startDate, endDate, monthlyValue, referenceValue, grantor, lessor,
				administrativeProcessNumber);
	}
}
