package br.gov.crateus.bcm.sicim.application.result;

import br.gov.crateus.bcm.sicim.domain.PossessionType;
import br.gov.crateus.bcm.sicim.domain.PropertyStatus;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyEntity;
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
	}

	public static PropertyResult from(PropertyEntity e) {
		Contract contract = e.getContractStartDate() == null ? null : new Contract(
				e.getContractStartDate(), e.getContractEndDate(), e.getContractMonthlyValue(),
				e.getContractReferenceValue(), e.getContractGrantor(), e.getContractLessor(),
				e.getContractAdministrativeProcessNumber());
		BigDecimal netBookValue = e.getOriginalValue().subtract(e.getAccumulatedDepreciation()).max(BigDecimal.ZERO);
		return new PropertyResult(
				e.getId(), e.getRegistrationNumber(), e.getNotaryOffice(), e.getNotarialDescription(),
				new Address(e.getAddressStreet(), e.getAddressNumber(), e.getAddressNeighborhood(),
						e.getNeighborhoodId(), e.getAddressZipCode(), e.getAddressReference()),
				e.getTotalArea(), e.getBuiltArea(), e.getLatitude(), e.getLongitude(), e.getManagingUnitId(),
				e.getBudgetUnit(), e.getUsageCategory(), e.getCustomCategoryName(), e.getPossessionType(), contract,
				e.getAcquisitionYear(), e.getOriginalValue(), e.getAccumulatedDepreciation(), netBookValue,
				e.getPublicPurpose(), e.getStatus(), e.getCreatedBy(), e.getApprovedBy(), e.getApprovedAt(),
				e.getLifecycleStatus(), e.getVersion(), e.getCreatedAt(), e.getUpdatedAt());
	}
}
