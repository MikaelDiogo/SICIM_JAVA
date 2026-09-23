package br.gov.crateus.bcm.sicim.api;

import br.gov.crateus.bcm.sicim.application.port.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Substitui o objeto "user" que o POST /auth/login do NestJS devolvia: a SPA descobre
 * quem é o usuário e quais perfis SICIM ele tem a partir do próprio JWT do Keycloak.
 */
@RestController
@RequestMapping("/api/v1/sicim/me")
@Tag(name = "sicim")
@SecurityRequirement(name = "bearer-jwt")
public class SicimMeController {

	private final CurrentUserProvider currentUser;

	public SicimMeController(CurrentUserProvider currentUser) {
		this.currentUser = currentUser;
	}

	public record MeResponse(String id, String username, Set<String> roles) {
	}

	@GetMapping
	@Operation(summary = "Identidade e roles SICIM do usuário autenticado (derivados do JWT)")
	public MeResponse me() {
		return new MeResponse(currentUser.subject(), currentUser.username(), currentUser.sicimRoles());
	}
}
