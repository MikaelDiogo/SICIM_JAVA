package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/** Resolve o autor da operação a partir do JWT (claim {@code sub}). */
final class AuditorResolver {

	static final String SYSTEM = "system";

	private AuditorResolver() {
	}

	static String currentAuditor() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return SYSTEM;
		}
		if (auth.getPrincipal() instanceof Jwt jwt && jwt.getSubject() != null) {
			return jwt.getSubject();
		}
		return auth.getName() != null ? auth.getName() : SYSTEM;
	}
}
