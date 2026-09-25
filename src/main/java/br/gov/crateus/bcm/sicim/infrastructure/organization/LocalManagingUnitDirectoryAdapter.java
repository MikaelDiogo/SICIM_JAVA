package br.gov.crateus.bcm.sicim.infrastructure.organization;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitDirectory;
import br.gov.crateus.bcm.sicim.application.port.ManagingUnitRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enquanto {@code sicim.integration.organization.base-url} não estiver configurada (Dev Host
 * hoje), RN17 é validada contra o registro local de órgãos gestores mantido pelo próprio SICIM
 * (ver {@code ManagingUnitController}, exceção documentada à regra 1.6 de REGRAS.md e
 * NOTA-TECNICA.md item 2) — em vez de aceitar qualquer UUID bem formado. Escolhida por
 * {@link ManagingUnitDirectoryConfig}.
 */
public class LocalManagingUnitDirectoryAdapter implements ManagingUnitDirectory {

	private static final Logger LOG = LoggerFactory.getLogger(LocalManagingUnitDirectoryAdapter.class);

	private final ManagingUnitRepository managingUnits;

	public LocalManagingUnitDirectoryAdapter(ManagingUnitRepository managingUnits) {
		this.managingUnits = managingUnits;
		LOG.info("sicim.integration.organization.base-url not configured: validating managingUnitId "
				+ "against the local SICIM registry (/api/v1/sicim/managing-units) until the platform "
				+ "organization API is available.");
	}

	@Override
	public boolean exists(UUID managingUnitId) {
		return managingUnits.findById(managingUnitId).isPresent();
	}
}
