package br.gov.crateus.bcm.sicim.application;

import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyEntity;
import java.util.LinkedHashMap;
import java.util.Map;

/** Snapshot plano e serializável do imóvel para o histórico (equivalente a toAuditSnapshot). */
final class PropertySnapshot {

	private PropertySnapshot() {
	}

	static Map<String, Object> of(PropertyEntity p) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", str(p.getId()));
		m.put("registrationNumber", p.getRegistrationNumber());
		m.put("notaryOffice", p.getNotaryOffice());
		m.put("notarialDescription", p.getNotarialDescription());
		Map<String, Object> address = new LinkedHashMap<>();
		address.put("street", p.getAddressStreet());
		address.put("number", p.getAddressNumber());
		address.put("neighborhood", p.getAddressNeighborhood());
		address.put("neighborhoodId", str(p.getNeighborhoodId()));
		address.put("zipCode", p.getAddressZipCode());
		address.put("reference", p.getAddressReference());
		m.put("address", address);
		m.put("totalArea", str(p.getTotalArea()));
		m.put("builtArea", str(p.getBuiltArea()));
		m.put("latitude", str(p.getLatitude()));
		m.put("longitude", str(p.getLongitude()));
		m.put("managingUnitId", str(p.getManagingUnitId()));
		m.put("budgetUnit", p.getBudgetUnit());
		m.put("usageCategory", str(p.getUsageCategory()));
		m.put("customCategoryName", p.getCustomCategoryName());
		m.put("possessionType", str(p.getPossessionType()));
		m.put("contractStartDate", str(p.getContractStartDate()));
		m.put("contractEndDate", str(p.getContractEndDate()));
		m.put("contractAdministrativeProcessNumber", p.getContractAdministrativeProcessNumber());
		m.put("acquisitionYear", p.getAcquisitionYear());
		m.put("originalValue", str(p.getOriginalValue()));
		m.put("accumulatedDepreciation", str(p.getAccumulatedDepreciation()));
		m.put("publicPurpose", p.getPublicPurpose());
		m.put("status", str(p.getStatus()));
		m.put("lifecycleStatus", p.getLifecycleStatus());
		return m;
	}

	private static String str(Object o) {
		return o == null ? null : o.toString();
	}
}
