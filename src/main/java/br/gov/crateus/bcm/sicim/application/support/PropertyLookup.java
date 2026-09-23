package br.gov.crateus.bcm.sicim.application.support;

import br.gov.crateus.bcm.sicim.application.port.PropertyRepository;
import br.gov.crateus.bcm.sicim.domain.Property;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Carrega um imóvel existente (não excluído logicamente) ou lança 404. */
@Component
public class PropertyLookup {

	private final PropertyRepository properties;

	public PropertyLookup(PropertyRepository properties) {
		this.properties = properties;
	}

	public Property require(UUID id) {
		return properties.findById(id)
				.filter(p -> !p.isDeleted())
				.orElseThrow(() -> SicimDomainException.notFound("No property found with id \"" + id + "\"."));
	}
}
