package br.gov.crateus.bcm.sicim.api;

import br.gov.crateus.bcm.sicim.api.dto.ListPropertyHistoryQuery;
import br.gov.crateus.bcm.sicim.application.PropertyHistoryService;
import br.gov.crateus.bcm.sicim.application.SicimRoles;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.application.result.PropertyHistoryResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Substitui GET /audit-logs do NestJS. */
@RestController
@RequestMapping("/api/v1/sicim/property-history")
@Tag(name = "sicim")
@SecurityRequirement(name = "bearer-jwt")
public class PropertyHistoryController {

	private final PropertyHistoryService historyService;

	public PropertyHistoryController(PropertyHistoryService historyService) {
		this.historyService = historyService;
	}

	@GetMapping
	@PreAuthorize(SicimRoles.ADMIN_ONLY)
	@Operation(summary = "Lista o histórico de alterações de imóveis (auditoria de produto)")
	public PageResult<PropertyHistoryResult> list(@Valid @ModelAttribute ListPropertyHistoryQuery query) {
		return historyService.list(query.toFilter());
	}
}
