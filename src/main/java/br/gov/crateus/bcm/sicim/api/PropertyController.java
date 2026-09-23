package br.gov.crateus.bcm.sicim.api;

import br.gov.crateus.bcm.sicim.api.dto.ListPropertiesQuery;
import br.gov.crateus.bcm.sicim.api.dto.RegisterPropertyRequest;
import br.gov.crateus.bcm.sicim.api.dto.UpdatePropertyRequest;
import br.gov.crateus.bcm.sicim.application.SicimRoles;
import br.gov.crateus.bcm.sicim.application.command.PropertyHistoryFilter;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.application.result.PropertyHistoryResult;
import br.gov.crateus.bcm.sicim.application.result.PropertyResult;
import br.gov.crateus.bcm.sicim.application.usecase.ApprovePropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.DeactivatePropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.GetPropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.ListPropertiesUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.ListPropertyHistoryUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.RecalculateDepreciationUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.RegisterPropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.UpdatePropertyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sicim/properties")
@Tag(name = "sicim", description = "SICIM — imóveis municipais")
@SecurityRequirement(name = "bearer-jwt")
public class PropertyController {

	private final RegisterPropertyUseCase registerProperty;
	private final UpdatePropertyUseCase updateProperty;
	private final ApprovePropertyUseCase approveProperty;
	private final DeactivatePropertyUseCase deactivateProperty;
	private final RecalculateDepreciationUseCase recalculateDepreciation;
	private final GetPropertyUseCase getProperty;
	private final ListPropertiesUseCase listProperties;
	private final ListPropertyHistoryUseCase listHistory;

	public PropertyController(RegisterPropertyUseCase registerProperty, UpdatePropertyUseCase updateProperty,
			ApprovePropertyUseCase approveProperty, DeactivatePropertyUseCase deactivateProperty,
			RecalculateDepreciationUseCase recalculateDepreciation, GetPropertyUseCase getProperty,
			ListPropertiesUseCase listProperties, ListPropertyHistoryUseCase listHistory) {
		this.registerProperty = registerProperty;
		this.updateProperty = updateProperty;
		this.approveProperty = approveProperty;
		this.deactivateProperty = deactivateProperty;
		this.recalculateDepreciation = recalculateDepreciation;
		this.getProperty = getProperty;
		this.listProperties = listProperties;
		this.listHistory = listHistory;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize(SicimRoles.CAN_REGISTER)
	@Operation(summary = "Cadastra imóvel (status inicial PENDING_APPROVAL)")
	public PropertyResult register(@Valid @RequestBody RegisterPropertyRequest request) {
		return registerProperty.execute(request.toCommand());
	}

	@GetMapping
	@PreAuthorize(SicimRoles.CAN_READ)
	@Operation(summary = "Lista imóveis com filtros e paginação { data, total, page, pageSize }")
	public PageResult<PropertyResult> list(@Valid @ModelAttribute ListPropertiesQuery query) {
		return listProperties.execute(query.toFilter());
	}

	@GetMapping("/custom-categories")
	@PreAuthorize(SicimRoles.CAN_READ)
	@Operation(summary = "Nomes distintos de categorias personalizadas (usageCategory = OTHER)")
	public List<String> customCategories() {
		return listProperties.customCategoryNames();
	}

	@GetMapping("/{id}")
	@PreAuthorize(SicimRoles.CAN_READ)
	@Operation(summary = "Detalha um imóvel")
	public PropertyResult get(@PathVariable UUID id) {
		return getProperty.execute(id);
	}

	@PatchMapping("/{id}")
	@PreAuthorize(SicimRoles.CAN_REGISTER)
	@Operation(summary = "Atualiza parcialmente um imóvel (matrícula e status não são editáveis)")
	public PropertyResult update(@PathVariable UUID id, @Valid @RequestBody UpdatePropertyRequest request) {
		return updateProperty.execute(id, request.toCommand());
	}

	@PatchMapping("/{id}/approve")
	@PreAuthorize(SicimRoles.CAN_APPROVE)
	@Operation(summary = "Aprova o cadastro do imóvel")
	public PropertyResult approve(@PathVariable UUID id) {
		return approveProperty.execute(id);
	}

	@PatchMapping("/{id}/deactivate")
	@PreAuthorize(SicimRoles.CAN_APPROVE)
	@Operation(summary = "Desativa o imóvel (soft-delete: status e lifecycle_status INACTIVE)")
	public PropertyResult deactivate(@PathVariable UUID id) {
		return deactivateProperty.execute(id);
	}

	@PatchMapping("/{id}/recalculate-depreciation")
	@PreAuthorize(SicimRoles.ADMIN_ONLY)
	@Operation(summary = "Recalcula a depreciação acumulada pela taxa da categoria de uso")
	public PropertyResult recalculate(@PathVariable UUID id) {
		return recalculateDepreciation.execute(id);
	}

	@GetMapping("/{id}/history")
	@PreAuthorize(SicimRoles.ADMIN_ONLY)
	@Operation(summary = "Histórico de alterações de um imóvel")
	public PageResult<PropertyHistoryResult> history(
			@PathVariable UUID id,
			@Parameter @RequestParam(defaultValue = "1") @Min(1) int page,
			@Parameter @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
		return listHistory.execute(new PropertyHistoryFilter(id, null, null, page, pageSize));
	}
}
