# Contrato da API — `/api/v1/sicim`

Toda rota: `Authorization: Bearer <JWT Keycloak>`, `@Tag("sicim")` + `@Operation` (ver `/swagger-ui.html`).
Erros em Problem Details (RFC 7807) com propriedade extra `message` (compatível com a SPA).
Envie `X-Correlation-Id` por **intenção de negócio**; ele é gravado no histórico do imóvel.

## Mapeamento NestJS → módulo BCM

| NestJS (`:3000`)                              | Módulo BCM                                                   | Roles                          |
| --------------------------------------------- | ------------------------------------------------------------ | ------------------------------ |
| `POST /auth/login`                            | **removido** — login no Keycloak (OIDC/PKCE)                 | —                              |
| `POST /users`, tabela `users` (bcrypt)        | **removido** — usuários e perfis no Keycloak                 | —                              |
| —                                             | `GET /api/v1/sicim/me` (id, username, roles do JWT)          | autenticado                    |
| `GET/POST /managing-units`                    | `GET/POST /api/v1/sicim/managing-units` (registro local **provisório**, RN19 — ver `NOTA-TECNICA.md` item 2) | GET: todas SICIM_*; POST/desativar: ADMIN |
| —                                             | `PATCH /api/v1/sicim/managing-units/{id}/deactivate`         | ADMIN                           |
| `POST /properties`                            | `POST /api/v1/sicim/properties`                              | ADMIN, REGISTRAR               |
| `GET /properties`                             | `GET /api/v1/sicim/properties`                               | todas SICIM_*                  |
| `GET /properties/custom-categories`           | `GET /api/v1/sicim/properties/custom-categories`             | todas SICIM_*                  |
| `GET /properties/:id`                         | `GET /api/v1/sicim/properties/{id}`                          | todas SICIM_*                  |
| `PATCH /properties/:id`                       | `PATCH /api/v1/sicim/properties/{id}`                        | ADMIN, REGISTRAR               |
| `PATCH /properties/:id/approve`               | `PATCH /api/v1/sicim/properties/{id}/approve`                | ADMIN, APPROVER                |
| `PATCH /properties/:id/deactivate`            | `PATCH /api/v1/sicim/properties/{id}/deactivate`             | ADMIN, APPROVER                |
| `PATCH /properties/:id/recalculate-depreciation` | `PATCH /api/v1/sicim/properties/{id}/recalculate-depreciation` | ADMIN                     |
| `GET /audit-logs`                             | `GET /api/v1/sicim/property-history` (`propertyId`, `userId`, `action`) | ADMIN               |
| —                                             | `GET /api/v1/sicim/properties/{id}/history`                  | ADMIN                          |
| —                                             | `GET /api/v1/sicim/hello` (smoke)                            | autenticado                    |

## Listagem

`GET /properties?status=&usageCategory=&managingUnitId=&acquisitionYearFrom=&acquisitionYearTo=&page=1&pageSize=20`

Resposta (mesmo formato do NestJS): `{ "data": [...], "total": 0, "page": 1, "pageSize": 20 }`.
Ordenação: `createdAt` desc. `pageSize` máximo 100.

## Corpo do imóvel

Mesmos campos do `RegisterPropertyDto` do NestJS. Novos/alterados:

- `address.neighborhoodId` (UUID, opcional) — bairro da plataforma (geography).
- Resposta inclui `approvedById`, `approvedAt`, `lifecycleStatus`, `version`.
- `createdById` passa a ser o `sub` do JWT (string), não mais UUID da tabela `users`.

## Eventos (outbox, mesma transação)

| eventType                          | Quando                         |
| ---------------------------------- | ------------------------------ |
| `PropertyRegistered`               | cadastro                        |
| `PropertyUpdated`                  | edição                          |
| `PropertyApproved`                 | aprovação                       |
| `PropertyDeactivated`              | desativação                     |
| `PropertyDepreciationRecalculated` | recálculo de depreciação        |

Aggregate `Property`; routing key `Property.<eventType>`. Payload:
`{ propertyId, registrationNumber, managingUnitId, status, action }`.
