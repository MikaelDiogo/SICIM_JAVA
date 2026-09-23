package br.gov.crateus.bcm.sicim.application.port;

import java.util.Set;

/** Identidade do usuário vinda do JWT (o módulo não autentica usuário/senha). */
public interface CurrentUserProvider {

	/** Claim {@code sub}. */
	String subject();

	/** Claim {@code preferred_username}, quando existir. */
	String username();

	/** Realm roles do domínio SICIM presentes no token. */
	Set<String> sicimRoles();
}
