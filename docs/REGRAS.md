# SICIM — Regras do Projeto

> Regras **obrigatórias**. Um PR/commit que viole qualquer item marcado como **[BLOQUEANTE]** é
> rejeitado na revisão da Seplati. Fontes: documentação oficial BCM, SDK 0.1.0 e orientações da
> coordenação.

---

## 1. Regras de plataforma (BCM / BDM)

1. **[BLOQUEANTE]** O SICIM é **Módulo**, não satélite. Não criar backend paralelo (Node, PM2, outro Spring Boot).
2. **[BLOQUEANTE]** Identificadores em **inglês**: pacotes, classes, métodos, tabelas, colunas, paths. Documentação e mensagens de UI podem ser PT-BR.
   - Certo: `sicim.properties`, `/api/v1/sicim/properties`, `RegisterPropertyUseCase`
   - Errado: `sicim.imoveis`, `/api/v1/sicim/imoveis`, `CadastrarImovelService`
3. **[BLOQUEANTE]** Convenções do módulo: id `sicim`, pacote `br.gov.crateus.bcm.sicim`, schema `sicim`, API `/api/v1/sicim/...`, `@BcmBusinessModule(id = "sicim")`.
4. **[BLOQUEANTE]** DDL **somente via Flyway** em `src/main/resources/db/module-migration/`. Nunca alterar migration já publicada: criar uma nova (`V<AAAAMMDD>__descricao.sql`).
5. **[BLOQUEANTE]** Toda tabela de negócio tem as colunas de auditoria: `id`, `created_at`, `updated_at`, `created_by`, `updated_by`, `org_id`, `source`, `sensitivity`, `lifecycle_status`, `version`.
6. **[BLOQUEANTE]** Sem `JOIN` cross-schema. Dados de outro domínio (órgão, bairro, usuário) são referenciados **só por UUID**.
7. **[BLOQUEANTE]** Sem `DELETE` físico. Exclusão = `lifecycle_status` (`INACTIVE`/`DELETED`).
8. **[BLOQUEANTE]** Sem autenticação própria: nada de `POST /login`, bcrypt, tabela de senha ou cookie de sessão. Identidade vem do JWT do Keycloak.
9. **[BLOQUEANTE]** Toda rota com `@PreAuthorize` usando as roles `SICIM_*` e documentada no OpenAPI (`@Tag("sicim")` + `@Operation`).
10. Eventos de domínio pelo `OutboxRecorder`, **na mesma transação** da escrita. Nunca publicar direto no RabbitMQ.
11. Erros HTTP em Problem Details (RFC 7807): 400 validação, 403 sem permissão, 404 inexistente, 409 conflito.
12. `sensitivity` padrão `INTERNAL`; `source` = canal (`WEB`, `API`, `MIGRATION`).
13. Sem secrets no repositório (`.env`, tokens, senhas reais). O zip do SDK **não** é versionado neste repositório público.

## 2. Regras de arquitetura (Clean Architecture)

```
api  ──▶  application  ──▶  domain
                ▲
infrastructure ─┘   (implementa as portas da application)
```

| Camada | Pode depender de | Não pode depender de |
| ------ | ---------------- | -------------------- |
| `domain` | apenas JDK | Spring, JPA, Jackson, qualquer outra camada |
| `application` | `domain`, anotações `@Service`/`@Component`/`@Transactional` | `infrastructure`, `api`, JPA |
| `infrastructure` | `application` (portas), `domain`, Spring Data, SDK | `api` |
| `api` | `application`, `domain` (enums/exceções) | `infrastructure` |

Regras:

1. **[BLOQUEANTE]** `domain` não importa nada de `org.springframework`, `jakarta.persistence` ou `com.fasterxml`.
2. **[BLOQUEANTE]** `application` não importa `infrastructure`. Acesso a banco, outbox, relógio e usuário é feito por **portas** (`application/port`).
3. Regras de negócio moram no **agregado** (`Property`) ou em value objects — nunca no controller e nunca no adapter JPA.
4. Um **caso de uso por classe** (`XxxUseCase` com um método público `execute`). Transação (`@Transactional`) fica no caso de uso.
5. Controllers só fazem: validar entrada (Bean Validation) → converter DTO em comando → chamar caso de uso → devolver resultado.
6. Entidades JPA (`*Entity`) nunca saem da `infrastructure`. A tradução é feita em `PropertyPersistenceMapper`.
7. DTOs de entrada ficam em `api/dto`; comandos em `application/command`; saídas em `application/result`.

Verificação rápida:

```bash
grep -rn "org.springframework\|jakarta.persistence" src/main/java/br/gov/crateus/bcm/sicim/domain     # deve vir vazio
grep -rn "sicim.infrastructure" src/main/java/br/gov/crateus/bcm/sicim/application                      # deve vir vazio
grep -rn "sicim.infrastructure" src/main/java/br/gov/crateus/bcm/sicim/api                              # deve vir vazio
```

## 3. Regras SOLID aplicadas ao SICIM

| Princípio | Como se aplica aqui | Exemplo no código |
| --------- | ------------------- | ----------------- |
| **S** — Responsabilidade única | Cada classe tem um motivo para mudar. Casos de uso separados; efeitos colaterais (histórico + evento) isolados; conversão comando→domínio isolada. | `ApprovePropertyUseCase`, `PropertyChangeRecorder`, `PropertyCommandMapper`, `PropertyPersistenceMapper` |
| **O** — Aberto/fechado | Novos comportamentos entram como nova classe, não como `if` em classe existente. Novo evento = novo item em `PropertyEvent`; nova taxa = nova entrada no mapa. | `PropertyEventPublisher.PropertyEvent`, `DepreciationCalculator.ANNUAL_RATE` |
| **L** — Substituição de Liskov | Qualquer implementação de porta pode substituir outra sem quebrar o caso de uso (JPA em produção, memória nos testes). | `JpaPropertyRepositoryAdapter` ↔ `InMemoryPropertyRepository` |
| **I** — Segregação de interfaces | Portas pequenas e específicas; histórico só tem `append`/`findPage` (não há update/delete). | `PropertyHistoryRepository`, `TimeProvider`, `CorrelationIdProvider` |
| **D** — Inversão de dependência | Casos de uso dependem de abstrações definidas na `application`; a `infrastructure` as implementa. | `PropertyRepository` ← `JpaPropertyRepositoryAdapter`; `PropertyEventPublisher` ← `OutboxPropertyEventPublisher`; `ManagingUnitDirectory` ← `ManagingUnitDirectoryAdapter`; `NeighborhoodDirectory` ← `NeighborhoodDirectoryAdapter` |

## 4. Regras de código limpo

1. **Nomes revelam intenção**, em inglês: `requiresContract()`, `ensureCanApprove()`, `netBookValue()`. Proibido `data`, `obj`, `aux`, `temp`, `manager`, `util` genéricos.
2. **Métodos curtos**, um nível de abstração. Se precisa de comentário para explicar um bloco, extraia um método com esse nome.
3. **Sem setters públicos no domínio**. Estado muda por métodos de negócio (`approve`, `deactivate`, `update`) que revalidam as invariantes.
4. **Imutabilidade por padrão**: `record` para value objects, comandos, resultados e estado; campos `final`.
5. **Fail fast**: value objects validam no construtor (`RegistrationNumber`, `Address`, `Geolocation`, `MonetaryValue`, `PossessionContract`). Objeto inválido não existe.
6. **Dinheiro e área em `BigDecimal`**, escala 2, `RoundingMode.HALF_UP`. Nunca `double`.
7. **Datas em UTC** (`OffsetDateTime`) e sempre via `TimeProvider` — proibido `now()` direto em caso de uso.
8. **Exceções de negócio** só via `SicimDomainException.validation/conflict/notFound`. Proibido `RuntimeException` genérica para regra de negócio.
9. **Injeção por construtor**, campos `final`. Proibido `@Autowired` em campo.
10. **Sem código morto**, imports não usados, `System.out`, `printStackTrace` ou TODO sem issue.
11. **Sem duplicação**: se a mesma regra aparece em dois lugares, ela pertence ao domínio.
12. **Comentários** explicam o *porquê* (regra municipal, decisão de plataforma), nunca o *o quê*.
13. Estilo: tabs (padrão do SDK), linhas ≤ 120 colunas, um import por linha, sem wildcard.
14. **Logs sem dados pessoais** (LGPD). Correlação via `X-Correlation-Id`, não por dados do usuário.

## 5. Regras de negócio (invariantes do domínio)

