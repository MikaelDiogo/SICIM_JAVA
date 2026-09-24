package br.gov.crateus.bcm.sicim.infrastructure.organization;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitDirectory;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Consulta o órgão gestor na API de organization da plataforma. Enquanto essa API não estiver
 * disponível no Dev Host ({@code sicim.integration.organization.base-url} vazio), é permissiva
 * (apenas o formato UUID, já validado no DTO, é exigido) — ver NOTA-TECNICA.md.
 */
@Component
public class ManagingUnitDirectoryAdapter implements ManagingUnitDirectory {

	private static final Logger LOG = LoggerFactory.getLogger(ManagingUnitDirectoryAdapter.class);

	private final RestClient restClient;
	private final boolean integrationConfigured;

	public ManagingUnitDirectoryAdapter(
			@Value("${sicim.integration.organization.base-url:}") String baseUrl) {
		this.integrationConfigured = !baseUrl.isBlank();
		this.restClient = integrationConfigured ? RestClient.create(baseUrl) : null;
		if (!integrationConfigured) {
			LOG.warn("sicim.integration.organization.base-url not configured: managing unit existence "
					+ "is not verified against the platform (format-only validation).");
		}
	}

	@Override
	public boolean exists(UUID managingUnitId) {
		if (!integrationConfigured) {
			return true;
		}
		try {
			restClient.get().uri("/managing-units/{id}", managingUnitId).retrieve().toBodilessEntity();
			return true;
		} catch (RestClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				return false;
			}
			throw e;
		}
	}
}
