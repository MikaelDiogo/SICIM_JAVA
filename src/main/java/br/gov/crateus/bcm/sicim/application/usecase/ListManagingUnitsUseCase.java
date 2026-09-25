package br.gov.crateus.bcm.sicim.application.usecase;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitRepository;
import br.gov.crateus.bcm.sicim.application.result.ManagingUnitResult;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListManagingUnitsUseCase {

	private final ManagingUnitRepository managingUnits;

	public ListManagingUnitsUseCase(ManagingUnitRepository managingUnits) {
		this.managingUnits = managingUnits;
	}

	@Transactional(readOnly = true)
	public List<ManagingUnitResult> execute() {
		return managingUnits.findAllActive().stream().map(ManagingUnitResult::from).toList();
	}
}
