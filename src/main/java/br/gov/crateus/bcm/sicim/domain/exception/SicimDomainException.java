package br.gov.crateus.bcm.sicim.domain.exception;

/** Erro de regra de negócio do SICIM. A camada api traduz para Problem Details (RFC 7807). */
public class SicimDomainException extends RuntimeException {

	private final ErrorType type;

	public SicimDomainException(String message, ErrorType type) {
		super(message);
		this.type = type;
	}

	public static SicimDomainException validation(String message) {
		return new SicimDomainException(message, ErrorType.VALIDATION);
	}

	public static SicimDomainException conflict(String message) {
		return new SicimDomainException(message, ErrorType.CONFLICT);
	}

	public static SicimDomainException notFound(String message) {
		return new SicimDomainException(message, ErrorType.NOT_FOUND);
	}

	public ErrorType getType() {
		return type;
	}
}
