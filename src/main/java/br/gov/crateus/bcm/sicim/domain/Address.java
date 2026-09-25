package br.gov.crateus.bcm.sicim.domain;

import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Endereço do imóvel. {@code neighborhoodId} referencia o bairro canônico da plataforma (geography);
 * {@code neighborhood} é o rótulo textual mantido por compatibilidade.
 */
public record Address(String street, String number, String neighborhood, UUID neighborhoodId,
		String zipCode, String reference) {

	private static final Pattern ZIP_CODE = Pattern.compile("^\\d{5}-?\\d{3}$");

	public Address {
		// Só o CEP é obrigatório (ver REGRAS.md) — rua/número/bairro podem ser completados depois.
		street = Text.optional(street);
		number = Text.optional(number);
		neighborhood = Text.optional(neighborhood);
		zipCode = zipCode == null ? null : zipCode.trim();
		reference = Text.optional(reference);
		if (zipCode == null || !ZIP_CODE.matcher(zipCode).matches()) {
			throw SicimDomainException.validation(
					"Invalid zip code: \"" + zipCode + "\". Use the format NNNNN-NNN.");
		}
	}
}
