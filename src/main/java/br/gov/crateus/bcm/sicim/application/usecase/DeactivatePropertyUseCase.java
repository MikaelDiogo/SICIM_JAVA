package br.gov.crateus.bcm.sicim.application.usecase;

import br.gov.crateus.bcm.sicim.application.port.PropertyEventPublisher.PropertyEvent;
import br.gov.crateus.bcm.sicim.application.port.PropertyRepository;
import br.gov.crateus.bcm.sicim.application.result.PropertyResult;
import br.gov.crateus.bcm.sicim.application.support.PropertyChangeRecorder;
import br.gov.crateus.bcm.sicim.application.support.PropertyLookup;
import br.gov.crateus.bcm.sicim.application.support.PropertySnapshot;
import br.gov.crateus.bcm.sicim.domain.Property;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Soft-delete: nenhum DELETE físico. */
@Service
public class DeactivatePropertyUseCase {

	private final PropertyLookup lookup;
	private final PropertyRepository properties;
	private final PropertyChangeRecorder recorder;

	public DeactivatePropertyUseCase(PropertyLookup lookup, PropertyRepository properties,
			PropertyChangeRecorder recorder) {
		this.lookup = lookup;
		this.properties = properties;
		this.recorder = recorder;
	}

	@Transactional
	public PropertyResult execute(UUID id) {
		Property property = lookup.require(id);
		Map<String, Object> before = PropertySnapshot.of(property);
		property.deactivate();
		Property saved = properties.save(property);
		recorder.record(saved, PropertyHistoryAction.DEACTIVATE, before, PropertyEvent.PROPERTY_DEACTIVATED);
		return PropertyResult.from(saved);
	}
}
