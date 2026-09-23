package br.gov.crateus.bcm.sicim.api.dto;

import br.gov.crateus.bcm.sicim.application.command.AddressInput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AddressRequest(
		@NotBlank @Size(max = 255) String street,
		@NotBlank @Size(max = 20) String number,
		@NotBlank @Size(max = 100) String neighborhood,
		@Schema(description = "UUID do bairro na plataforma (geography). Opcional até a integração.")
		UUID neighborhoodId,
		@NotBlank @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "must match NNNNN-NNN") String zipCode,
		@Size(max = 255) String reference
) {

	public AddressInput toInput() {
		return new AddressInput(street, number, neighborhood, neighborhoodId, zipCode, reference);
	}
}
