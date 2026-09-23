package br.gov.crateus.bcm.sicim.domain;

import java.time.OffsetDateTime;

/** Metadados de auditoria BDM que o domínio precisa conhecer (preenchidos pela infraestrutura). */
public record PropertyAudit(String createdBy, OffsetDateTime createdAt, OffsetDateTime updatedAt,
		LifecycleStatus lifecycleStatus, long version) {

	public static PropertyAudit forNewRecord() {
		return new PropertyAudit(null, null, null, LifecycleStatus.ACTIVE, 0L);
	}

	PropertyAudit withLifecycle(LifecycleStatus status) {
		return new PropertyAudit(createdBy, createdAt, updatedAt, status, version);
	}
}
