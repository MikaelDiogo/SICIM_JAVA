package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Registro append-only (auditoria de produto / LGPD). Nunca atualizado nem apagado. */
@Entity
@Immutable
@Table(name = "property_history", schema = "sicim")
public class PropertyHistoryEntity extends SicimAuditableEntity {

	@Column(name = "property_id", nullable = false, updatable = false)
	private UUID propertyId;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", length = 64, nullable = false, updatable = false)
	private PropertyHistoryAction action;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "data_before", columnDefinition = "jsonb", updatable = false)
	private Map<String, Object> dataBefore;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "data_after", columnDefinition = "jsonb", updatable = false)
	private Map<String, Object> dataAfter;

	@Column(name = "correlation_id", length = 64, updatable = false)
	private String correlationId;

	protected PropertyHistoryEntity() {
	}

	public PropertyHistoryEntity(UUID propertyId, PropertyHistoryAction action,
			Map<String, Object> dataBefore, Map<String, Object> dataAfter, String correlationId) {
		this.propertyId = propertyId;
		this.action = action;
		this.dataBefore = dataBefore;
		this.dataAfter = dataAfter;
		this.correlationId = correlationId;
	}

	public UUID getPropertyId() { return propertyId; }
	public PropertyHistoryAction getAction() { return action; }
	public Map<String, Object> getDataBefore() { return dataBefore; }
	public Map<String, Object> getDataAfter() { return dataAfter; }
	public String getCorrelationId() { return correlationId; }
}
