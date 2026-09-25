package br.gov.crateus.bcm.sicim.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ManagingUnitTest {

	@Test
	void registerStartsActiveWithoutCreatedAt() {
		ManagingUnit unit = ManagingUnit.register("Secretaria de Educação", "SEDUC", ManagingUnitType.SECRETARIAT);

		assertThat(unit.id()).isNotNull();
		assertThat(unit.name()).isEqualTo("Secretaria de Educação");
		assertThat(unit.acronym()).isEqualTo("SEDUC");
		assertThat(unit.lifecycleStatus()).isEqualTo(LifecycleStatus.ACTIVE);
		assertThat(unit.createdAt()).isNull();
	}

	@Test
	void deactivateFlipsLifecycleStatusKeepingIdentity() {
		ManagingUnit unit = ManagingUnit.register("Secretaria de Saúde", "SESAU", ManagingUnitType.SECRETARIAT);

		ManagingUnit deactivated = unit.deactivate();

		assertThat(deactivated.id()).isEqualTo(unit.id());
		assertThat(deactivated.lifecycleStatus()).isEqualTo(LifecycleStatus.INACTIVE);
	}

	@Test
	void blankNameIsRejected() {
		assertThatThrownBy(() -> ManagingUnit.register("  ", "SEAD", ManagingUnitType.SECRETARIAT))
				.isInstanceOf(br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException.class)
				.hasMessageContaining("name");
	}

	@Test
	void blankAcronymIsRejected() {
		assertThatThrownBy(() -> ManagingUnit.register("Secretaria de Administração", " ", ManagingUnitType.SECRETARIAT))
				.isInstanceOf(br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException.class)
				.hasMessageContaining("acronym");
	}
}
