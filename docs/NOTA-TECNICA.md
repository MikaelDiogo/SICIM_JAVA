# Nota técnica de entrega — módulo SICIM

| Item               | Valor                                                              |
| ------------------ | ------------------------------------------------------------------ |
| SDK                | `bcm-module-sdk` **0.1.0** (Java 21, Spring Boot 3.5.3)            |
| Tipo               | Módulo BCM (não satélite)                                          |
| Module id / schema | `sicim`                                                            |
| Pacote             | `br.gov.crateus.bcm.sicim` (api / application / domain / infrastructure) |
| Flyway             | `db/module-migration/V20260923__sicim_schema.sql`                  |
| Origem             | Port de `MikaelDiogo/SICIM_BackEnd` (NestJS + TypeORM + PostGIS)   |

## Roles e claims

`SICIM_ADMIN`, `SICIM_APPROVER`, `SICIM_REGISTRAR`, `SICIM_VIEWER` (ver `KEYCLOAK-DEV.md`).
Nenhuma claim customizada exigida na v1. Autor das operações = claim `sub`.

## Env vars

| Variável | Uso | Padrão |
| -------- | --- | ------ |
| `sicim.integration.organization.base-url` | Base URL da API de organization (valida `managingUnitId` — RN17) | vazio → validação permissiva (só formato UUID) |
| `sicim.integration.geography.base-url` | Base URL da API de geography (valida `neighborhoodId` — RN18) | vazio → validação permissiva (só formato UUID) |

## O que mudou em relação ao NestJS

- Removidos `auth` e `user` (login, bcrypt, tabela `users`): identidade = Keycloak/JWT.
- Removido `managing-unit` (tabela `managing_units` e seed de secretarias): órgão é dado canônico
  da plataforma; `properties.managing_unit_id` guarda apenas o UUID, sem FK/JOIN cross-schema.
- `audit_logs` → `sicim.property_history` (append-only, JSONB antes/depois, `correlation_id`,
  sem IP por minimização LGPD) + evento de outbox em toda escrita.
- Colunas de auditoria BDM em todas as tabelas; lock otimista (`version`) retorna 409.
- Desativação = soft-delete (`status`/`lifecycle_status` = `INACTIVE`); nenhum `DELETE` físico.
- Nova regra: imóvel `INACTIVE` não pode ser aprovado nem editado (409).
- Enums TypeORM (`CREATE TYPE`) → `VARCHAR` + `CHECK`.

## Limitações e pontos para alinhar com a Seplati

1. **PostGIS** — a posição continua em `latitude`/`longitude` `NUMERIC(9,6)`, mas a coluna
   `geom geometry(Point,4326)` (+ índice GiST) já está preparada em
   `V20261015__sicim_property_geom.sql`: o bloco `DO` só roda o DDL dependente de PostGIS se a
   extensão existir no servidor, então é no-op seguro no Dev Host atual (`postgres:16-alpine`,
   sem PostGIS) e ativa sozinha quando o BDM (ou um Dev Host trocado para `postgis/postgis`)
   tiver a extensão — nenhuma ação adicional de código.
2. **Validação do órgão gestor** — a porta `ManagingUnitDirectory` (RN17) e o adapter
   `ManagingUnitDirectoryAdapter` já existem. Sem `sicim.integration.organization.base-url`
   configurada (caso do Dev Host hoje, que não expõe essa API), a validação segue permissiva
   (só formato UUID); falta a Seplati informar a base URL real da API de organization.
3. **Bairro** — `address_neighborhood` (texto) mantido por compatibilidade; `neighborhood_id`
   (UUID de geography) é opcional e agora validado (RN18) pela porta `NeighborhoodDirectory` /
   adapter `NeighborhoodDirectoryAdapter`, com o mesmo fallback permissivo do item 2 enquanto
   `sicim.integration.geography.base-url` não for configurada. Alvo: tornar o UUID a fonte da
   verdade.
4. **Entidade base** — o módulo espelha as colunas de `SdkAuditableEntity` em `SicimAuditableEntity`
   (o dev-host depende do módulo; depender de volta criaria ciclo). A Seplati pode trocar por
   `BaseAuditableEntity` na portabilidade sem mudar o schema.
5. **Nomes das roles** — proposta do time, pendente de confirmação do Coordenador.
6. **Dados legados** — não há migração de dados do banco NestJS; se necessário, carga com
   `source = 'MIGRATION'` a ser combinada.

## UAT (Dev Host)

| # | Cenário                                                            | Esperado            |
| - | ------------------------------------------------------------------ | ------------------- |
| 1 | `GET /api/v1/sicim/hello`                                          | 200 `{module:sicim}` |
| 2 | Flyway: `\dt sicim.*` mostra `properties` e `property_history`     | ok                  |
| 3 | `sicim-cadastro` cadastra imóvel OWNED                             | 201 PENDING_APPROVAL |
| 4 | Mesmo número de matrícula de novo                                  | 409                 |
| 5 | RENTED sem contrato / área construída > total / fora de Crateús / órgão gestor ou bairro inexistente (com integração configurada) | 400 |
| 6 | `sicim-consulta` tenta cadastrar                                   | 403                 |
| 7 | `sicim-aprovador` aprova                                           | 200 APPROVED        |
| 8 | `sicim-aprovador` desativa; depois tenta aprovar                   | 200 / 409           |
| 9 | `sicim-admin` recalcula depreciação                                | 200, valores atualizados |
| 10| `sicim-admin` consulta `/property-history?propertyId=`             | entradas CREATE, APPROVE, ... com `created_by` = sub |
| 11| `select * from sdk.outbox_event`                        | um evento por escrita |
| 12| Duas edições concorrentes do mesmo imóvel                          | segunda recebe 409  |

Status de execução: preencher na entrega (data, responsável, resultado).

Testes automatizados: `./gradlew :sicim:test` — regras de domínio, casos de uso (409/404/400,
histórico e outbox) e matriz role × ação com JWT simulado (200/201 vs 403, 401 sem token).
