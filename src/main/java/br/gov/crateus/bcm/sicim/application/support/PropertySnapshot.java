package br.gov.crateus.bcm.sicim.application.support;

import br.gov.crateus.bcm.sicim.domain.PossessionContract;
import br.gov.crateus.bcm.sicim.domain.Property;
import br.gov.crateus.bcm.sicim.domain.PropertyState;
import java.util.LinkedHashMap;
import java.util.Map;

/** Snapshot plano e serializável (JSONB) do imóvel para o histórico. */
public final class PropertySnapshot {

	private PropertySnapshot() {
	}

	public static Map<String, Object> of(Property property) {
		PropertyState s = property.state();
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", text(s.id()));
		m.put("registrationNumber", s.registrationNumber().value());
		m.put("notaryOffice", s.notaryOffice());
		m.put("notarialDescription", s.notarialDescription());
		m.put("address", address(s));
		m.put("totalArea", text(s.totalArea()));
		m.put("builtArea", text(s.builtArea()));
		m.put("latitude", text(s.geolocation().latitude()));
		m.put("longitude", text(s.geolocation().longitude()));
		m.put("managingUnitId", text(s.managingUnitId()));
		m.put("budgetUnit", s.budgetUnit());
		m.put("usageCategory", text(s.usageCategory()));
		m.put("customCategoryName", s.customCategoryName());
		m.put("possessionType", text(s.possessionType()));
		m.put("possessionContract", contract(s.possessionContract()));
		m.put("acquisitionYear", s.acquisitionYear());
		m.put("originalValue", text(s.originalValue().amount()));
		m.put("accumulatedDepreciation", text(s.accumulatedDepreciation().amount()));
		m.put("publicPurpose", s.publicPurpose());
		m.put("status", text(s.status()));
		m.put("lifecycleStatus", text(s.audit().lifecycleStatus()));
		return m;
	}

	private static Map<String, Object> address(PropertyState s) {
		Map<String, Object> a = new LinkedHashMap<>();
		a.put("street", s.address().street());
		a.put("number", s.address().number());
		a.put("neighborhood", s.address().neighborhood());
		a.put("neighborhoodId", text(s.address().neighborhoodId()));
		a.put("zipCode", s.address().zipCode());
		a.put("reference", s.address().reference());
		return a;
	}

	private static Map<String, Object> contract(PossessionContract c) {
		if (c == null) {
			return null;
		}
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("startDate", text(c.startDate()));
		m.put("endDate", text(c.endDate()));
		m.put("monthlyValue", text(c.monthlyValue()));
		m.put("referenceValue", text(c.referenceValue()));
		m.put("administrativeProcessNumber", c.administrativeProcessNumber());
		return m;
	}

	private static String text(Object value) {
		return value == null ? null : value.toString();
	}
}
