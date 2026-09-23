package br.gov.crateus.bcm.sicim.api;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Esquema Bearer JWT (token emitido pelo Keycloak) referenciado pelas rotas do SICIM. */
@Configuration
@SecurityScheme(name = "bearer-jwt", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SicimOpenApiConfig {
}
