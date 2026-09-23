package br.gov.crateus.bcm.sicim.infrastructure.security;

import br.gov.crateus.bcm.sicim.application.port.CurrentUserProvider;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

	private static final String ROLE_PREFIX = "ROLE_SICIM_";

	@Override
	public String subject() {
		Authentication auth = authentication();
		if (auth == null) {
			return "system";
		}
		if (auth.getPrincipal() instanceof Jwt jwt && jwt.getSubject() != null) {
			return jwt.getSubject();
		}
		return auth.getName();
	}

	@Override
	public String username() {
		Authentication auth = authentication();
		if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
			String preferred = jwt.getClaimAsString("preferred_username");
			return preferred != null ? preferred : jwt.getSubject();
		}
		return auth == null ? null : auth.getName();
	}

	@Override
	public Set<String> sicimRoles() {
		Authentication auth = authentication();
		if (auth == null) {
			return Set.of();
		}
		return auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.filter(a -> a.startsWith(ROLE_PREFIX))
				.map(a -> a.substring("ROLE_".length()))
				.collect(Collectors.toUnmodifiableSet());
	}

	private static Authentication authentication() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.isAuthenticated() ? auth : null;
	}
}
