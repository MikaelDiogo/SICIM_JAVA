package br.gov.crateus.bcm.sicim.application.command;

import java.util.UUID;

public record AddressInput(String street, String number, String neighborhood, UUID neighborhoodId,
		String zipCode, String reference) {
}
