package br.gov.crateus.bcm.sicim.infrastructure.organization;

import br.gov.crateus.bcm.sicim.application.port.ManagingUnitDirectory;
import br.gov.crateus.bcm.sicim.application.port.ManagingUnitRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Decide qual {@link ManagingUnitDirectory} usar. {@code @ConditionalOnMissingBean} só é
 * confiável dentro de métodos {@code @Bean} de uma mesma {@code @Configuration} (ordem de
 * declaração garantida) — não entre classes {@code @Component} soltas, que têm ordem de scan
 * não determinística.
 */
@Configuration
public class ManagingUnitDirectoryConfig {

	@Bean
	@ConditionalOnProperty(prefix = "sicim.integration.organization", name = "base-url")
	public ManagingUnitDirectory platformManagingUnitDirectory(
			@Value("${sicim.integration.organization.base-url}") String baseUrl) {
		return new PlatformManagingUnitDirectoryAdapter(baseUrl);
	}

	@Bean
	@ConditionalOnMissingBean(ManagingUnitDirectory.class)
	public ManagingUnitDirectory localManagingUnitDirectory(ManagingUnitRepository managingUnits) {
		return new LocalManagingUnitDirectoryAdapter(managingUnits);
	}
}
