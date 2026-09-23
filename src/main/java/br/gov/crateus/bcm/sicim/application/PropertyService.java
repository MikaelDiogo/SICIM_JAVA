package br.gov.crateus.bcm.sicim.application;

import br.gov.crateus.bcm.sdk.outbox.OutboxRecorder;
import br.gov.crateus.bcm.sicim.application.command.AddressInput;
import br.gov.crateus.bcm.sicim.application.command.PossessionContractInput;
import br.gov.crateus.bcm.sicim.application.command.PropertyFilter;
import br.gov.crateus.bcm.sicim.application.command.RegisterPropertyCommand;
import br.gov.crateus.bcm.sicim.application.command.UpdatePropertyCommand;
import br.gov.crateus.bcm.sicim.application.port.CorrelationIdProvider;
import br.gov.crateus.bcm.sicim.application.port.CurrentUserProvider;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.application.result.PropertyResult;
import br.gov.crateus.bcm.sicim.domain.Address;
import br.gov.crateus.bcm.sicim.domain.DepreciationCalculator;
import br.gov.crateus.bcm.sicim.domain.Geolocation;
import br.gov.crateus.bcm.sicim.domain.MonetaryValue;
import br.gov.crateus.bcm.sicim.domain.PossessionContract;
import br.gov.crateus.bcm.sicim.domain.PossessionType;
import br.gov.crateus.bcm.sicim.domain.PropertyHistoryAction;
import br.gov.crateus.bcm.sicim.domain.PropertyRules;
import br.gov.crateus.bcm.sicim.domain.PropertyStatus;
import br.gov.crateus.bcm.sicim.domain.RegistrationNumber;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyEntity;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyHistoryEntity;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyHistoryJpaRepository;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertyJpaRepository;
import br.gov.crateus.bcm.sicim.infrastructure.persistence.PropertySpecifications;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso do imóvel (Register, Update, Approve, Deactivate, RecalculateDepreciation, Get, List).
 * Cada escrita grava histórico e evento de outbox na MESMA transação de negócio.
 */
@Service
public class PropertyService {

	static final String AGGREGATE = "Property";
	static final String SOURCE_WEB = "WEB";

	private final PropertyJpaRepository properties;
	private final PropertyHistoryJpaRepository history;
	private final OutboxRecorder outbox;
	private final CurrentUserProvider currentUser;
	private final CorrelationIdProvider correlationId;
	private final Clock clock;

	public PropertyService(PropertyJpaRepository properties, PropertyHistoryJpaRepository history,
			OutboxRecorder outbox, CurrentUserProvider currentUser, CorrelationIdProvider correlationId) {
		this(properties, history, outbox, currentUser, correlationId, Clock.systemUTC());
	}

	PropertyService(PropertyJpaRepository properties, PropertyHistoryJpaRepository history, OutboxRecorder outbox,
			CurrentUserProvider currentUser, CorrelationIdProvider correlationId, Clock clock) {
		this.properties = properties;
		this.history = history;
		this.outbox = outbox;
		this.currentUser = currentUser;
		this.correlationId = correlationId;
		this.clock = clock;
	}

