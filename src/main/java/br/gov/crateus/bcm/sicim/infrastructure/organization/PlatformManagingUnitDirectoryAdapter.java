package br.gov.crateus.bcm.sicim.infrastructure.organization;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitDirectory;
import java.util.UUID;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Consulta o órgão gestor na API de organization real da plataforma. Só é usado quando
 * {@code sicim.integration.organization.base-url} está configurada — ver
 * {@link ManagingUnitDirectoryConfig} (que decide entre esta implementação e
 * {@link LocalManagingUnitDirectoryAdapter}).
 */
public class PlatformManagingUnitDirectoryAdapter implements ManagingUnitDirectory {

	private final RestClient restClient;

	public PlatformManagingUnitDirectoryAdapter(String baseUrl) {
		this.restClient = RestClient.create(baseUrl);
	}

	@Override
	public boolean exists(UUID managingUnitId) {
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
