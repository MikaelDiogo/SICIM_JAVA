package br.gov.crateus.bcm.sicim.api;

import br.gov.crateus.bcm.sicim.api.dto.ListPropertiesQuery;
import br.gov.crateus.bcm.sicim.api.dto.ListPropertyHistoryQuery;
import br.gov.crateus.bcm.sicim.api.dto.RegisterPropertyRequest;
import br.gov.crateus.bcm.sicim.api.dto.UpdatePropertyRequest;
import br.gov.crateus.bcm.sicim.application.PropertyHistoryService;
import br.gov.crateus.bcm.sicim.application.PropertyService;
import br.gov.crateus.bcm.sicim.application.SicimRoles;
import br.gov.crateus.bcm.sicim.application.command.PropertyHistoryFilter;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.application.result.PropertyHistoryResult;
import br.gov.crateus.bcm.sicim.application.result.PropertyResult;
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
import org.springframework.validation.annotation.Validated;
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
@Validated
@RequestMapping("/api/v1/sicim/properties")
@Tag(name = "sicim", description = "SICIM — imóveis municipais")
@SecurityRequirement(name = "bearer-jwt")
public class PropertyController {

	private final PropertyService propertyService;
	private final PropertyHistoryService historyService;

	public PropertyController(PropertyService propertyService, PropertyHistoryService historyService) {
		this.propertyService = propertyService;
		this.historyService = historyService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize(SicimRoles.CAN_REGISTER)
	@Operation(summary = "Cadastra imóvel (status inicial PENDING_APPROVAL)")
	public PropertyResult register(@Valid @RequestBody RegisterPropertyRequest request) {
		return propertyService.register(request.toCommand());
	}

	@GetMapping
	@PreAuthorize(SicimRoles.CAN_READ)
	@Operation(summary = "Lista imóveis com filtros e paginação { data, total, page, pageSize }")
	public PageResult<PropertyResult> list(@Valid @ModelAttribute ListPropertiesQuery query) {
		return propertyService.list(query.toFilter());
	}

	@GetMapping("/custom-categories")
	@PreAuthorize(SicimRoles.CAN_READ)
	@Operation(summary = "Nomes distintos de categorias personalizadas (usageCategory = OTHER)")
	public List<String> customCategories() {
		return propertyService.listCustomCategoryNames();
	}

	@GetMapping("/{id}")
	@PreAuthorize(SicimRoles.CAN_READ)
	@Operation(summary = "Detalha um imóvel")
	public PropertyResult get(@PathVariable UUID id) {
		return propertyService.get(id);
	}

	@PatchMapping("/{id}")
	@PreAuthorize(SicimRoles.CAN_REGISTER)
	@Operation(summary = "Atualiza parcialmente um imóvel (matrícula e status não são editáveis)")
	public PropertyResult update(@PathVariable UUID id, @Valid @RequestBody UpdatePropertyRequest request) {
		return propertyService.update(id, request.toCommand());
	}

	@PatchMapping("/{id}/approve")
	@PreAuthorize(SicimRoles.CAN_APPROVE)
	@Operation(summary = "Aprova o cadastro do imóvel")
	public PropertyResult approve(@PathVariable UUID id) {
		return propertyService.approve(id);
	}

	@PatchMapping("/{id}/deactivate")
	@PreAuthorize(SicimRoles.CAN_APPROVE)
	@Operation(summary = "Desativa o imóvel (soft-delete: status e lifecycle_status INACTIVE)")
	public PropertyResult deactivate(@PathVariable UUID id) {
		return propertyService.deactivate(id);
	}

	@PatchMapping("/{id}/recalculate-depreciation")
	@PreAuthorize(SicimRoles.ADMIN_ONLY)
	@Operation(summary = "Recalcula a depreciação acumulada pela taxa da categoria de uso")
	public PropertyResult recalculateDepreciation(@PathVariable UUID id) {
		return propertyService.recalculateDepreciation(id);
	}

	@GetMapping("/{id}/history")
	@PreAuthorize(SicimRoles.ADMIN_ONLY)
	@Operation(summary = "Histórico de alterações de um imóvel")
	public PageResult<PropertyHistoryResult> history(
			@PathVariable UUID id,
			@Parameter @RequestParam(defaultValue = "1") @Min(1) int page,
			@Parameter @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
		return historyService.list(new PropertyHistoryFilter(id, null, null, page, pageSize));
	}
}
