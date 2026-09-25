package br.gov.crateus.bcm.sicim.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.gov.crateus.bcm.sicim.application.result.ManagingUnitResult;
import br.gov.crateus.bcm.sicim.application.result.PageResult;
import br.gov.crateus.bcm.sicim.application.usecase.ApprovePropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.DeactivateManagingUnitUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.DeactivatePropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.GetManagingUnitUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.GetPropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.ListManagingUnitsUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.ListPropertiesUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.ListPropertyHistoryUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.RecalculateDepreciationUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.RegisterManagingUnitUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.RegisterPropertyUseCase;
import br.gov.crateus.bcm.sicim.application.usecase.UpdatePropertyUseCase;
import br.gov.crateus.bcm.sicim.domain.ManagingUnitType;
import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Matriz role × ação exigida pela documentação (200/201 vs 403), com JWT simulado.
 *
 * <pre>
 * Ação                         ADMIN  APPROVER  REGISTRAR  VIEWER
 * Listar / detalhar            sim    sim       sim        sim
 * Cadastrar / editar           sim    não       sim        não
 * Aprovar / desativar          sim    sim       não        não
 * Recalcular depreciação       sim    não       não        não
 * Histórico (auditoria)        sim    não       não        não
 * </pre>
 */
@WebMvcTest(controllers = {PropertyController.class, PropertyHistoryController.class, ManagingUnitController.class})
@Import(TestSecurityConfig.class)
class SicimAuthorizationMatrixTest {

	private static final String BASE = "/api/v1/sicim";
	private static final UUID ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final List<String> ROLES = List.of("SICIM_ADMIN", "SICIM_APPROVER", "SICIM_REGISTRAR", "SICIM_VIEWER");

	private static final String VALID_BODY = """
			{
			  "registrationNumber": "MAT-2024-00001",
			  "notaryOffice": "Cartório do 1º Ofício de Crateús",
			  "notarialDescription": "Terreno com edificação",
			  "address": { "street": "Rua Coronel Zezé", "number": "100", "neighborhood": "Centro", "zipCode": "63700-000" },
			  "totalArea": 500.0,
			  "builtArea": 200.0,
			  "latitude": -5.1783,
			  "longitude": -40.6775,
			  "managingUnitId": "11111111-1111-1111-1111-111111111111",
			  "usageCategory": "EDUCATIONAL",
			  "possessionType": "OWNED",
			  "acquisitionYear": 2016,
			  "originalValue": 100000.0,
			  "publicPurpose": "Escola municipal"
			}
			""";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private RegisterPropertyUseCase registerProperty;
	@MockitoBean
	private UpdatePropertyUseCase updateProperty;
	@MockitoBean
	private ApprovePropertyUseCase approveProperty;
	@MockitoBean
	private DeactivatePropertyUseCase deactivateProperty;
	@MockitoBean
	private RecalculateDepreciationUseCase recalculateDepreciation;
	@MockitoBean
	private GetPropertyUseCase getProperty;
	@MockitoBean
	private ListPropertiesUseCase listProperties;
	@MockitoBean
	private ListPropertyHistoryUseCase listHistory;
	@MockitoBean
	private RegisterManagingUnitUseCase registerManagingUnit;
	@MockitoBean
	private ListManagingUnitsUseCase listManagingUnits;
	@MockitoBean
	private GetManagingUnitUseCase getManagingUnit;
	@MockitoBean
	private DeactivateManagingUnitUseCase deactivateManagingUnit;

	private static final String MANAGING_UNIT_BODY = """
			{ "name": "Secretaria de Educação", "acronym": "SEDUC", "type": "SECRETARIAT" }
			""";

	@BeforeEach
	void stubs() {
		when(listProperties.execute(any())).thenReturn(new PageResult<>(List.of(), 0, 1, 20));
		when(listProperties.customCategoryNames()).thenReturn(List.of());
		when(listHistory.execute(any())).thenReturn(new PageResult<>(List.of(), 0, 1, 20));
		when(listManagingUnits.execute()).thenReturn(List.of());
		when(registerManagingUnit.execute(any())).thenReturn(new ManagingUnitResult(ID, "Secretaria de Educação",
				"SEDUC", ManagingUnitType.SECRETARIAT, "ACTIVE", OffsetDateTime.now()));
		when(deactivateManagingUnit.execute(any())).thenReturn(new ManagingUnitResult(ID, "Secretaria de Educação",
				"SEDUC", ManagingUnitType.SECRETARIAT, "INACTIVE", OffsetDateTime.now()));
	}

