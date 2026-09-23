package br.gov.crateus.bcm.sicim.domain;

import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;

/** Normalização de texto de domínio (trim + obrigatoriedade). */
public final class Text {

	private Text() {
	}

	public static String required(String value, String field) {
		if (value == null || value.isBlank()) {
			throw SicimDomainException.validation(field + " is required.");
		}
		return value.trim();
	}

	public static String optional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
