# Keycloak DEV — roles e usuários do SICIM

Realm `bcm-sdk` do Dev Host (`http://localhost:8180`, admin/admin). O converter do host transforma
`realm_access.roles` em `ROLE_*`; o módulo usa `@PreAuthorize("hasRole('SICIM_...')")`.

## Realm roles

| Realm role        | Perfil NestJS de origem | Uso                                                      |
| ----------------- | ----------------------- | -------------------------------------------------------- |
| `SICIM_ADMIN`     | `ADMINISTRATION`        | Tudo: cadastrar, aprovar, desativar, depreciação, histórico |
| `SICIM_APPROVER`  | `APPROVAL`              | Consultar, aprovar e desativar                            |
| `SICIM_REGISTRAR` | `REGISTRATION`          | Consultar, cadastrar e editar                             |
| `SICIM_VIEWER`    | `VIEWER`                | Somente consulta                                          |

> Os nomes são proposta do time. Precisam ser **confirmados com o Coordenador Seplati** antes da
> entrega, como manda o fluxo de módulo (escopo, schema e roles alinhados).

## Usuários de teste

| Username          | Senha             | Role              |
| ----------------- | ----------------- | ----------------- |
| `sicim-admin`     | `sicim-admin`     | `SICIM_ADMIN`     |
| `sicim-aprovador` | `sicim-aprovador` | `SICIM_APPROVER`  |
| `sicim-cadastro`  | `sicim-cadastro`  | `SICIM_REGISTRAR` |
| `sicim-consulta`  | `sicim-consulta`  | `SICIM_VIEWER`    |

Criação automática (com o compose do SDK no ar):

```bash
bash scripts/keycloak-dev-setup.sh
```

Ou manualmente no Admin Console: *Realm roles → Create role* e *Users → Add user → Credentials /
Role mapping*.

## Token de smoke

```bash
TOKEN=$(curl -s -X POST 'http://localhost:8180/realms/bcm-sdk/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d client_id=bcm-sdk-public -d grant_type=password \
  -d username=sicim-admin -d password=sicim-admin \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/sicim/me
```

Em produção a Seplati cria as mesmas roles no realm institucional; o time não administra o IdP real.
