package br.gov.crateus.bcm.sicim.domain;

/** Ciclo de vida BDM (soft-delete). DELETED é lógico — sem DELETE físico. */
public enum LifecycleStatus {
	ACTIVE,
	INACTIVE,
	ARCHIVED,
	DELETED
}
