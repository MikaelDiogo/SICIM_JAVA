package br.gov.crateus.bcm.sicim.application.port;

import java.util.UUID;

/** Consulta a existência do bairro na plataforma (domínio geography). */
public interface NeighborhoodDirectory {

	boolean exists(UUID neighborhoodId);
}
