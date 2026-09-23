# SICIM — Documento de Construção

> Como o módulo foi construído, como está organizado e **como evoluí-lo** sem quebrar as regras
> (`REGRAS.md`). Para o "o quê" e o "porquê", veja `CONTEXTO.md`.

## 1. Pré-requisitos

| Ferramenta | Versão |
| ---------- | ------ |
| JDK | 21 |
| Docker + Docker Compose | recente |
| SDK BCM | `bcm-module-sdk-0.1.0.zip` (recebido da Seplati — **não** versionar) |
| Portas livres | 5433 (Postgres), 8180 (Keycloak), 8080 (Dev Host) |

## 2. Montando o ambiente

```bash
unzip bcm-module-sdk-0.1.0.zip && cd bcm-module-sdk-0.1.0
git clone https://github.com/MikaelDiogo/<repo-sicim>.git sicim
```

`settings.gradle.kts`:

```kotlin
include("bcm-sdk-api")
include("bcm-dev-host")
include("module-skeleton")
include("sicim")
```

`bcm-dev-host/build.gradle.kts` — trocar `implementation(project(":module-skeleton"))` por:

```kotlin
implementation(project(":sicim"))
```

Subir tudo:

```bash
docker compose up -d                     # Postgres :5433 + Keycloak :8180
bash sicim/scripts/keycloak-dev-setup.sh # roles SICIM_* e usuários de teste
./gradlew :bcm-dev-host:bootRun          # API :8080 + Flyway
```

Smoke:

```bash
curl -s http://localhost:8080/api/v1/system/ping
curl -s http://localhost:8080/api/v1/sicim/hello        # {"module":"sicim","status":"up"}
docker exec -it bcm-sdk-postgres psql -U bcm_sdk -d bcm_sdk -c '\dt sicim.*'
```

Swagger: <http://localhost:8080/swagger-ui.html>. Debug sem JWT (só local):
`BCM_SDK_JWT_ENABLED=false ./gradlew :bcm-dev-host:bootRun`.

## 3. Estrutura do módulo

```
sicim/
├── build.gradle.kts                  ← java-library; Spring como compileOnly (o host fornece)
├── docs/                             ← CONTEXTO, REGRAS, CONSTRUCAO, API, KEYCLOAK-DEV, FRONT-MIGRACAO, NOTA-TECNICA
├── scripts/keycloak-dev-setup.sh
└── src/
    ├── main/java/br/gov/crateus/bcm/sicim/
    │   ├── package-info.java         ← @BcmBusinessModule(id = "sicim")
    │   ├── domain/                   ← regras puras (JDK apenas)
    │   │   ├── Property.java         ← agregado: register, update, approve, deactivate, recalculateDepreciation
    │   │   ├── PropertyState.java    ← estado imutável do agregado
    │   │   ├── NewProperty / PropertyChanges / PropertyAudit / PropertyHistoryEntry
    │   │   ├── RegistrationNumber, Address, Geolocation, MonetaryValue, PossessionContract  ← value objects
    │   │   ├── PropertyRules, DepreciationCalculator, Text
    │   │   ├── enums: PropertyStatus, UsageCategory, PossessionType, LifecycleStatus, PropertyHistoryAction
    │   │   └── exception/SicimDomainException (+ ErrorType)
    │   ├── application/
    │   │   ├── usecase/              ← 1 classe por caso de uso, @Transactional
    │   │   ├── port/                 ← interfaces que a infraestrutura implementa
    │   │   ├── support/              ← colaboradores compartilhados entre casos de uso
    │   │   ├── command/              ← entradas (primitivos)
    │   │   ├── result/               ← saídas (JSON compatível com o front)
    │   │   └── SicimRoles.java       ← expressões de autorização
    │   ├── infrastructure/
    │   │   ├── persistence/          ← entidades JPA, Spring Data, specifications, adapters, mapper
    │   │   ├── event/                ← adapter de outbox
    │   │   ├── security/             ← usuário do JWT, correlation id
    │   │   └── time/                 ← relógio do sistema
    │   └── api/                      ← controllers REST, DTOs validados, Problem Details, OpenAPI
    ├── main/resources/db/module-migration/V20260923__sicim_schema.sql
    └── test/java/br/gov/crateus/bcm/sicim/
        ├── domain/                   ← JUnit puro
        ├── application/              ← casos de uso + fakes em memória
        └── api/                      ← @WebMvcTest + jwt() (matriz de autorização)
```

## 4. Fluxo de uma requisição (exemplo: aprovar imóvel)

```
PATCH /api/v1/sicim/properties/{id}/approve   (Bearer JWT com SICIM_APPROVER)
 │
 ├─ api/PropertyController.approve            @PreAuthorize(SicimRoles.CAN_APPROVE)
 │
 ├─ application/usecase/ApprovePropertyUseCase.execute   ── @Transactional (início)
 │    ├─ PropertyLookup.require(id)            → PropertyRepository.findById  (404 se não existir)
 │    ├─ PropertySnapshot.of(property)         → "antes"
 │    ├─ property.approve(sub, now)            → domínio valida RN11 (409 se INACTIVE)
 │    ├─ PropertyRepository.save(property)     → adapter JPA (lock otimista, RN16)
 │    └─ PropertyChangeRecorder.record(...)
 │         ├─ PropertyHistoryRepository.append → sicim.property_history
 │         └─ PropertyEventPublisher.publish   → OutboxRecorder → sdk.outbox_event / shared.outbox
 │                                                                  ── @Transactional (commit)
 └─ PropertyResult → JSON
```

Se qualquer passo falhar, **nada** é gravado: imóvel, histórico e evento são atômicos.

