package br.gov.crateus.bcm.sicim.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.gov.crateus.bcm.sicim.domain.exception.ErrorType;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PropertyTest {

	static final int YEAR = 2026;

	static NewProperty owned() {
		return new NewProperty(RegistrationNumber.of("MAT-2024-00001"), "Cartório", "Descrição",
				new Address("Rua A", "10", "Centro", null, "63700-000", null),
				new BigDecimal("500"), new BigDecimal("200"),
				new Geolocation(new BigDecimal("-5.1783"), new BigDecimal("-40.6775")),
				UUID.randomUUID(), null, UsageCategory.ADMINISTRATIVE, "ignored", PossessionType.OWNED, null,
				2016, MonetaryValue.of(new BigDecimal("100000")), "Sede administrativa");
	}

	static PropertyChanges changes(PossessionType type, PossessionContract contract, BigDecimal builtArea) {
		return new PropertyChanges(null, null, null, null, builtArea, null, null, null, null, null, null, type,
				contract, null, null, null);
	}

	@Test
	void registerStartsPendingWithZeroDepreciation() {
		Property p = Property.register(owned(), YEAR);

		assertThat(p.state().status()).isEqualTo(PropertyStatus.PENDING_APPROVAL);
		assertThat(p.state().accumulatedDepreciation().amount()).isEqualByComparingTo("0");
		assertThat(p.state().customCategoryName()).isNull();
		assertThat(p.state().audit().lifecycleStatus()).isEqualTo(LifecycleStatus.ACTIVE);
	}

	@Test
	void updateRevalidatesInvariants() {
		Property p = Property.register(owned(), YEAR);

		assertThatThrownBy(() -> p.update(changes(null, null, new BigDecimal("900")), YEAR))
				.isInstanceOf(SicimDomainException.class);
		assertThatThrownBy(() -> p.update(changes(PossessionType.RENTED, null, null), YEAR))
				.isInstanceOf(SicimDomainException.class);
		assertThat(p.state().builtArea()).isEqualByComparingTo("200");
	}

	@Test
	void switchingToRentedWithContractKeepsContract() {
		Property p = Property.register(owned(), YEAR);
		PossessionContract contract = new PossessionContract(OffsetDateTime.parse("2025-01-01T00:00:00Z"), null,
				new BigDecimal("2500"), null, null, "Locador", "PA-2025-001");

		p.update(changes(PossessionType.RENTED, contract, null), YEAR);

		assertThat(p.state().possessionContract()).isEqualTo(contract);
	}

	@Test
	void approveThenDeactivateThenApproveIsConflict() {
		Property p = Property.register(owned(), YEAR);
		p.approve("sub-1", OffsetDateTime.parse("2026-09-23T12:00:00Z"));
		assertThat(p.state().approvedBy()).isEqualTo("sub-1");

		p.deactivate();
		assertThat(p.state().audit().lifecycleStatus()).isEqualTo(LifecycleStatus.INACTIVE);

		assertThatThrownBy(() -> p.approve("sub-2", OffsetDateTime.now()))
				.isInstanceOfSatisfying(SicimDomainException.class,
						e -> assertThat(e.getType()).isEqualTo(ErrorType.CONFLICT));
	}

	@Test
	void recalculateDepreciationUpdatesNetBookValue() {
		Property p = Property.register(owned(), YEAR);

		p.recalculateDepreciation(YEAR);

		assertThat(p.state().accumulatedDepreciation().amount()).isEqualByComparingTo("20000.00");
		assertThat(p.netBookValue().amount()).isEqualByComparingTo("80000.00");
	}
}
