package br.gov.crateus.bcm.sicim.api.dto;

import br.gov.crateus.bcm.sicim.application.command.RegisterManagingUnitCommand;
import br.gov.crateus.bcm.sicim.domain.ManagingUnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterManagingUnitRequest(
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Size(max = 20) String acronym,
		@NotNull ManagingUnitType type
) {

	public RegisterManagingUnitCommand toCommand() {
		return new RegisterManagingUnitCommand(name, acronym, type);
	}
}
