package br.gov.crateus.bcm.sicim.domain;

import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.util.regex.Pattern;

/** Matrícula do imóvel no formato MAT-YYYY-NNNNN (ex.: MAT-2024-00001). */
public record RegistrationNumber(String value) {

	private static final Pattern FORMAT = Pattern.compile("^MAT-\\d{4}-\\d{5}$");

	public RegistrationNumber {
		if (value == null || !FORMAT.matcher(value).matches()) {
			throw SicimDomainException.validation(
					"Invalid registration number: \"" + value + "\". Use the format MAT-YYYY-NNNNN.");
		}
	}

	public static RegistrationNumber of(String raw) {
		return new RegistrationNumber(raw == null ? null : raw.trim().toUpperCase());
	}
}
