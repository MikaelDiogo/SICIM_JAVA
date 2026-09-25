package br.gov.crateus.bcm.sicim.application.usecase;

import br.gov.crateus.bcm.sicim.application.command.UpdatePropertyCommand;
import br.gov.crateus.bcm.sicim.application.port.PropertyEventPublisher.PropertyEvent;
import br.gov.crateus.bcm.sicim.application.port.PropertyRepository;
import br.gov.crateus.bcm.sicim.application.port.TimeProvider;
import br.gov.crateus.bcm.sicim.application.result.PropertyResult;
import br.gov.crateus.bcm.sicim.application.support.PropertyChangeRecorder;
import br.gov.crateus.bcm.sicim.application.support.PropertyCommandMapper;
import br.gov.crateus.bcm.sicim.application.support.PropertyLookup;
import br.gov.crateus.bcm.sicim.application.support.PropertyReferenceValidator;
import br.gov.crateus.bcm.sicim.application.support.PropertySnapshot;
import br.gov.crateus.bcm.sicim.domain.Property;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import br.gov.crateus.bcm.sicim.domain.RegistrationNumber;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdatePropertyUseCase {

	private final PropertyLookup lookup;
	private final PropertyRepository properties;
	private final PropertyChangeRecorder recorder;
	private final PropertyReferenceValidator references;
	private final TimeProvider time;

	public UpdatePropertyUseCase(PropertyLookup lookup, PropertyRepository properties,
			PropertyChangeRecorder recorder, PropertyReferenceValidator references, TimeProvider time) {
		this.lookup = lookup;
		this.properties = properties;
		this.recorder = recorder;
		this.references = references;
		this.time = time;
	}

	@Transactional
	public PropertyResult execute(UUID id, UpdatePropertyCommand command) {
		Property property = lookup.require(id);
		Map<String, Object> before = PropertySnapshot.of(property);
		references.validateManagingUnit(command.managingUnitId());
		if (command.address() != null) {
			references.validateNeighborhood(command.address().neighborhoodId());
		}
		RegistrationNumber requested = RegistrationNumber.of(command.registrationNumber());
		if (requested != null && !requested.equals(property.state().registrationNumber())
				&& properties.existsByRegistrationNumber(requested.value())) {
			throw SicimDomainException.conflict(
					"A property with registration number \"" + requested.value() + "\" already exists.");
		}
		property.update(PropertyCommandMapper.toChanges(command), time.currentYear());
		Property saved = properties.save(property);
		recorder.record(saved, PropertyHistoryAction.UPDATE, before, PropertyEvent.PROPERTY_UPDATED);
		return PropertyResult.from(saved);
	}
}
