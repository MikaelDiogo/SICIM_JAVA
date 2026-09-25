package br.gov.crateus.bcm.sicim.domain;

import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Imóvel Municipal. Toda mudança de estado passa por um método de negócio que revalida as
 * invariantes; nada de setters públicos. Sem dependência de Spring/JPA.
 */
public final class Property {

	private PropertyState state;

	private Property(PropertyState state) {
		this.state = state;
	}

	/** Cadastro: gera id, status PENDING_APPROVAL e depreciação zero. */
	public static Property register(NewProperty data, int currentYear) {
		Objects.requireNonNull(data, "data");
		// Só descrição, endereço (CEP), localização e órgão gestor são obrigatórios no cadastro
		// (ver REGRAS.md) — o resto pode ser completado depois, inclusive após a aprovação.
		PropertyState state = new PropertyState(
				UUID.randomUUID(),
				data.registrationNumber(),
				Text.optional(data.notaryOffice()),
				Text.required(data.notarialDescription(), "notarialDescription"),
				require(data.address(), "address"),
				data.totalArea(),
				data.builtArea(),
				require(data.geolocation(), "geolocation"),
				require(data.managingUnitId(), "managingUnitId"),
				Text.optional(data.budgetUnit()),
				data.usageCategory(),
				PropertyRules.normalizeCustomCategory(data.usageCategory(), data.customCategoryName()),
				data.possessionType(),
				effectiveContract(data.possessionType(), data.possessionContract()),
				data.acquisitionYear(),
				data.originalValue(),
				MonetaryValue.ZERO,
				Text.optional(data.publicPurpose()),
				PropertyStatus.PENDING_APPROVAL,
				null,
				null,
				PropertyAudit.forNewRecord());
		validate(state, data.possessionContract(), currentYear);
		return new Property(state);
	}

	/** Reconstitui a partir da persistência (não reaplica invariantes de criação). */
	public static Property reconstitute(PropertyState state) {
		return new Property(Objects.requireNonNull(state, "state"));
	}

	public void update(PropertyChanges c, int currentYear) {
		PropertyRules.ensureCanChange(state.status());
		PropertyState s = state;

		Geolocation geolocation = c.latitude() == null && c.longitude() == null
				? s.geolocation()
				: new Geolocation(orElse(c.latitude(), s.geolocation().latitude()),
						orElse(c.longitude(), s.geolocation().longitude()));
		UsageCategory category = orElse(c.usageCategory(), s.usageCategory());
		String customCategory = PropertyRules.normalizeCustomCategory(category,
				orElse(c.customCategoryName(), s.customCategoryName()));
		PossessionType possessionType = orElse(c.possessionType(), s.possessionType());
		PossessionContract requestedContract = orElse(c.possessionContract(), s.possessionContract());

		PropertyState next = new PropertyState(
				s.id(),
				orElse(c.registrationNumber(), s.registrationNumber()),
				c.notaryOffice() == null ? s.notaryOffice() : Text.optional(c.notaryOffice()),
				c.notarialDescription() == null ? s.notarialDescription()
						: Text.required(c.notarialDescription(), "notarialDescription"),
				orElse(c.address(), s.address()),
				orElse(c.totalArea(), s.totalArea()),
				orElse(c.builtArea(), s.builtArea()),
				geolocation,
				orElse(c.managingUnitId(), s.managingUnitId()),
				c.budgetUnit() == null ? s.budgetUnit() : Text.optional(c.budgetUnit()),
				category,
				customCategory,
				possessionType,
				effectiveContract(possessionType, requestedContract),
				orElse(c.acquisitionYear(), s.acquisitionYear()),
				orElse(c.originalValue(), s.originalValue()),
				s.accumulatedDepreciation(),
				c.publicPurpose() == null ? s.publicPurpose() : Text.optional(c.publicPurpose()),
				s.status(),
				s.approvedBy(),
				s.approvedAt(),
				s.audit());
		validate(next, requestedContract, currentYear);
		state = next;
	}

	public void approve(String approvedBy, OffsetDateTime approvedAt) {
		PropertyRules.ensureCanApprove(state.status());
		state = copy(PropertyStatus.APPROVED, Text.required(approvedBy, "approvedBy"),
				Objects.requireNonNull(approvedAt, "approvedAt"), state.accumulatedDepreciation(), state.audit());
	}

	/** Desativação lógica: status e lifecycle INACTIVE. */
	public void deactivate() {
		state = copy(PropertyStatus.INACTIVE, state.approvedBy(), state.approvedAt(),
				state.accumulatedDepreciation(), state.audit().withLifecycle(LifecycleStatus.INACTIVE));
	}

	public void recalculateDepreciation(int currentYear) {
		if (state.originalValue() == null || state.usageCategory() == null || state.acquisitionYear() == null) {
			throw SicimDomainException.validation("Cannot recalculate depreciation: acquisitionYear, originalValue "
					+ "and usageCategory must be filled in first.");
		}
		MonetaryValue depreciation = DepreciationCalculator.calculate(
				state.originalValue(), state.usageCategory(), state.acquisitionYear(), currentYear);
		state = copy(state.status(), state.approvedBy(), state.approvedAt(), depreciation, state.audit());
	}

	/** {@code null} enquanto originalValue não é preenchido (campo opcional — ver REGRAS.md). */
	public MonetaryValue netBookValue() {
		return state.originalValue() == null ? null : state.originalValue().subtract(state.accumulatedDepreciation());
	}

	public boolean isDeleted() {
		return state.audit().lifecycleStatus() == LifecycleStatus.DELETED;
	}

	public UUID id() {
		return state.id();
	}

	public PropertyState state() {
		return state;
	}

	// ---------------------------------------------------------------------------------------------

	private static void validate(PropertyState s, PossessionContract requestedContract, int currentYear) {
		PropertyRules.validateAreas(s.totalArea(), s.builtArea());
		PropertyRules.validatePossession(s.possessionType(), requestedContract);
		PropertyRules.validateAcquisitionYear(s.acquisitionYear(), currentYear);
		PropertyRules.validateOriginalValue(s.originalValue());
	}

	private static PossessionContract effectiveContract(PossessionType type, PossessionContract contract) {
		return type != null && type.requiresContract() ? contract : null;
	}

	private PropertyState copy(PropertyStatus status, String approvedBy, OffsetDateTime approvedAt,
			MonetaryValue depreciation, PropertyAudit audit) {
		PropertyState s = state;
		return new PropertyState(s.id(), s.registrationNumber(), s.notaryOffice(), s.notarialDescription(),
				s.address(), s.totalArea(), s.builtArea(), s.geolocation(), s.managingUnitId(), s.budgetUnit(),
				s.usageCategory(), s.customCategoryName(), s.possessionType(), s.possessionContract(),
				s.acquisitionYear(), s.originalValue(), depreciation, s.publicPurpose(), status, approvedBy,
				approvedAt, audit);
	}

	private static <T> T require(T value, String field) {
		if (value == null) {
			throw SicimDomainException.validation(field + " is required.");
		}
		return value;
	}

	private static <T> T orElse(T value, T fallback) {
		return value != null ? value : fallback;
	}
}
