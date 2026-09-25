package br.gov.crateus.bcm.sicim.application.usecase;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitRepository;
import br.gov.crateus.bcm.sicim.application.result.ManagingUnitResult;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetManagingUnitUseCase {

	private final ManagingUnitRepository managingUnits;

	public GetManagingUnitUseCase(ManagingUnitRepository managingUnits) {
		this.managingUnits = managingUnits;
	}

	@Transactional(readOnly = true)
	public ManagingUnitResult execute(UUID id) {
		return managingUnits.findById(id).map(ManagingUnitResult::from)
				.orElseThrow(() -> SicimDomainException.notFound("Managing unit \"" + id + "\" not found."));
	}
}
