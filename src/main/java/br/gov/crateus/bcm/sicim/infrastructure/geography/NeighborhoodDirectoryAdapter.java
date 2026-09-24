package br.gov.crateus.bcm.sicim.infrastructure.geography;

import br.gov.crateus.bcm.sicim.application.port.NeighborhoodDirectory;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Consulta o bairro na API de geography da plataforma. Enquanto essa API não estiver disponível
 * no Dev Host ({@code sicim.integration.geography.base-url} vazio), é permissiva — ver
 * NOTA-TECNICA.md.
 */
@Component
public class NeighborhoodDirectoryAdapter implements NeighborhoodDirectory {

	private static final Logger LOG = LoggerFactory.getLogger(NeighborhoodDirectoryAdapter.class);

	private final RestClient restClient;
	private final boolean integrationConfigured;

	public NeighborhoodDirectoryAdapter(
			@Value("${sicim.integration.geography.base-url:}") String baseUrl) {
		this.integrationConfigured = !baseUrl.isBlank();
		this.restClient = integrationConfigured ? RestClient.create(baseUrl) : null;
		if (!integrationConfigured) {
			LOG.warn("sicim.integration.geography.base-url not configured: neighborhood existence "
					+ "is not verified against the platform (format-only validation).");
		}
	}

	@Override
	public boolean exists(UUID neighborhoodId) {
		if (!integrationConfigured) {
			return true;
		}
		try {
			restClient.get().uri("/neighborhoods/{id}", neighborhoodId).retrieve().toBodilessEntity();
			return true;
		} catch (RestClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				return false;
			}
			throw e;
		}
	}
}
