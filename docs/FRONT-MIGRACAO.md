# Reaproveitando o SICIM_FrontEnd como SPA do módulo

A SPA continua React + TypeScript + Vite + Mantine. O que precisa mudar para falar com o BCM:

1. **Base URL** — `VITE_API_BASE_URL=http://localhost:8080/api/v1/sicim` (DEV). Os paths
   `/properties/...` do `property.api.ts` continuam iguais depois do prefixo.
2. **Login** — remover `LoginForm` + `POST /auth/login` + token no `localStorage`. Usar OIDC contra o
   Keycloak (realm `bcm-sdk`, client público `bcm-sdk-public`, Authorization Code + PKCE), por exemplo
   com `keycloak-js` ou `oidc-client-ts`. A documentação proíbe login próprio com cookie/bcrypt.
3. **Usuário e perfil** — `AuthenticatedUser.role` vira lista de roles vindas de `GET /me`
   (`SICIM_ADMIN` | `SICIM_APPROVER` | `SICIM_REGISTRAR` | `SICIM_VIEWER`). Ajustar `nav-items.ts`
   (`roles: ['ADMINISTRATION']` → `['SICIM_ADMIN']`) e `roleLabels`.
4. **Tela de usuários** (`UsersPage`, `user.api.ts`) — remover. Gestão de usuários é no Keycloak
   (Seplati em produção).
5. **Órgãos gestores** (`managing-unit.api.ts`) — passar a consumir a API de organization da
   plataforma (`/api/v1/...`, conforme OpenAPI vigente do BCM). No Dev Host não existe essa API:
   usar lista fixa de UUIDs de teste em `.env.development` até a integração.
6. **Auditoria** — `/audit-logs` → `/property-history` (campos `userId`, `entityId`, `action`,
   `dataBefore`, `dataAfter`, `timestamp` mantidos; `sourceIp` removido, entra `correlationId`).
7. **X-Correlation-Id** — gerar `crypto.randomUUID()` por intenção de negócio (ex.: "cadastrar
   imóvel", "aprovar imóvel") e reutilizar em todas as chamadas daquele fluxo.
8. **Erros** — o corpo agora é Problem Details; `extractErrorMessage` continua funcionando porque o
   módulo envia também a propriedade `message`.
9. **Header** — substituir `AppHeader`/`Topbar` pelo `<pvh-header>` institucional
   (`https://api.pontodatec.com.br/institutional/v1/`), com `no-default-nav` e evento `pvh:navigate`.
