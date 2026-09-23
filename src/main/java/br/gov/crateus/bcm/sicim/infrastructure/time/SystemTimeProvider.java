package br.gov.crateus.bcm.sicim.infrastructure.time;

import br.gov.crateus.bcm.sicim.application.port.TimeProvider;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
class SystemTimeProvider implements TimeProvider {

	@Override
	public OffsetDateTime now() {
		return OffsetDateTime.now(ZoneOffset.UTC);
	}
}
