package br.gov.crateus.bcm.sicim.application.usecase;

import br.gov.crateus.bcm.sicim.application.result.PropertyResult;
import br.gov.crateus.bcm.sicim.application.support.PropertyLookup;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetPropertyUseCase {

	private final PropertyLookup lookup;

	public GetPropertyUseCase(PropertyLookup lookup) {
		this.lookup = lookup;
	}

	@Transactional(readOnly = true)
	public PropertyResult execute(UUID id) {
		return PropertyResult.from(lookup.require(id));
	}
}
