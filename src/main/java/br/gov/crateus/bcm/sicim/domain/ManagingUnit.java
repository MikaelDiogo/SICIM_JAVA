package br.gov.crateus.bcm.sicim.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Órgão gestor do registro local mantido pelo SICIM enquanto a API de organization da
 * plataforma não existe (ver NOTA-TECNICA.md item 2). Sem transição de estado além do
 * cadastro e da desativação lógica — não precisa da ceremônia de {@code Property}/{@code
 * PropertyState}.
 */
public record ManagingUnit(UUID id, String name, String acronym, ManagingUnitType type,
		LifecycleStatus lifecycleStatus, OffsetDateTime createdAt) {

	public ManagingUnit {
		name = Text.required(name, "name");
		acronym = Text.required(acronym, "acronym");
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(lifecycleStatus, "lifecycleStatus");
	}

	/** Cadastro: gera id e nasce ativo. {@code createdAt} é preenchido pela persistência. */
	public static ManagingUnit register(String name, String acronym, ManagingUnitType type) {
		return new ManagingUnit(UUID.randomUUID(), name, acronym, type, LifecycleStatus.ACTIVE, null);
	}

	/** Desativação lógica: sem DELETE físico (regra 1.7). */
	public ManagingUnit deactivate() {
		return new ManagingUnit(id, name, acronym, type, LifecycleStatus.INACTIVE, createdAt);
	}
}
