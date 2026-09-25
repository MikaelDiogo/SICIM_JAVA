package br.gov.crateus.bcm.sicim.application.usecase;

import br.gov.crateus.bcm.sicim.application.command.RegisterManagingUnitCommand;
import br.gov.crateus.bcm.sicim.application.port.ManagingUnitRepository;
import br.gov.crateus.bcm.sicim.application.result.ManagingUnitResult;
import br.gov.crateus.bcm.sicim.domain.ManagingUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterManagingUnitUseCase {

	private final ManagingUnitRepository managingUnits;

	public RegisterManagingUnitUseCase(ManagingUnitRepository managingUnits) {
		this.managingUnits = managingUnits;
	}

	@Transactional
	public ManagingUnitResult execute(RegisterManagingUnitCommand command) {
		ManagingUnit saved = managingUnits
				.save(ManagingUnit.register(command.name(), command.acronym(), command.type()));
		return ManagingUnitResult.from(saved);
	}
}
