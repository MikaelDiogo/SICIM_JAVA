package br.gov.crateus.bcm.sicim.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.gov.crateus.bcm.sicim.application.command.RegisterManagingUnitCommand;
import br.gov.crateus.bcm.sicim.application.fake.InMemoryManagingUnitRepository;
import br.gov.crateus.bcm.sicim.application.result.ManagingUnitResult;
import br.gov.crateus.bcm.sicim.application.usecase.DeactivateManagingUnitUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.ListManagingUnitsUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.RegisterManagingUnitUseCase;
import br.gov.crateus.bcm.sicim.domain.ManagingUnitType;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ManagingUnitUseCasesTest {

	private final InMemoryManagingUnitRepository repository = new InMemoryManagingUnitRepository();
	private final RegisterManagingUnitUseCase register = new RegisterManagingUnitUseCase(repository);
	private final ListManagingUnitsUseCase list = new ListManagingUnitsUseCase(repository);
	private final DeactivateManagingUnitUseCase deactivate = new DeactivateManagingUnitUseCase(repository);

	@Test
	void registerPersistsAndReturnsResultWithCreatedAt() {
		ManagingUnitResult result = register
				.execute(new RegisterManagingUnitCommand("Secretaria de Educação", "SEDUC", ManagingUnitType.SECRETARIAT));

		assertThat(result.id()).isNotNull();
		assertThat(result.acronym()).isEqualTo("SEDUC");
		assertThat(result.lifecycleStatus()).isEqualTo("ACTIVE");
		assertThat(result.createdAt()).isNotNull();
	}

	@Test
	void listReturnsOnlyActiveUnits() {
		register.execute(new RegisterManagingUnitCommand("Secretaria de Saúde", "SESAU", ManagingUnitType.SECRETARIAT));
		ManagingUnitResult toDeactivate = register
				.execute(new RegisterManagingUnitCommand("Fundação Extinta", "FEXT", ManagingUnitType.FOUNDATION));

		deactivate.execute(toDeactivate.id());

		assertThat(list.execute()).extracting(ManagingUnitResult::acronym).containsExactly("SESAU");
	}

	@Test
	void deactivateUnknownUnitIsNotFound() {
		assertThatThrownBy(() -> deactivate.execute(UUID.randomUUID()))
				.isInstanceOf(SicimDomainException.class)
				.hasMessageContaining("not found");
	}
}
