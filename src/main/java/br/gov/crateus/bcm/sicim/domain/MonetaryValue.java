package br.gov.crateus.bcm.sicim.domain;

import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Valor em BRL, 2 casas, nunca negativo (regra de patrimônio público). */
public record MonetaryValue(BigDecimal amount) {

	public static final MonetaryValue ZERO = new MonetaryValue(BigDecimal.ZERO);

	public MonetaryValue {
		if (amount == null || amount.signum() < 0) {
			throw SicimDomainException.validation("Monetary value cannot be negative. Received: " + amount);
		}
		amount = amount.setScale(2, RoundingMode.HALF_UP);
	}

	public static MonetaryValue of(BigDecimal amount) {
		return new MonetaryValue(amount);
	}

	/** Subtração com piso em zero. */
	public MonetaryValue subtract(MonetaryValue other) {
		return new MonetaryValue(amount.subtract(other.amount).max(BigDecimal.ZERO));
	}

	public MonetaryValue min(MonetaryValue other) {
		return amount.compareTo(other.amount) <= 0 ? this : other;
	}
}
