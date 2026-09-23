package br.gov.crateus.bcm.sicim.application.port;

import java.time.OffsetDateTime;

/** Relógio injetável (testável). */
public interface TimeProvider {

	OffsetDateTime now();

	default int currentYear() {
		return now().getYear();
	}
}
