package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sicim.domain.ManagingUnitType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "managing_units", schema = "sicim")
public class ManagingUnitEntity extends SicimAuditableEntity {

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "acronym", length = 20, nullable = false)
	private String acronym;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", length = 32, nullable = false)
	private ManagingUnitType type;

	public String getName() { return name; }
	public void setName(String v) { this.name = v; }
	public String getAcronym() { return acronym; }
	public void setAcronym(String v) { this.acronym = v; }
	public ManagingUnitType getType() { return type; }
	public void setType(ManagingUnitType v) { this.type = v; }
}
