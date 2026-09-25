package br.gov.crateus.bcm.sicim.application.support;

import br.gov.crateus.bcm.sicim.application.command.AddressInput;
import br.gov.crateus.bcm.sicim.application.command.PossessionContractInput;
import br.gov.crateus.bcm.sicim.application.command.RegisterPropertyCommand;
import br.gov.crateus.bcm.sicim.application.command.UpdatePropertyCommand;
import br.gov.crateus.bcm.sicim.domain.Address;
import br.gov.crateus.bcm.sicim.domain.Geolocation;
import br.gov.crateus.bcm.sicim.domain.MonetaryValue;
import br.gov.crateus.bcm.sicim.domain.NewProperty;
import br.gov.crateus.bcm.sicim.domain.PossessionContract;
import br.gov.crateus.bcm.sicim.domain.PropertyChanges;
import br.gov.crateus.bcm.sicim.domain.RegistrationNumber;
import java.math.BigDecimal;

/** Converte comandos (primitivos) em objetos de domínio (value objects validados). */
public final class PropertyCommandMapper {

	private PropertyCommandMapper() {
	}

	public static NewProperty toNewProperty(RegisterPropertyCommand c) {
		return new NewProperty(
				RegistrationNumber.of(c.registrationNumber()),
				c.notaryOffice(),
				c.notarialDescription(),
				toAddress(c.address()),
				c.totalArea(),
				c.builtArea(),
				new Geolocation(c.latitude(), c.longitude()),
				c.managingUnitId(),
				c.budgetUnit(),
				c.usageCategory(),
				c.customCategoryName(),
				c.possessionType(),
				toContract(c.possessionContract()),
				c.acquisitionYear(),
				toMoney(c.originalValue()),
				c.publicPurpose());
	}

	public static PropertyChanges toChanges(UpdatePropertyCommand c) {
		return new PropertyChanges(
				RegistrationNumber.of(c.registrationNumber()),
				c.notaryOffice(),
				c.notarialDescription(),
				toAddress(c.address()),
				c.totalArea(),
				c.builtArea(),
				c.latitude(),
				c.longitude(),
				c.managingUnitId(),
				c.budgetUnit(),
				c.usageCategory(),
				c.customCategoryName(),
				c.possessionType(),
				toContract(c.possessionContract()),
				c.acquisitionYear(),
				toMoney(c.originalValue()),
				c.publicPurpose());
	}

	private static Address toAddress(AddressInput in) {
		return in == null ? null : new Address(in.street(), in.number(), in.neighborhood(), in.neighborhoodId(),
				in.zipCode(), in.reference());
	}

	private static PossessionContract toContract(PossessionContractInput in) {
		return in == null ? null : new PossessionContract(in.startDate(), in.endDate(), in.monthlyValue(),
				in.referenceValue(), in.grantor(), in.lessor(), in.administrativeProcessNumber());
	}

	private static MonetaryValue toMoney(BigDecimal amount) {
		return amount == null ? null : MonetaryValue.of(amount);
	}
}
