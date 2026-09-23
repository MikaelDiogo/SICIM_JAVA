package br.gov.crateus.bcm.sicim.application.command;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PossessionContractInput(OffsetDateTime startDate, OffsetDateTime endDate, BigDecimal monthlyValue,
		BigDecimal referenceValue, String grantor, String lessor, String administrativeProcessNumber) {
}
