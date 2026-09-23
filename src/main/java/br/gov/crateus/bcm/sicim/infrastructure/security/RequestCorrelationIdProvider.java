package br.gov.crateus.bcm.sicim.infrastructure.security;

import br.gov.crateus.bcm.sicim.application.port.CorrelationIdProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Lê o X-Correlation-Id do request (o BCM propaga/gera e coloca no MDC). Fallback: MDC.
 * No Dev Host não há filtro de correlação; o valor vem só do header enviado pela SPA.
 */
@Component
public class RequestCorrelationIdProvider implements CorrelationIdProvider {

	public static final String HEADER = "X-Correlation-Id";
	private static final int MAX_LENGTH = 64;

	@Override
	public String currentCorrelationId() {
		String value = null;
		if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
			HttpServletRequest request = attrs.getRequest();
			value = request.getHeader(HEADER);
		}
		if (value == null || value.isBlank()) {
			value = MDC.get("correlationId");
		}
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.length() > MAX_LENGTH ? value.substring(0, MAX_LENGTH) : value;
	}
}
