package br.gov.crateus.bcm.sicim.domain;

import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PossessionContract(
		OffsetDateTime startDate,
		OffsetDateTime endDate,
		BigDecimal monthlyValue,
		BigDecimal referenceValue,
		String grantor,
		String lessor,
		String administrativeProcessNumber
) {

	public PossessionContract {
		if (startDate == null) {
			throw SicimDomainException.validation("Possession contract start date is required.");
		}
		if (administrativeProcessNumber == null || administrativeProcessNumber.isBlank()) {
			throw SicimDomainException.validation("Possession contract administrative process number is required.");
		}
		if (endDate != null && endDate.isBefore(startDate)) {
			throw SicimDomainException.validation("Possession contract end date cannot be before start date.");
		}
		if (monthlyValue != null && monthlyValue.signum() <= 0) {
			throw SicimDomainException.validation("Possession contract monthly value must be positive.");
		}
		if (referenceValue != null && referenceValue.signum() <= 0) {
			throw SicimDomainException.validation("Possession contract reference value must be positive.");
		}
	}

	public boolean isCurrent(OffsetDateTime now) {
		return endDate == null || endDate.isAfter(now);
	}
}
