package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sdk.persistence.AuditableRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Espelha as colunas de auditoria do BCM ({@code BaseAuditableEntity} / {@code SdkAuditableEntity}).
 *
 * <p>A documentação permite estender {@code SdkAuditableEntity} OU espelhar as colunas. Como o
 * {@code bcm-dev-host} depende deste módulo, depender de volta dele criaria ciclo no Gradle; por isso
 * o módulo implementa o contrato {@link AuditableRecord} do {@code bcm-sdk-api}. Na portabilidade para o
 * monólito a Seplati pode trocar esta classe por {@code BaseAuditableEntity} sem alterar o schema.
 *
 * <p>{@code created_by}/{@code updated_by} vêm do JWT ({@code sub}) via {@link AuditorResolver}.
 */
@MappedSuperclass
public abstract class SicimAuditableEntity implements AuditableRecord {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id = UUID.randomUUID();

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "created_by", updatable = false)
	private String createdBy;

	@Column(name = "updated_by")
	private String updatedBy;

	@Column(name = "org_id")
	private UUID orgId;

	@Column(name = "source", length = 64)
	private String source;

	@Column(name = "sensitivity", length = 32, nullable = false)
	private String sensitivity = "INTERNAL";

	@Column(name = "lifecycle_status", length = 32, nullable = false)
	private String lifecycleStatus = "ACTIVE";

	@Version
	@Column(name = "version", nullable = false)
	private long version;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		createdAt = now;
		updatedAt = now;
		String auditor = AuditorResolver.currentAuditor();
		if (createdBy == null) {
			createdBy = auditor;
		}
		updatedBy = auditor;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
		updatedBy = AuditorResolver.currentAuditor();
	}

	@Override
	public UUID getId() {
		return id;
	}

	/** Usado só ao persistir um agregado novo, para manter o id gerado no domínio. */
	public void assignId(UUID id) {
		if (createdAt != null) {
			throw new IllegalStateException("Cannot reassign the id of a persisted record.");
		}
		this.id = id;
	}

	@Override
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	@Override
	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public UUID getOrgId() {
		return orgId;
	}

	public void setOrgId(UUID orgId) {
		this.orgId = orgId;
	}

	@Override
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(String sensitivity) {
		this.sensitivity = sensitivity;
	}

	@Override
	public String getLifecycleStatus() {
		return lifecycleStatus;
	}

	public void setLifecycleStatus(String lifecycleStatus) {
		this.lifecycleStatus = lifecycleStatus;
	}

	@Override
	public long getVersion() {
		return version;
	}
}