	record Case(String name, MockHttpServletRequestBuilder request, int okStatus, Set<String> allowed) {
		@Override
		public String toString() {
			return name;
		}
	}

	static Stream<Case> cases() {
		Set<String> all = Set.copyOf(ROLES);
		return Stream.of(
				new Case("list", get(BASE + "/properties"), 200, all),
				new Case("custom-categories", get(BASE + "/properties/custom-categories"), 200, all),
				new Case("get", get(BASE + "/properties/" + ID), 200, all),
				new Case("register", post(BASE + "/properties").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY),
						201, Set.of("SICIM_ADMIN", "SICIM_REGISTRAR")),
				new Case("update", patch(BASE + "/properties/" + ID).contentType(MediaType.APPLICATION_JSON)
						.content("{\"publicPurpose\":\"Posto de saúde\"}"), 200, Set.of("SICIM_ADMIN", "SICIM_REGISTRAR")),
				new Case("approve", patch(BASE + "/properties/" + ID + "/approve"), 200,
						Set.of("SICIM_ADMIN", "SICIM_APPROVER")),
				new Case("deactivate", patch(BASE + "/properties/" + ID + "/deactivate"), 200,
						Set.of("SICIM_ADMIN", "SICIM_APPROVER")),
				new Case("recalculate", patch(BASE + "/properties/" + ID + "/recalculate-depreciation"), 200,
						Set.of("SICIM_ADMIN")),
				new Case("property-history", get(BASE + "/property-history"), 200, Set.of("SICIM_ADMIN")),
				new Case("history-by-property", get(BASE + "/properties/" + ID + "/history"), 200, Set.of("SICIM_ADMIN")),
				new Case("managing-unit-list", get(BASE + "/managing-units"), 200, all),
				new Case("managing-unit-register", post(BASE + "/managing-units").contentType(MediaType.APPLICATION_JSON)
						.content(MANAGING_UNIT_BODY), 201, Set.of("SICIM_ADMIN")),
				new Case("managing-unit-deactivate", patch(BASE + "/managing-units/" + ID + "/deactivate"), 200,
						Set.of("SICIM_ADMIN")));
	}

	static Stream<Arguments> matrix() {
		return cases().flatMap(c -> ROLES.stream().map(role -> Arguments.of(c, role)));
	}

	@ParameterizedTest(name = "{0} as {1}")
	@MethodSource("matrix")
	void roleMatrix(Case c, String role) throws Exception {
		int expected = c.allowed().contains(role) ? c.okStatus() : 403;
		mvc.perform(c.request().with(jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role))))
				.andExpect(status().is(expected));
	}

	@Test
	void unauthenticatedIs401() throws Exception {
		mvc.perform(get(BASE + "/properties")).andExpect(status().isUnauthorized());
	}

	@Test
	void platformUserWithoutSicimRoleIs403() throws Exception {
		mvc.perform(get(BASE + "/properties").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void invalidBodyIsProblemDetails400() throws Exception {
		mvc.perform(post(BASE + "/properties").contentType(MediaType.APPLICATION_JSON).content("{}")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SICIM_ADMIN"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void domainNotFoundIsProblemDetails404() throws Exception {
		when(getProperty.execute(ID)).thenThrow(SicimDomainException.notFound("No property found"));
		mvc.perform(get(BASE + "/properties/" + ID)
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SICIM_VIEWER"))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("No property found"));
	}

	@Test
	void domainConflictIsProblemDetails409() throws Exception {
		when(approveProperty.execute(ID)).thenThrow(SicimDomainException.conflict("inactive"));
		mvc.perform(patch(BASE + "/properties/" + ID + "/approve")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SICIM_APPROVER"))))
				.andExpect(status().isConflict());
	}
}