	@Transactional
	public PropertyResult register(RegisterPropertyCommand cmd) {
		RegistrationNumber registration = RegistrationNumber.of(cmd.registrationNumber());
		if (properties.existsByRegistrationNumber(registration.value())) {
			throw SicimDomainException.conflict(
					"A property with registration number \"" + registration.value() + "\" already exists.");
		}
		requireManagingUnit(cmd.managingUnitId());

		PropertyEntity p = new PropertyEntity();
		p.setRegistrationNumber(registration.value());
		p.setNotaryOffice(required(cmd.notaryOffice(), "notaryOffice"));
		p.setNotarialDescription(required(cmd.notarialDescription(), "notarialDescription"));
		applyAddress(p, cmd.address());
		PropertyRules.validateAreas(cmd.totalArea(), cmd.builtArea());
		p.setTotalArea(cmd.totalArea());
		p.setBuiltArea(cmd.builtArea());
		applyGeolocation(p, new Geolocation(cmd.latitude(), cmd.longitude()));
		p.setManagingUnitId(cmd.managingUnitId());
		p.setBudgetUnit(cmd.budgetUnit());
		if (cmd.usageCategory() == null) {
			throw SicimDomainException.validation("usageCategory is required.");
		}
		p.setUsageCategory(cmd.usageCategory());
		p.setCustomCategoryName(PropertyRules.normalizeCustomCategory(cmd.usageCategory(), cmd.customCategoryName()));
		applyPossession(p, cmd.possessionType(), toContract(cmd.possessionType(), cmd.possessionContract()));
		if (cmd.acquisitionYear() == null) {
			throw SicimDomainException.validation("acquisitionYear is required.");
		}
		PropertyRules.validateAcquisitionYear(cmd.acquisitionYear(), currentYear());
		p.setAcquisitionYear(cmd.acquisitionYear());
		p.setOriginalValue(positiveValue(cmd.originalValue(), "originalValue"));
		p.setAccumulatedDepreciation(MonetaryValue.ZERO.amount());
		p.setPublicPurpose(required(cmd.publicPurpose(), "publicPurpose"));
		p.setStatus(PropertyStatus.PENDING_APPROVAL);
		p.setSource(SOURCE_WEB);

		PropertyEntity saved = properties.save(p);
		recordChange(saved, PropertyHistoryAction.CREATE, null, "PropertyRegistered");
		return PropertyResult.from(saved);
	}

	@Transactional
	public PropertyResult update(UUID id, UpdatePropertyCommand cmd) {
		PropertyEntity p = find(id);
		PropertyRules.ensureCanChange(p.getStatus());
		Map<String, Object> before = PropertySnapshot.of(p);

		if (cmd.managingUnitId() != null) {
			requireManagingUnit(cmd.managingUnitId());
			p.setManagingUnitId(cmd.managingUnitId());
		}
		if (cmd.notaryOffice() != null) {
			p.setNotaryOffice(required(cmd.notaryOffice(), "notaryOffice"));
		}
		if (cmd.notarialDescription() != null) {
			p.setNotarialDescription(required(cmd.notarialDescription(), "notarialDescription"));
		}
		if (cmd.address() != null) {
			applyAddress(p, cmd.address());
		}
		var totalArea = cmd.totalArea() != null ? cmd.totalArea() : p.getTotalArea();
		var builtArea = cmd.builtArea() != null ? cmd.builtArea() : p.getBuiltArea();
		PropertyRules.validateAreas(totalArea, builtArea);
		p.setTotalArea(totalArea);
		p.setBuiltArea(builtArea);
		if (cmd.latitude() != null || cmd.longitude() != null) {
			applyGeolocation(p, new Geolocation(
					cmd.latitude() != null ? cmd.latitude() : p.getLatitude(),
					cmd.longitude() != null ? cmd.longitude() : p.getLongitude()));
		}
		if (cmd.budgetUnit() != null) {
			p.setBudgetUnit(cmd.budgetUnit());
		}
		if (cmd.usageCategory() != null || cmd.customCategoryName() != null) {
			UsageCategory category = cmd.usageCategory() != null ? cmd.usageCategory() : p.getUsageCategory();
			String custom = cmd.customCategoryName() != null ? cmd.customCategoryName() : p.getCustomCategoryName();
			p.setUsageCategory(category);
			p.setCustomCategoryName(PropertyRules.normalizeCustomCategory(category, custom));
		}
		if (cmd.possessionType() != null || cmd.possessionContract() != null) {
			PossessionType type = cmd.possessionType() != null ? cmd.possessionType() : p.getPossessionType();
			PossessionContract contract = cmd.possessionContract() != null
					? toContract(type, cmd.possessionContract())
					: currentContract(p, type);
			applyPossession(p, type, contract);
		}
		if (cmd.acquisitionYear() != null) {
			PropertyRules.validateAcquisitionYear(cmd.acquisitionYear(), currentYear());
			p.setAcquisitionYear(cmd.acquisitionYear());
		}
		if (cmd.originalValue() != null) {
			p.setOriginalValue(positiveValue(cmd.originalValue(), "originalValue"));
		}
		if (cmd.publicPurpose() != null) {
			p.setPublicPurpose(required(cmd.publicPurpose(), "publicPurpose"));
		}

		PropertyEntity saved = properties.save(p);
		recordChange(saved, PropertyHistoryAction.UPDATE, before, "PropertyUpdated");
		return PropertyResult.from(saved);
	}

