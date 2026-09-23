package br.gov.crateus.bcm.sicim.application.result;

import br.gov.crateus.bcm.sicim.domain.PossessionContract;
import br.gov.crateus.bcm.sicim.domain.PossessionType;
import br.gov.crateus.bcm.sicim.domain.Property;
import br.gov.crateus.bcm.sicim.domain.PropertyState;
import br.gov.crateus.bcm.sicim.domain.PropertyStatus;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Representação do imóvel — campos compatíveis com o PropertyPresenter do NestJS (reuso do front). */
public record PropertyResult(
		UUID id,
		String registrationNumber,
		String notaryOffice,
		String notarialDescription,
		Address address,
		BigDecimal totalArea,
		BigDecimal builtArea,
		BigDecimal latitude,
		BigDecimal longitude,
		UUID managingUnitId,
		String budgetUnit,
		UsageCategory usageCategory,
		String customCategoryName,
		PossessionType possessionType,
		Contract possessionContract,
		Integer acquisitionYear,
		BigDecimal originalValue,
		BigDecimal accumulatedDepreciation,
		BigDecimal netBookValue,
		String publicPurpose,
		PropertyStatus status,
		String createdById,
		String approvedById,
		OffsetDateTime approvedAt,
		String lifecycleStatus,
		long version,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {

	public record Address(String street, String number, String neighborhood, UUID neighborhoodId,
			String zipCode, String reference) {
	}

	public record Contract(OffsetDateTime startDate, OffsetDateTime endDate, BigDecimal monthlyValue,
			BigDecimal referenceValue, String grantor, String lessor, String administrativeProcessNumber) {

		static Contract from(PossessionContract c) {
			return c == null ? null : new Contract(c.startDate(), c.endDate(), c.monthlyValue(),
					c.referenceValue(), c.grantor(), c.lessor(), c.administrativeProcessNumber());
		}
	}

	public static PropertyResult from(Property property) {
		PropertyState s = property.state();
		var a = s.address();
		return new PropertyResult(
				s.id(), s.registrationNumber().value(), s.notaryOffice(), s.notarialDescription(),
				new Address(a.street(), a.number(), a.neighborhood(), a.neighborhoodId(), a.zipCode(), a.reference()),
				s.totalArea(), s.builtArea(), s.geolocation().latitude(), s.geolocation().longitude(),
				s.managingUnitId(), s.budgetUnit(), s.usageCategory(), s.customCategoryName(), s.possessionType(),
				Contract.from(s.possessionContract()), s.acquisitionYear(), s.originalValue().amount(),
				s.accumulatedDepreciation().amount(), property.netBookValue().amount(), s.publicPurpose(),
				s.status(), s.audit().createdBy(), s.approvedBy(), s.approvedAt(),
				s.audit().lifecycleStatus().name(), s.audit().version(), s.audit().createdAt(),
				s.audit().updatedAt());
	}
}
