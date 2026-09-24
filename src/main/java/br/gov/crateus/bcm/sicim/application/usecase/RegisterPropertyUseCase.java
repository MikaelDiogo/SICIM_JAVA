package br.gov.crateus.bcm.sicim.application.usecase;

import br.gov.crateus.bcm.sicim.application.command.RegisterPropertyCommand;
import br.gov.crateus.bcm.sicim.application.port.PropertyEventPublisher.PropertyEvent;
import br.gov.crateus.bcm.sicim.application.port.PropertyRepository;
import br.gov.crateus.bcm.sicim.application.port.TimeProvider;
import br.gov.crateus.bcm.sicim.application.result.PropertyResult;
import br.gov.crateus.bcm.sicim.application.support.PropertyChangeRecorder;
import br.gov.crateus.bcm.sicim.application.support.PropertyCommandMapper;
import br.gov.crateus.bcm.sicim.application.support.PropertyReferenceValidator;
import br.gov.crateus.bcm.sicim.domain.NewProperty;
import br.gov.crateus.bcm.sicim.domain.Property;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPropertyUseCase {

	private final PropertyRepository properties;
	private final PropertyChangeRecorder recorder;
	private final PropertyReferenceValidator references;
	private final TimeProvider time;

	public RegisterPropertyUseCase(PropertyRepository properties, PropertyChangeRecorder recorder,
			PropertyReferenceValidator references, TimeProvider time) {
		this.properties = properties;
		this.recorder = recorder;
		this.references = references;
		this.time = time;
	}

	@Transactional
	public PropertyResult execute(RegisterPropertyCommand command) {
		NewProperty data = PropertyCommandMapper.toNewProperty(command);
		references.validateManagingUnit(data.managingUnitId());
		references.validateNeighborhood(data.address().neighborhoodId());
		String registration = data.registrationNumber().value();
		if (properties.existsByRegistrationNumber(registration)) {
			throw SicimDomainException.conflict(
					"A property with registration number \"" + registration + "\" already exists.");
		}
		Property saved = properties.save(Property.register(data, time.currentYear()));
		recorder.record(saved, PropertyHistoryAction.CREATE, null, PropertyEvent.PROPERTY_REGISTERED);
		return PropertyResult.from(saved);
	}
}
