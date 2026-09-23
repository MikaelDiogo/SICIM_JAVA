package br.gov.crateus.bcm.sicim.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.crateus.bcm.sdk.outbox.OutboxRecorder;
import br.gov.crateus.bcm.sicim.application.command.AddressInput;
import br.gov.crateus.bcm.sicim.application.command.PossessionContractInput;
import br.gov.crateus.bcm.sicim.application.command.RegisterPropertyCommand;
import br.gov.crateus.bcm.sicim.application.port.CorrelationIdProvider;
import br.gov.crateus.bcm.sicim.application.port.CurrentUserProvider;
import br.gov.crateus.bcm.sicim.application.result.PropertyResult;
import br.gov.crateus.bcm.sicim.domain.PossessionType;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import br.gov.crateus.bcm.sicim.domain.PropertyStatus;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import br.gov.crateus.bcm.sicim.domain.exception.ErrorType;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyEntity;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyHistoryEntity;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyHistoryJpaRepository;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyJpaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PropertyServiceTest {

	private static final UUID UNIT = UUID.fromString("11111111-1111-1111-1111-111111111111");

	private PropertyJpaRepository properties;
	private PropertyHistoryJpaRepository history;
	private OutboxRecorder outbox;
	private CurrentUserProvider currentUser;
	private PropertyService service;

	@BeforeEach
	void setUp() {
		properties = mock(PropertyJpaRepository.class);
		history = mock(PropertyHistoryJpaRepository.class);
		outbox = mock(OutboxRecorder.class);
		currentUser = mock(CurrentUserProvider.class);
		CorrelationIdProvider correlation = () -> "corr-123";
		when(currentUser.subject()).thenReturn("user-sub");
		when(properties.save(any(PropertyEntity.class))).thenAnswer(inv -> inv.getArgument(0));
		when(history.save(any(PropertyHistoryEntity.class))).thenAnswer(inv -> inv.getArgument(0));
		Clock clock = Clock.fixed(Instant.parse("2026-09-23T12:00:00Z"), ZoneOffset.UTC);
		service = new PropertyService(properties, history, outbox, currentUser, correlation, clock);
	}

	private static RegisterPropertyCommand command(PossessionType type, PossessionContractInput contract,
			String total, String built) {
		return new RegisterPropertyCommand("mat-2024-00001", "Cartório 1º Ofício", "Descrição cartorial",
				new AddressInput("Rua Coronel Zezé", "100", "Centro", null, "63700-000", null),
				new BigDecimal(total), new BigDecimal(built), new BigDecimal("-5.1783"), new BigDecimal("-40.6775"),
				UNIT, null, UsageCategory.EDUCATIONAL, "ignored", type, contract, 2016,
				new BigDecimal("100000"), "Escola municipal");
	}

	private PropertyEntity existing(PropertyStatus status) {
		PropertyEntity p = new PropertyEntity();
		p.setRegistrationNumber("MAT-2024-00001");
		p.setManagingUnitId(UNIT);
		p.setOriginalValue(new BigDecimal("100000.00"));
		p.setAccumulatedDepreciation(BigDecimal.ZERO);
		p.setUsageCategory(UsageCategory.ADMINISTRATIVE);
		p.setAcquisitionYear(2016);
		p.setStatus(status);
		p.setPossessionType(PossessionType.OWNED);
		when(properties.findById(p.getId())).thenReturn(Optional.of(p));
		return p;
	}

	@Test
	void registerCreatesPendingPropertyWithHistoryAndOutboxInSameFlow() {
		PropertyResult result = service.register(command(PossessionType.OWNED, null, "500", "200"));

		assertThat(result.status()).isEqualTo(PropertyStatus.PENDING_APPROVAL);
		assertThat(result.registrationNumber()).isEqualTo("MAT-2024-00001");
		assertThat(result.customCategoryName()).isNull();
		assertThat(result.accumulatedDepreciation()).isEqualByComparingTo("0");

		ArgumentCaptor<PropertyHistoryEntity> entry = ArgumentCaptor.forClass(PropertyHistoryEntity.class);
		verify(history).save(entry.capture());
		assertThat(entry.getValue().getAction()).isEqualTo(PropertyHistoryAction.CREATE);
		assertThat(entry.getValue().getDataBefore()).isNull();
		assertThat(entry.getValue().getCorrelationId()).isEqualTo("corr-123");
		verify(outbox).record(eq("Property"), anyString(), eq("PropertyRegistered"), any());
	}

	@Test
	void registerRejectsDuplicateRegistrationNumberWith409() {
		when(properties.existsByRegistrationNumber("MAT-2024-00001")).thenReturn(true);

		assertThatThrownBy(() -> service.register(command(PossessionType.OWNED, null, "500", "200")))
				.isInstanceOfSatisfying(SicimDomainException.class,
						e -> assertThat(e.getType()).isEqualTo(ErrorType.CONFLICT));
		verify(properties, never()).save(any());
		verify(outbox, never()).record(any(), any(), any(), any());
	}

	@Test
	void registerRequiresContractForRentedProperty() {
		assertThatThrownBy(() -> service.register(command(PossessionType.RENTED, null, "500", "200")))
				.isInstanceOf(SicimDomainException.class);

		PossessionContractInput contract = new PossessionContractInput(OffsetDateTime.parse("2025-01-01T00:00:00Z"),
				null, new BigDecimal("2500"), null, null, "Locador", "PA-2025-001");
		PropertyResult ok = service.register(command(PossessionType.RENTED, contract, "500", "200"));
		assertThat(ok.possessionContract()).isNotNull();
	}

	@Test
	void registerRejectsBuiltAreaGreaterThanTotal() {
		assertThatThrownBy(() -> service.register(command(PossessionType.OWNED, null, "100", "150")))
				.isInstanceOf(SicimDomainException.class);
	}

	@Test
	void approveRecordsApproverFromJwt() {
		PropertyEntity p = existing(PropertyStatus.PENDING_APPROVAL);

		PropertyResult result = service.approve(p.getId());

		assertThat(result.status()).isEqualTo(PropertyStatus.APPROVED);
		assertThat(result.approvedById()).isEqualTo("user-sub");
		verify(outbox).record(eq("Property"), eq(p.getId().toString()), eq("PropertyApproved"), any());
	}

	@Test
	void approveInactivePropertyIsConflict() {
		PropertyEntity p = existing(PropertyStatus.INACTIVE);

		assertThatThrownBy(() -> service.approve(p.getId()))
				.isInstanceOfSatisfying(SicimDomainException.class,
						e -> assertThat(e.getType()).isEqualTo(ErrorType.CONFLICT));
	}

	@Test
	void deactivateIsSoftDelete() {
		PropertyEntity p = existing(PropertyStatus.APPROVED);

		PropertyResult result = service.deactivate(p.getId());

		assertThat(result.status()).isEqualTo(PropertyStatus.INACTIVE);
		assertThat(result.lifecycleStatus()).isEqualTo("INACTIVE");
		verify(properties, never()).delete(any());
	}

	@Test
	void recalculateDepreciationUsesCategoryRateAndClock() {
		PropertyEntity p = existing(PropertyStatus.APPROVED);

		PropertyResult result = service.recalculateDepreciation(p.getId());

		assertThat(result.accumulatedDepreciation()).isEqualByComparingTo("20000.00");
		assertThat(result.netBookValue()).isEqualByComparingTo("80000.00");
	}

	@Test
	void unknownPropertyIs404() {
		UUID id = UUID.randomUUID();
		when(properties.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.get(id))
				.isInstanceOfSatisfying(SicimDomainException.class,
						e -> assertThat(e.getType()).isEqualTo(ErrorType.NOT_FOUND));
	}
}