	@Transactional
	public PropertyResult approve(UUID id) {
		PropertyEntity p = find(id);
		PropertyRules.ensureCanApprove(p.getStatus());
		Map<String, Object> before = PropertySnapshot.of(p);
		p.setStatus(PropertyStatus.APPROVED);
		p.setApprovedBy(currentUser.subject());
		p.setApprovedAt(OffsetDateTime.now(clock));
		PropertyEntity saved = properties.save(p);
		recordChange(saved, PropertyHistoryAction.APPROVE, before, "PropertyApproved");
		return PropertyResult.from(saved);
	}

	/** Desativação lógica: status INACTIVE + lifecycle_status INACTIVE. Sem DELETE físico. */
	@Transactional
	public PropertyResult deactivate(UUID id) {
		PropertyEntity p = find(id);
		Map<String, Object> before = PropertySnapshot.of(p);
		p.setStatus(PropertyStatus.INACTIVE);
		p.setLifecycleStatus("INACTIVE");
		PropertyEntity saved = properties.save(p);
		recordChange(saved, PropertyHistoryAction.DEACTIVATE, before, "PropertyDeactivated");
		return PropertyResult.from(saved);
	}

	@Transactional
	public PropertyResult recalculateDepreciation(UUID id) {
		PropertyEntity p = find(id);
		Map<String, Object> before = PropertySnapshot.of(p);
		MonetaryValue depreciation = DepreciationCalculator.calculate(
				MonetaryValue.of(p.getOriginalValue()), p.getUsageCategory(), p.getAcquisitionYear(), currentYear());
		p.setAccumulatedDepreciation(depreciation.amount());
		PropertyEntity saved = properties.save(p);
		recordChange(saved, PropertyHistoryAction.RECALCULATE_DEPRECIATION, before, "PropertyDepreciationRecalculated");
		return PropertyResult.from(saved);
	}

	@Transactional(readOnly = true)
	public PropertyResult get(UUID id) {
		return PropertyResult.from(find(id));
	}

