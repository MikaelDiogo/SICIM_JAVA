package br.gov.crateus.bcm.sicim.api;

import br.gov.crateus.bcm.sicim.api.dto.RegisterManagingUnitRequest;
import br.gov.crateus.bcm.sicim.application.SicimRoles;
import br.gov.crateus.bcm.sicim.application.result.ManagingUnitResult;
import br.gov.crateus.bcm.sicim.application.usecase.DeactivateManagingUnitUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.GetManagingUnitUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.ListManagingUnitsUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.RegisterManagingUnitUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro local de órgãos gestores mantido pelo SICIM enquanto a API de organization da
 * plataforma não existe. Exceção documentada à regra 1.6 de REGRAS.md — ver NOTA-TECNICA.md
 * item 2: até a Seplati entregar a API real, {@code managingUnitId} é validado (RN17) contra
 * este registro, que passa a ser a fonte da verdade provisória.
 */
@RestController
@RequestMapping("/api/v1/sicim/managing-units")
@Tag(name = "sicim", description = "SICIM — órgãos gestores (registro local provisório)")
@SecurityRequirement(name = "bearer-jwt")
public class ManagingUnitController {

	private final RegisterManagingUnitUseCase registerManagingUnit;
	private final ListManagingUnitsUseCase listManagingUnits;
	private final GetManagingUnitUseCase getManagingUnit;
	private final DeactivateManagingUnitUseCase deactivateManagingUnit;

	public ManagingUnitController(RegisterManagingUnitUseCase registerManagingUnit,
			ListManagingUnitsUseCase listManagingUnits, GetManagingUnitUseCase getManagingUnit,
			DeactivateManagingUnitUseCase deactivateManagingUnit) {
		this.registerManagingUnit = registerManagingUnit;
		this.listManagingUnits = listManagingUnits;
		this.getManagingUnit = getManagingUnit;
		this.deactivateManagingUnit = deactivateManagingUnit;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize(SicimRoles.ADMIN_ONLY)
	@Operation(summary = "Cadastra um órgão gestor no registro local do SICIM")
	public ManagingUnitResult register(@Valid @RequestBody RegisterManagingUnitRequest request) {
		return registerManagingUnit.execute(request.toCommand());
	}

	@GetMapping
	@PreAuthorize(SicimRoles.CAN_READ)
	@Operation(summary = "Lista órgãos gestores ativos")
	public List<ManagingUnitResult> list() {
		return listManagingUnits.execute();
	}

	@GetMapping("/{id}")
	@PreAuthorize(SicimRoles.CAN_READ)
	@Operation(summary = "Detalha um órgão gestor (inclusive desativado)")
	public ManagingUnitResult get(@PathVariable UUID id) {
		return getManagingUnit.execute(id);
	}

	@PatchMapping("/{id}/deactivate")
	@PreAuthorize(SicimRoles.ADMIN_ONLY)
	@Operation(summary = "Desativa um órgão gestor (soft-delete: sem DELETE físico)")
	public ManagingUnitResult deactivate(@PathVariable UUID id) {
		return deactivateManagingUnit.execute(id);
	}
}