| # | Regra | Onde | Erro |
| - | ----- | ---- | ---- |
| RN01 | Matrícula no formato `MAT-YYYY-NNNNN`, normalizada em maiúsculas | `RegistrationNumber` | 400 |
| RN02 | Matrícula única | `RegisterPropertyUseCase` + `UNIQUE` no banco | 409 |
| RN03 | CEP `NNNNN-NNN` ou `NNNNNNNN` | `Address` | 400 |
| RN04 | Coordenada dentro do bounding box de Crateús (lat −5,65 a −4,70; lng −41,20 a −40,10) | `Geolocation` | 400 |
| RN05 | Área total e construída positivas; construída ≤ total | `PropertyRules.validateAreas` + `CHECK` | 400 |
| RN06 | Posse diferente de `OWNED` exige contrato; contrato exige início e nº de processo; fim ≥ início | `PropertyRules`, `PossessionContract` | 400 |
| RN07 | Ano de aquisição entre 1800 e o ano corrente | `PropertyRules.validateAcquisitionYear` | 400 |
| RN08 | Valor original positivo; valores monetários nunca negativos | `Property`, `MonetaryValue` | 400 |
| RN09 | Nome de categoria personalizada só quando categoria = `OTHER` | `PropertyRules.normalizeCustomCategory` | — (descartado) |
| RN10 | Cadastro nasce `PENDING_APPROVAL` com depreciação zero | `Property.register` | — |
| RN11 | Imóvel `INACTIVE` não pode ser aprovado nem editado | `PropertyRules.ensureCanApprove/ensureCanChange` | 409 |
| RN12 | Desativação é lógica (`INACTIVE` + `lifecycle_status INACTIVE`) | `Property.deactivate` | — |
| RN13 | Depreciação linear por categoria, limitada ao valor original | `DepreciationCalculator` | — |
| RN14 | Matrícula e status não são editáveis via PATCH | `PropertyChanges` (não possui os campos) | — |
| RN15 | Toda escrita gera entrada de histórico (antes/depois) e evento de outbox na mesma transação | `PropertyChangeRecorder` | — |
| RN16 | Edição concorrente do mesmo imóvel | `version` + `JpaPropertyRepositoryAdapter` | 409 |
| RN17 | Órgão gestor (`managingUnitId`) deve existir na plataforma (organization) | `PropertyReferenceValidator` | 400 |
| RN18 | Bairro (`neighborhoodId`), quando informado, deve existir na plataforma (geography) | `PropertyReferenceValidator` | 400 |

## 6. Regras de segurança e autorização

| Ação | ADMIN | APPROVER | REGISTRAR | VIEWER |
| ---- | :---: | :------: | :-------: | :----: |
| Listar / detalhar / categorias | ✅ | ✅ | ✅ | ✅ |
| Cadastrar / editar | ✅ | ❌ | ✅ | ❌ |
| Aprovar / desativar | ✅ | ✅ | ❌ | ❌ |
| Recalcular depreciação | ✅ | ❌ | ❌ | ❌ |
| Histórico / auditoria | ✅ | ❌ | ❌ | ❌ |

- Expressões centralizadas em `SicimRoles` — não escrever strings de role soltas nos controllers.
- Autorização **sempre no backend**; o front apenas esconde botões.
- Toda nova rota exige linha nova na matriz de `SicimAuthorizationMatrixTest`.

## 7. Regras de testes

1. **[BLOQUEANTE]** `./gradlew :sicim:test` verde antes de cada push.
2. Toda regra de negócio nova → teste unitário no `domain` (sem Spring).
3. Todo caso de uso novo → teste com **fakes em memória** das portas (`application/fake`). Mockito só para a fatia web.
4. Toda rota nova → casos na matriz de autorização (feliz + 403) com `jwt()`.
5. Nome de teste descreve o comportamento: `deactivatedPropertyCannotBeApprovedOrEdited`.

## 8. Regras de Git e versionamento

1. **[BLOQUEANTE]** Repositório **público** no GitHub; o link é compartilhado com a coordenação para acompanhamento.
2. **[BLOQUEANTE]** Desenvolvimento **gradual**: cada etapa é commitada e enviada assim que concluída. Proibido desenvolver tudo fora do GitHub e publicar só no fim.
3. **[BLOQUEANTE]** Prefixos de commit (somente estes):

| Prefixo | Uso | Exemplo |
| ------- | --- | ------- |
| `feat:` | nova funcionalidade | `feat: endpoint de listagem de imóveis por órgão gestor` |
| `fix:` | correção de erro | `fix: 409 ao aprovar imóvel inativo` |
| `docs:` | documentação | `docs: roteiro de UAT preenchido` |
| `debug:` | ajustes e testes de depuração | `debug: log de correlação no adapter JPA` |

4. Mensagem no imperativo/descritiva, em PT-BR, ≤ 100 caracteres na primeira linha.
5. Um assunto por commit. Não misturar refatoração, feature e documentação.
6. Nunca reescrever histórico já publicado (`push --force` proibido na `main`).
