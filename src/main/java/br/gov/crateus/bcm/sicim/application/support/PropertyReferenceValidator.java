package br.gov.crateus.bcm.sicim.application.support;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitDirectory;
import br.gov.crateus.bcm.sicim.application.port.NeighborhoodDirectory;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Valida referências de outros domínios da plataforma antes da escrita (RN17, RN18). */
@Component
public class PropertyReferenceValidator {

	private final ManagingUnitDirectory managingUnits;
	private final NeighborhoodDirectory neighborhoods;

	public PropertyReferenceValidator(ManagingUnitDirectory managingUnits, NeighborhoodDirectory neighborhoods) {
		this.managingUnits = managingUnits;
		this.neighborhoods = neighborhoods;
	}

	/** RN17: o órgão gestor precisa existir na plataforma (organization). */
	public void validateManagingUnit(UUID managingUnitId) {
		if (managingUnitId != null && !managingUnits.exists(managingUnitId)) {
			throw SicimDomainException.validation(
					"Managing unit \"" + managingUnitId + "\" does not exist.");
		}
	}

	/** RN18: o bairro, quando informado, precisa existir na plataforma (geography). */
	public void validateNeighborhood(UUID neighborhoodId) {
		if (neighborhoodId != null && !neighborhoods.exists(neighborhoodId)) {
			throw SicimDomainException.validation(
					"Neighborhood \"" + neighborhoodId + "\" does not exist.");
		}
	}
}
