package br.gov.crateus.bcm.sicim.application.usecase;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitRepository;
import br.gov.crateus.bcm.sicim.application.result.ManagingUnitResult;
import br.gov.crateus.bcm.sicim.domain.ManagingUnit;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateManagingUnitUseCase {

	private final ManagingUnitRepository managingUnits;

	public DeactivateManagingUnitUseCase(ManagingUnitRepository managingUnits) {
		this.managingUnits = managingUnits;
	}

	@Transactional
	public ManagingUnitResult execute(UUID id) {
		ManagingUnit unit = managingUnits.findById(id)
				.orElseThrow(() -> SicimDomainException.notFound("Managing unit \"" + id + "\" not found."));
		return ManagingUnitResult.from(managingUnits.save(unit.deactivate()));
	}
}