## 5. Como o módulo foi construído (ordem dos commits)

1. `docs:` README do módulo.
2. `feat:` projeto Gradle a partir do `module-skeleton`, `@BcmBusinessModule`, `GET /hello`.
3. `feat:` migration Flyway do schema `sicim` com auditoria BDM.
4. `feat:` value objects, enums e regras do domínio portados do NestJS.
5. `feat:` entidades JPA + repositórios Spring Data.
6. `feat:` casos de uso com histórico e outbox; `feat:` API REST com OpenAPI e Problem Details.
7. `feat:` testes (domínio, casos de uso, matriz de autorização).
8. `docs:` Keycloak, API, migração do front, nota técnica.
9. `fix:` refatoração SOLID — agregado `Property` no domínio, um caso de uso por classe, portas na
   aplicação e adapters na infraestrutura (a aplicação deixou de depender de JPA).
10. `docs:` CONTEXTO, REGRAS e este documento.

Mapeamento do código NestJS de origem:

| NestJS | Módulo Java |
| ------ | ----------- |
| `property/domain/entities/property.entity.ts` | `domain/Property` + `PropertyState` |
| `property/domain/value-objects/*.vo.ts` | `domain/RegistrationNumber`, `Address`, `Geolocation`, `MonetaryValue` |
| `property/domain/services/depreciation-calculator.ts` | `domain/DepreciationCalculator` |
| `property/domain/repositories/property.repository.ts` (`IPropertyRepository`) | `application/port/PropertyRepository` |
| `property/application/use-cases/*.use-case.ts` | `application/usecase/*UseCase` |
| `property/infrastructure/persistence/*` | `infrastructure/persistence/*` |
| `property/interface/controllers` + presenters | `api/PropertyController` + `application/result/PropertyResult` |
| `audit-log/*` | `property_history` + `ListPropertyHistoryUseCase` |
| `shared/interface/filters/domain-exception.filter.ts` | `api/SicimExceptionHandler` |
| `auth/*`, `user/*`, `managing-unit/*` | removidos (Keycloak e plataforma) |

## 6. Receitas de evolução

### 6.1 Nova regra de negócio

1. Escreva o teste em `test/.../domain` descrevendo a regra.
2. Implemente no value object ou em `PropertyRules`, chamada a partir do método do agregado.
3. Adicione a regra na tabela da seção 5 de `REGRAS.md` (RNxx).
4. Commit: `feat: <regra>`.

### 6.2 Novo caso de uso (ex.: reativar imóvel)

1. **Domínio** — método de negócio no agregado:
   ```java
   public void reactivate() {
       if (state.status() != PropertyStatus.INACTIVE) {
           throw SicimDomainException.conflict("Only inactive properties can be reactivated.");
       }
       // novo estado via copy(...)
   }
   ```
2. **Evento/histórico** — nova constante em `PropertyHistoryAction` e em `PropertyEvent`.
3. **Aplicação** — `ReactivatePropertyUseCase` seguindo o molde de `DeactivatePropertyUseCase`
   (lookup → snapshot → método do domínio → save → recorder).
4. **API** — endpoint no `PropertyController` com `@PreAuthorize` e `@Operation`.
5. **Testes** — domínio, caso de uso com fakes e linha nova na matriz de autorização.
6. **Docs** — `API.md` e a matriz de `REGRAS.md`.
7. Commits separados por passo (`feat:` código, `docs:` documentação).

### 6.3 Mudança de schema

1. Nunca editar `V20260923__sicim_schema.sql` depois de publicado.
2. Criar `V<AAAAMMDD>__<descricao>.sql` (ex.: `V20261015__sicim_property_geom.sql`).
3. Nova coluna de negócio → campo na `*Entity`, no `PropertyState` e no `PropertyPersistenceMapper`.
4. Hibernate roda com `ddl-auto: validate`: se a entidade divergir do SQL, o host não sobe.

### 6.4 Nova porta externa (ex.: validar órgão na organization)

1. Interface em `application/port` (ex.: `ManagingUnitDirectory.exists(UUID)`).
2. Implementação em `infrastructure/...` (no BCM, chamada ao código de organization no monólito).
3. Fake em `test/.../application/fake`.
4. Caso de uso depende só da interface (DIP).

## 7. Testes

```bash
./gradlew :sicim:test
```

| Suíte | O que cobre |
| ----- | ----------- |
| `domain/DomainRulesTest`, `domain/PropertyTest` | RN01–RN14 |
| `application/PropertyUseCasesTest` | orquestração, 404/409, histórico antes/depois, eventos, relógio, usuário do JWT |
| `api/SicimAuthorizationMatrixTest` | 4 roles × 10 ações (200/201 vs 403), 401 sem token, Problem Details 400/404/409 |

## 8. Entrega à Seplati

Conteúdo mínimo (documentação oficial):

- código `br.gov.crateus.bcm.sicim` + `build.gradle.kts`;
- migrations Flyway;
- SPA + instruções de build (`FRONT-MIGRACAO.md`);
- nota técnica (`NOTA-TECNICA.md`) com SDK, roles, env vars, UAT executado e limitações;
- testes de autorização e regras críticas.

Checklist antes de entregar:

- [ ] `./gradlew :sicim:test` verde
- [ ] Dev Host sobe com Flyway limpo (`docker compose down -v` + `bootRun`)
- [ ] UAT da nota técnica executado e preenchido
- [ ] Verificações de arquitetura da seção 2 de `REGRAS.md` vazias
- [ ] Sem secrets no repositório; roles confirmadas com o Coordenador
