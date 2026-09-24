package br.gov.crateus.bcm.sicim.application.port;

import java.util.UUID;

/** Consulta a existência do órgão gestor na plataforma (domínio organization). */
public interface ManagingUnitDirectory {

	boolean exists(UUID managingUnitId);
}
