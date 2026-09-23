package br.gov.crateus.bcm.sicim.domain;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * Depreciação linear anual por categoria de uso (política contábil do município).
 * Parametrizável — ajuste as taxas aqui conforme a política evoluir.
 */
public final class DepreciationCalculator {

	public static final Map<UsageCategory, BigDecimal> ANNUAL_RATE = new EnumMap<>(Map.of(
			UsageCategory.ADMINISTRATIVE, new BigDecimal("0.02"),
			UsageCategory.EDUCATIONAL, new BigDecimal("0.02"),
			UsageCategory.HEALTH, new BigDecimal("0.025"),
			UsageCategory.SOCIAL_ASSISTANCE, new BigDecimal("0.02"),
			UsageCategory.CULTURAL, new BigDecimal("0.015"),
			UsageCategory.OTHER, new BigDecimal("0.02")
	));

	private DepreciationCalculator() {
	}

	public static MonetaryValue calculate(MonetaryValue originalValue, UsageCategory category,
			int acquisitionYear, int currentYear) {
		int yearsElapsed = Math.max(0, currentYear - acquisitionYear);
		BigDecimal depreciated = originalValue.amount()
				.multiply(ANNUAL_RATE.get(category))
				.multiply(BigDecimal.valueOf(yearsElapsed));
		return MonetaryValue.of(depreciated).min(originalValue);
	}
}
