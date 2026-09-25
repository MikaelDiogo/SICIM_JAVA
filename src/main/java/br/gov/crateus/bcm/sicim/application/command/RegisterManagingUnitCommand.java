package br.gov.crateus.bcm.sicim.application.command;

import br.gov.crateus.bcm.sicim.domain.ManagingUnitType;

public record RegisterManagingUnitCommand(String name, String acronym, ManagingUnitType type) {
}
