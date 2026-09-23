package br.gov.crateus.bcm.sicim.application;

/**
 * Realm roles do Keycloak para o domínio SICIM (convertidas pelo host em {@code ROLE_*}).
 * Mapeamento a partir dos perfis do NestJS: ADMINISTRATION → SICIM_ADMIN, APPROVAL → SICIM_APPROVER,
 * REGISTRATION → SICIM_REGISTRAR, VIEWER → SICIM_VIEWER.
 */
public final class SicimRoles {

	public static final String ADMIN = "SICIM_ADMIN";
	public static final String APPROVER = "SICIM_APPROVER";
	public static final String REGISTRAR = "SICIM_REGISTRAR";
	public static final String VIEWER = "SICIM_VIEWER";

	/** Expressões SpEL para {@code @PreAuthorize}. */
	public static final String CAN_READ =
			"hasAnyRole('SICIM_ADMIN','SICIM_APPROVER','SICIM_REGISTRAR','SICIM_VIEWER')";
	public static final String CAN_REGISTER = "hasAnyRole('SICIM_ADMIN','SICIM_REGISTRAR')";
	public static final String CAN_APPROVE = "hasAnyRole('SICIM_ADMIN','SICIM_APPROVER')";
	public static final String ADMIN_ONLY = "hasRole('SICIM_ADMIN')";

	private SicimRoles() {
	}
}
