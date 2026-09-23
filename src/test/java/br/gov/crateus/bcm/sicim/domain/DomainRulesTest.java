package br.gov.crateus.bcm.sicim.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.gov.crateus.bcm.sicim.domain.exception.ErrorType;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class DomainRulesTest {

	@Test
	void registrationNumberIsNormalizedAndValidated() {
		assertThat(RegistrationNumber.of(" mat-2024-00001 ").value()).isEqualTo("MAT-2024-00001");
		assertThatThrownBy(() -> RegistrationNumber.of("2024-1")).isInstanceOf(SicimDomainException.class);
	}

	@Test
	void zipCodeMustMatchFormat() {
		new Address("Rua A", "10", "Centro", "63700-000", null);
		new Address("Rua A", "10", "Centro", "63700000", null);
		assertThatThrownBy(() -> new Address("Rua A", "10", "Centro", "6370", null))
				.isInstanceOf(SicimDomainException.class);
	}

	@Test
	void geolocationMustBeInsideCrateus() {
		new Geolocation(new BigDecimal("-5.1783"), new BigDecimal("-40.6775"));
		assertThatThrownBy(() -> new Geolocation(new BigDecimal("-3.7319"), new BigDecimal("-38.5267")))
				.isInstanceOf(SicimDomainException.class);
	}

	@Test
	void monetaryValueNeverNegative() {
		assertThatThrownBy(() -> MonetaryValue.of(new BigDecimal("-1"))).isInstanceOf(SicimDomainException.class);
		MonetaryValue small = MonetaryValue.of(new BigDecimal("10"));
		assertThat(small.subtract(MonetaryValue.of(new BigDecimal("50"))).amount())
				.isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void builtAreaCannotExceedTotalArea() {
		PropertyRules.validateAreas(new BigDecimal("100"), new BigDecimal("100"));
		assertThatThrownBy(() -> PropertyRules.validateAreas(new BigDecimal("100"), new BigDecimal("100.01")))
				.isInstanceOf(SicimDomainException.class);
	}

	@Test
	void nonOwnedPossessionRequiresContract() {
		PropertyRules.validatePossession(PossessionType.OWNED, null);
		for (PossessionType type : PossessionType.values()) {
			if (type != PossessionType.OWNED) {
				assertThatThrownBy(() -> PropertyRules.validatePossession(type, null))
						.isInstanceOf(SicimDomainException.class);
			}
		}
	}

	@Test
	void contractEndCannotPrecedeStart() {
		OffsetDateTime start = OffsetDateTime.parse("2025-01-01T00:00:00Z");
		assertThatThrownBy(() -> new PossessionContract(start, start.minusDays(1), null, null, null, null, "PA-1"))
				.isInstanceOf(SicimDomainException.class);
	}

	@Test
	void customCategoryOnlyForOther() {
		assertThat(PropertyRules.normalizeCustomCategory(UsageCategory.HEALTH, "UBS")).isNull();
		assertThat(PropertyRules.normalizeCustomCategory(UsageCategory.OTHER, " Galpão ")).isEqualTo("Galpão");
	}

	@Test
	void inactivePropertyCannotBeApproved() {
		assertThatThrownBy(() -> PropertyRules.ensureCanApprove(PropertyStatus.INACTIVE))
				.isInstanceOfSatisfying(SicimDomainException.class,
						e -> assertThat(e.getType()).isEqualTo(ErrorType.CONFLICT));
	}

	@Test
	void depreciationIsLinearAndCappedAtOriginalValue() {
		MonetaryValue original = MonetaryValue.of(new BigDecimal("100000"));
		assertThat(DepreciationCalculator.calculate(original, UsageCategory.ADMINISTRATIVE, 2016, 2026).amount())
				.isEqualByComparingTo("20000.00");
		assertThat(DepreciationCalculator.calculate(original, UsageCategory.HEALTH, 1900, 2026).amount())
				.isEqualByComparingTo("100000.00");
		assertThat(DepreciationCalculator.calculate(original, UsageCategory.CULTURAL, 2030, 2026).amount())
				.isEqualByComparingTo("0.00");
	}
}
