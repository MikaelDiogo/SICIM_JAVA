package br.gov.crateus.bcm.sicim.infrastructure.event;

import br.gov.crateus.bcm.sdk.outbox.OutboxRecorder;
import br.gov.crateus.bcm.sicim.application.port.PropertyEventPublisher;
import br.gov.crateus.bcm.sicim.domain.Property;
import br.gov.crateus.bcm.sicim.domain.PropertyState;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Publica via {@link OutboxRecorder} do SDK (propagation MANDATORY no host): o evento só existe se a
 * transação de negócio for confirmada. Routing key resultante: {@code Property.<eventType>}.
 */
@Component
class OutboxPropertyEventPublisher implements PropertyEventPublisher {

	static final String AGGREGATE_TYPE = "Property";

	private final OutboxRecorder outbox;

	OutboxPropertyEventPublisher(OutboxRecorder outbox) {
		this.outbox = outbox;
	}

	@Override
	public void publish(PropertyEvent event, Property property) {
		PropertyState s = property.state();
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("propertyId", s.id().toString());
		payload.put("registrationNumber", s.registrationNumber() == null ? null : s.registrationNumber().value());
		payload.put("managingUnitId", s.managingUnitId().toString());
		payload.put("status", s.status().name());
		outbox.record(AGGREGATE_TYPE, s.id().toString(), event.eventType(), payload);
	}
}
