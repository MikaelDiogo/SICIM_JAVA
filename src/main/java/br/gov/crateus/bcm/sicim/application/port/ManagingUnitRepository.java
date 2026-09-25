package br.gov.crateus.bcm.sicim.application.port;

import br.gov.crateus.bcm.sicim.domain.ManagingUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de persistência do registro local de órgãos gestores (implementada na infraestrutura). */
public interface ManagingUnitRepository {

	Optional<ManagingUnit> findById(UUID id);

	List<ManagingUnit> findAllActive();

	ManagingUnit save(ManagingUnit unit);
}
