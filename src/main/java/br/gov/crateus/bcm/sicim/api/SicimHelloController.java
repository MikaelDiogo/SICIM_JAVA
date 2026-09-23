package br.gov.crateus.bcm.sicim.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sicim")
@Tag(name = "sicim")
public class SicimHelloController {

	@GetMapping("/hello")
	@Operation(summary = "Smoke test do módulo SICIM no Dev Host")
	public Map<String, String> hello() {
		return Map.of("module", "sicim", "status", "up");
	}
}
