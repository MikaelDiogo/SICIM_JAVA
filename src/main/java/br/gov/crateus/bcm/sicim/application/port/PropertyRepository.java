package br.gov.crateus.bcm.sicim.application.port;

import br.gov.crateus.bcm.sicim.application.command.PropertyFilter;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.domain.Property;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de persistência do agregado (implementada na infraestrutura). */
public interface PropertyRepository {

	Optional<Property> findById(UUID id);

	boolean existsByRegistrationNumber(String registrationNumber);

	Property save(Property property);

	PageResult<Property> findPage(PropertyFilter filter);

	List<String> findCustomCategoryNames();
}
