package br.gov.crateus.bcm.sicim.domain;

public enum PossessionType {
	OWNED,
	RENTED,
	GRANTED,
	LOAN,
	USUFRUCT,
	USE_PERMIT;

	/** Todo tipo de posse que não seja próprio exige contrato. */
	public boolean requiresContract() {
		return this != OWNED;
	}
}
