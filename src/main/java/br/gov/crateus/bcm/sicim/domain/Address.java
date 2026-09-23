package br.gov.crateus.bcm.sicim.domain;

import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.util.regex.Pattern;

public record Address(String street, String number, String neighborhood, String zipCode, String reference) {

	private static final Pattern ZIP_CODE = Pattern.compile("^\\d{5}-?\\d{3}$");

	public Address {
		if (isBlank(street) || isBlank(number) || isBlank(neighborhood)) {
			throw SicimDomainException.validation("Address street, number and neighborhood are required.");
		}
		if (zipCode == null || !ZIP_CODE.matcher(zipCode).matches()) {
			throw SicimDomainException.validation(
					"Invalid zip code: \"" + zipCode + "\". Use the format NNNNN-NNN.");
		}
	}

	private static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}
}