	@Transactional(readOnly = true)
	public PageResult<PropertyResult> list(PropertyFilter f) {
		Specification<PropertyEntity> spec = Specification.allOf(Arrays.asList(
				PropertySpecifications.notDeleted(),
				PropertySpecifications.hasStatus(f.status()),
				PropertySpecifications.hasUsageCategory(f.usageCategory()),
				PropertySpecifications.hasManagingUnit(f.managingUnitId()),
				PropertySpecifications.acquiredFrom(f.acquisitionYearFrom()),
				PropertySpecifications.acquiredTo(f.acquisitionYearTo())));
		PageRequest pageable = PageRequest.of(f.page() - 1, f.pageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<PropertyEntity> page = properties.findAll(spec, pageable);
		return new PageResult<>(page.map(PropertyResult::from).getContent(), page.getTotalElements(),
				f.page(), f.pageSize());
	}

	@Transactional(readOnly = true)
	public List<String> listCustomCategoryNames() {
		return properties.findDistinctCustomCategoryNames();
	}

	// ---------------------------------------------------------------------------------------------

	private PropertyEntity find(UUID id) {
		return properties.findById(id)
				.filter(p -> !"DELETED".equals(p.getLifecycleStatus()))
				.orElseThrow(() -> SicimDomainException.notFound("No property found with id \"" + id + "\"."));
	}

	private void recordChange(PropertyEntity saved, PropertyHistoryAction action, Map<String, Object> before,
			String eventType) {
		Map<String, Object> after = PropertySnapshot.of(saved);
		PropertyHistoryEntity entry = new PropertyHistoryEntity(saved.getId(), action, before, after,
				correlationId.currentCorrelationId());
		entry.setSource(SOURCE_WEB);
		entry.setOrgId(saved.getOrgId());
		history.save(entry);

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("propertyId", saved.getId().toString());
		payload.put("registrationNumber", saved.getRegistrationNumber());
		payload.put("managingUnitId", saved.getManagingUnitId().toString());
		payload.put("status", saved.getStatus().name());
		payload.put("action", action.name());
		outbox.record(AGGREGATE, saved.getId().toString(), eventType, payload);
	}

	/**
	 * O órgão gestor é da plataforma (organization). O módulo só guarda o UUID e não faz JOIN
	 * cross-schema; a validação de existência fica a cargo da integração Seplati (ver nota técnica).
	 */
	private static void requireManagingUnit(UUID managingUnitId) {
		if (managingUnitId == null) {
			throw SicimDomainException.validation("managingUnitId is required.");
		}
	}

	private static void applyAddress(PropertyEntity p, AddressInput in) {
		if (in == null) {
			throw SicimDomainException.validation("address is required.");
		}
		Address address = new Address(trim(in.street()), trim(in.number()), trim(in.neighborhood()),
				trim(in.zipCode()), trim(in.reference()));
		p.setAddressStreet(address.street());
		p.setAddressNumber(address.number());
		p.setAddressNeighborhood(address.neighborhood());
		p.setNeighborhoodId(in.neighborhoodId());
		p.setAddressZipCode(address.zipCode());
		p.setAddressReference(address.reference());
	}

	private static void applyGeolocation(PropertyEntity p, Geolocation g) {
		p.setLatitude(g.latitude());
		p.setLongitude(g.longitude());
	}

	private static void applyPossession(PropertyEntity p, PossessionType type, PossessionContract contract) {
		PropertyRules.validatePossession(type, contract);
		p.setPossessionType(type);
		PossessionContract effective = type.requiresContract() ? contract : null;
		p.setContractStartDate(effective == null ? null : effective.startDate());
		p.setContractEndDate(effective == null ? null : effective.endDate());
		p.setContractMonthlyValue(effective == null ? null : effective.monthlyValue());
		p.setContractReferenceValue(effective == null ? null : effective.referenceValue());
		p.setContractGrantor(effective == null ? null : effective.grantor());
		p.setContractLessor(effective == null ? null : effective.lessor());
		p.setContractAdministrativeProcessNumber(effective == null ? null : effective.administrativeProcessNumber());
	}

	private static PossessionContract toContract(PossessionType type, PossessionContractInput in) {
		if (in == null || type == null || !type.requiresContract()) {
			return null;
		}
		return new PossessionContract(in.startDate(), in.endDate(), in.monthlyValue(), in.referenceValue(),
				in.grantor(), in.lessor(), in.administrativeProcessNumber());
	}

	private static PossessionContract currentContract(PropertyEntity p, PossessionType type) {
		if (!type.requiresContract() || p.getContractStartDate() == null) {
			return null;
		}
		return new PossessionContract(p.getContractStartDate(), p.getContractEndDate(), p.getContractMonthlyValue(),
				p.getContractReferenceValue(), p.getContractGrantor(), p.getContractLessor(),
				p.getContractAdministrativeProcessNumber());
	}

	private static java.math.BigDecimal positiveValue(java.math.BigDecimal value, String field) {
		if (value == null || value.signum() <= 0) {
			throw SicimDomainException.validation(field + " must be positive.");
		}
		return MonetaryValue.of(value).amount();
	}

	private static String required(String value, String field) {
		if (value == null || value.isBlank()) {
			throw SicimDomainException.validation(field + " is required.");
		}
		return value.trim();
	}

	private static String trim(String s) {
		return s == null ? null : s.trim();
	}

	private int currentYear() {
		return Year.now(clock).getValue();
	}
}
