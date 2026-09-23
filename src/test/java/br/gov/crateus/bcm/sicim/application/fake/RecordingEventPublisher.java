package br.gov.crateus.bcm.sicim.application.fake;

import br.gov.crateus.bcm.sicim.application.port.PropertyEventPublisher;
import br.gov.crateus.bcm.sicim.domain.Property;
import java.util.ArrayList;
import java.util.List;

public class RecordingEventPublisher implements PropertyEventPublisher {

	public final List<PropertyEvent> events = new ArrayList<>();

	@Override
	public void publish(PropertyEvent event, Property property) {
		events.add(event);
	}
}
