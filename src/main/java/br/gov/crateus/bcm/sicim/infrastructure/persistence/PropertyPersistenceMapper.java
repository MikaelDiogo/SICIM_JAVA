package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sicim.domain.Address;
import br.gov.crateus.bcm.sicim.domain.Geolocation;
import br.gov.crateus.bcm.sicim.domain.LifecycleStatus;
import br.gov.crateus.bcm.sicim.domain.MonetaryValue;
import br.gov.crateus.bcm.sicim.domain.PossessionContract;
import br.gov.crateus.bcm.sicim.domain.Property;
import br.gov.crateus.bcm.sicim.domain.PropertyAudit;
import br.gov.crateus.bcm.sicim.domain.PropertyState;
import br.gov.crateus.bcm.sicim.domain.RegistrationNumber;

/** Tradução agregado ↔ entidade JPA. Única classe que conhece os dois lados. */
final class PropertyPersistenceMapper {

	private PropertyPersistenceMapper() {
	}

	static Property toDomain(PropertyEntity e) {
		PossessionContract contract = e.getContractStartDate() == null ? null : new PossessionContract(
				e.getContractStartDate(), e.getContractEndDate(), e.getContractMonthlyValue(),
				e.getContractReferenceValue(), e.getContractGrantor(), e.getContractLessor(),
				e.getContractAdministrativeProcessNumber());
		return Property.reconstitute(new PropertyState(
				e.getId(),
				e.getRegistrationNumber() == null ? null : new RegistrationNumber(e.getRegistrationNumber()),
				e.getNotaryOffice(),
				e.getNotarialDescription(),
				new Address(e.getAddressStreet(), e.getAddressNumber(), e.getAddressNeighborhood(),
						e.getNeighborhoodId(), e.getAddressZipCode(), e.getAddressReference()),
				e.getTotalArea(),
				e.getBuiltArea(),
				new Geolocation(e.getLatitude(), e.getLongitude()),
				e.getManagingUnitId(),
				e.getBudgetUnit(),
				e.getUsageCategory(),
				e.getCustomCategoryName(),
				e.getPossessionType(),
				contract,
				e.getAcquisitionYear(),
				e.getOriginalValue() == null ? null : MonetaryValue.of(e.getOriginalValue()),
				MonetaryValue.of(e.getAccumulatedDepreciation()),
				e.getPublicPurpose(),
				e.getStatus(),
				e.getApprovedBy(),
				e.getApprovedAt(),
				new PropertyAudit(e.getCreatedBy(), e.getCreatedAt(), e.getUpdatedAt(),
						LifecycleStatus.valueOf(e.getLifecycleStatus()), e.getVersion())));
	}

	static void copyToEntity(Property property, PropertyEntity e) {
		PropertyState s = property.state();
		e.setRegistrationNumber(s.registrationNumber() == null ? null : s.registrationNumber().value());
		e.setNotaryOffice(s.notaryOffice());
		e.setNotarialDescription(s.notarialDescription());
		e.setAddressStreet(s.address().street());
		e.setAddressNumber(s.address().number());
		e.setAddressNeighborhood(s.address().neighborhood());
		e.setNeighborhoodId(s.address().neighborhoodId());
		e.setAddressZipCode(s.address().zipCode());
		e.setAddressReference(s.address().reference());
		e.setTotalArea(s.totalArea());
		e.setBuiltArea(s.builtArea());
		e.setLatitude(s.geolocation().latitude());
		e.setLongitude(s.geolocation().longitude());
		e.setManagingUnitId(s.managingUnitId());
		e.setBudgetUnit(s.budgetUnit());
		e.setUsageCategory(s.usageCategory());
		e.setCustomCategoryName(s.customCategoryName());
		e.setPossessionType(s.possessionType());
		copyContract(s.possessionContract(), e);
		e.setAcquisitionYear(s.acquisitionYear());
		e.setOriginalValue(s.originalValue() == null ? null : s.originalValue().amount());
		e.setAccumulatedDepreciation(s.accumulatedDepreciation().amount());
		e.setPublicPurpose(s.publicPurpose());
		e.setStatus(s.status());
		e.setApprovedBy(s.approvedBy());
		e.setApprovedAt(s.approvedAt());
		e.setLifecycleStatus(s.audit().lifecycleStatus().name());
	}

	private static void copyContract(PossessionContract c, PropertyEntity e) {
		e.setContractStartDate(c == null ? null : c.startDate());
		e.setContractEndDate(c == null ? null : c.endDate());
		e.setContractMonthlyValue(c == null ? null : c.monthlyValue());
		e.setContractReferenceValue(c == null ? null : c.referenceValue());
		e.setContractGrantor(c == null ? null : c.grantor());
		e.setContractLessor(c == null ? null : c.lessor());
		e.setContractAdministrativeProcessNumber(c == null ? null : c.administrativeProcessNumber());
	}
}
