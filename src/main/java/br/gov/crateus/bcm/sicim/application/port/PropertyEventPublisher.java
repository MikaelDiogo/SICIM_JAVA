package br.gov.crateus.bcm.sicim.application.port;

import br.gov.crateus.bcm.sicim.domain.Property;

/** Publica eventos de domínio do imóvel. Deve participar da transação do chamador (outbox). */
public interface PropertyEventPublisher {

	void publish(PropertyEvent event, Property property);

	enum PropertyEvent {
		PROPERTY_REGISTERED("PropertyRegistered"),
		PROPERTY_UPDATED("PropertyUpdated"),
		PROPERTY_APPROVED("PropertyApproved"),
		PROPERTY_DEACTIVATED("PropertyDeactivated"),
		PROPERTY_DEPRECIATION_RECALCULATED("PropertyDepreciationRecalculated");

		private final String eventType;

		PropertyEvent(String eventType) {
			this.eventType = eventType;
		}

		public String eventType() {
			return eventType;
		}
	}
}
