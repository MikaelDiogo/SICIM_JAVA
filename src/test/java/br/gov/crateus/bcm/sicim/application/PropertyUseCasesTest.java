package br.gov.crateus.bcm.sicim.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.gov.crateus.bcm.sicim.application.command.AddressInput;
import br.gov.crateus.bcm.sicim.application.command.PossessionContractInput;
import br.gov.crateus.bcm.sicim.application.command.RegisterPropertyCommand;
import br.gov.crateus.bcm.sicim.application.command.UpdatePropertyCommand;
import br.gov.crateus.bcm.sicim.application.fake.InMemoryPropertyRepository;
import br.gov.crateus.bcm.sicim.application.fake.RecordingEventPublisher;
import br.gov.crateus.bcm.sicim.application.fake.RecordingHistoryRepository;
import br.gov.crateus.bcm.sicim.application.port.CurrentUserProvider;
import br.gov.crateus.bcm.sicim.application.port.PropertyEventPublisher.PropertyEvent;
import br.gov.crateus.bcm.sicim.application.port.TimeProvider;
import br.gov.crateus.bcm.sicim.application.result.PropertyResult;
import br.gov.crateus.bcm.sicim.application.support.PropertyChangeRecorder;
import br.gov.crateus.bcm.sicim.application.support.PropertyLookup;
import br.gov.crateus.bcm.sicim.application.usecase.ApprovePropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.DeactivatePropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.GetPropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.RecalculateDepreciationUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.RegisterPropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.UpdatePropertyUseCase;
import br.gov.crateus.bcm.sicim.domain.PossessionType;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import br.gov.crateus.bcm.sicim.domain.PropertyStatus;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import br.gov.crateus.bcm.sicim.domain.exception.ErrorType;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PropertyUseCasesTest {

	private static final UUID UNIT = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-09-23T12:00:00Z");

	private InMemoryPropertyRepository properties;
	private RecordingHistoryRepository history;
	private RecordingEventPublisher events;

	private RegisterPropertyUseCase register;
	private UpdatePropertyUseCase update;
	private ApprovePropertyUseCase approve;
	private DeactivatePropertyUseCase deactivate;
	private RecalculateDepreciationUseCase recalculate;
	private GetPropertyUseCase get;

	@BeforeEach
	void setUp() {
		properties = new InMemoryPropertyRepository();
		history = new RecordingHistoryRepository();
		events = new RecordingEventPublisher();
		TimeProvider time = () -> NOW;
		CurrentUserProvider user = new CurrentUserProvider() {
			public String subject() { return "approver-sub"; }
			public String username() { return "sicim-aprovador"; }
			public Set<String> sicimRoles() { return Set.of("SICIM_APPROVER"); }
		};
		PropertyChangeRecorder recorder = new PropertyChangeRecorder(history, events, () -> "corr-1");
		PropertyLookup lookup = new PropertyLookup(properties);

		register = new RegisterPropertyUseCase(properties, recorder, time);
		update = new UpdatePropertyUseCase(lookup, properties, recorder, time);
		approve = new ApprovePropertyUseCase(lookup, properties, recorder, user, time);
		deactivate = new DeactivatePropertyUseCase(lookup, properties, recorder);
		recalculate = new RecalculateDepreciationUseCase(lookup, properties, recorder, time);
		get = new GetPropertyUseCase(lookup);
	}

	private static RegisterPropertyCommand command(PossessionType type, PossessionContractInput contract) {
		return new RegisterPropertyCommand("mat-2024-00001", "Cartório 1º Ofício", "Descrição cartorial",
				new AddressInput("Rua Coronel Zezé", "100", "Centro", null, "63700-000", null),
				new BigDecimal("500"), new BigDecimal("200"), new BigDecimal("-5.1783"), new BigDecimal("-40.6775"),
				UNIT, null, UsageCategory.ADMINISTRATIVE, null, type, contract, 2016,
				new BigDecimal("100000"), "Sede administrativa");
	}

	private static UpdatePropertyCommand purpose(String publicPurpose) {
		return new UpdatePropertyCommand(null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, publicPurpose);
	}

	@Test
	void registerPersistsRecordsHistoryAndPublishesEvent() {
		PropertyResult result = register.execute(command(PossessionType.OWNED, null));

		assertThat(result.status()).isEqualTo(PropertyStatus.PENDING_APPROVAL);
		assertThat(result.registrationNumber()).isEqualTo("MAT-2024-00001");
		assertThat(history.entries).singleElement().satisfies(e -> {
			assertThat(e.action()).isEqualTo(PropertyHistoryAction.CREATE);
			assertThat(e.dataBefore()).isNull();
			assertThat(e.correlationId()).isEqualTo("corr-1");
		});
		assertThat(events.events).containsExactly(PropertyEvent.PROPERTY_REGISTERED);
	}

	@Test
	void duplicateRegistrationIsConflictAndHasNoSideEffects() {
		register.execute(command(PossessionType.OWNED, null));

		assertThatThrownBy(() -> register.execute(command(PossessionType.OWNED, null)))
				.isInstanceOfSatisfying(SicimDomainException.class,
						e -> assertThat(e.getType()).isEqualTo(ErrorType.CONFLICT));
		assertThat(properties.size()).isEqualTo(1);
		assertThat(events.events).hasSize(1);
	}

	@Test
	void rentedRequiresContract() {
		assertThatThrownBy(() -> register.execute(command(PossessionType.RENTED, null)))
				.isInstanceOf(SicimDomainException.class);

		PossessionContractInput contract = new PossessionContractInput(OffsetDateTime.parse("2025-01-01T00:00:00Z"),
				null, new BigDecimal("2500"), null, null, "Locador", "PA-2025-001");
		assertThat(register.execute(command(PossessionType.RENTED, contract)).possessionContract()).isNotNull();
	}

	@Test
	void updateStoresBeforeAndAfterSnapshots() {
		UUID id = register.execute(command(PossessionType.OWNED, null)).id();

		update.execute(id, purpose("Arquivo municipal"));

		var entry = history.entries.get(1);
		assertThat(entry.action()).isEqualTo(PropertyHistoryAction.UPDATE);
		assertThat(entry.dataBefore()).containsEntry("publicPurpose", "Sede administrativa");
		assertThat(entry.dataAfter()).containsEntry("publicPurpose", "Arquivo municipal");
	}

	@Test
	void approveUsesAuthenticatedSubjectAndClock() {
		UUID id = register.execute(command(PossessionType.OWNED, null)).id();

		PropertyResult result = approve.execute(id);

		assertThat(result.approvedById()).isEqualTo("approver-sub");
		assertThat(result.approvedAt()).isEqualTo(NOW);
		assertThat(events.events).endsWith(PropertyEvent.PROPERTY_APPROVED);
	}

	@Test
	void deactivatedPropertyCannotBeApprovedOrEdited() {
		UUID id = register.execute(command(PossessionType.OWNED, null)).id();
		deactivate.execute(id);

		assertThatThrownBy(() -> approve.execute(id)).isInstanceOf(SicimDomainException.class);
		assertThatThrownBy(() -> update.execute(id, purpose("x"))).isInstanceOf(SicimDomainException.class);
		assertThat(get.execute(id).lifecycleStatus()).isEqualTo("INACTIVE");
	}

	@Test
	void recalculateDepreciationUsesCurrentYearFromClock() {
		UUID id = register.execute(command(PossessionType.OWNED, null)).id();

		PropertyResult result = recalculate.execute(id);

		assertThat(result.accumulatedDepreciation()).isEqualByComparingTo("20000.00");
		assertThat(result.netBookValue()).isEqualByComparingTo("80000.00");
	}

	@Test
	void unknownPropertyIsNotFound() {
		assertThatThrownBy(() -> get.execute(UUID.randomUUID()))
				.isInstanceOfSatisfying(SicimDomainException.class,
						e -> assertThat(e.getType()).isEqualTo(ErrorType.NOT_FOUND));
	}
}
