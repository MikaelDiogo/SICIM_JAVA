package br.gov.crateus.bcm.sicim.domain;

import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.math.BigDecimal;

/** Invariantes do imóvel (portadas de Property.validateInvariants do NestJS). */
public final class PropertyRules {

	public static final int MIN_ACQUISITION_YEAR = 1800;

	private PropertyRules() {
	}

	// Total/construída, posse, ano e valor original são opcionais (ver REGRAS.md) — só validam
	// quando informados. Objeto incompleto não é erro; regra de formato/consistência, sim.
	public static void validateAreas(BigDecimal totalArea, BigDecimal builtArea) {
		if (totalArea == null || builtArea == null) {
			return;
		}
		if (totalArea.signum() <= 0 || builtArea.signum() <= 0) {
			throw SicimDomainException.validation("Total area and built area must be positive.");
		}
		if (builtArea.compareTo(totalArea) > 0) {
			throw SicimDomainException.validation(
					"Built area (" + builtArea + " m²) cannot be greater than total area (" + totalArea + " m²).");
		}
	}

	public static void validatePossession(PossessionType type, PossessionContract contract) {
		if (type == null) {
			return;
		}
		if (type.requiresContract() && contract == null) {
			throw SicimDomainException.validation(
					"PossessionContract is required for properties with possession type \"" + type + "\".");
		}
	}

	public static void validateAcquisitionYear(Integer year, int currentYear) {
		if (year == null) {
			return;
		}
		if (year < MIN_ACQUISITION_YEAR || year > currentYear) {
			throw SicimDomainException.validation(
					"Acquisition year must be between " + MIN_ACQUISITION_YEAR + " and " + currentYear + ".");
		}
	}

	public static void validateOriginalValue(MonetaryValue originalValue) {
		if (originalValue == null) {
			return;
		}
		if (originalValue.amount().signum() <= 0) {
			throw SicimDomainException.validation("originalValue must be positive.");
		}
	}

	/** Nome de categoria personalizada só existe quando a categoria é OTHER. */
	public static String normalizeCustomCategory(UsageCategory category, String customName) {
		if (category != UsageCategory.OTHER || customName == null || customName.isBlank()) {
			return null;
		}
		return customName.trim();
	}

	public static void ensureCanApprove(PropertyStatus status) {
		if (status == PropertyStatus.INACTIVE) {
			throw SicimDomainException.conflict("An inactive property cannot be approved.");
		}
	}

	public static void ensureCanChange(PropertyStatus status) {
		if (status == PropertyStatus.INACTIVE) {
			throw SicimDomainException.conflict("An inactive property cannot be changed.");
		}
	}
}
