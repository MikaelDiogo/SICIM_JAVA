#!/usr/bin/env bash
# Cria as realm roles SICIM_* e os usuários de teste no Keycloak DEV do SDK (realm bcm-sdk).
# USO LOCAL APENAS. Nunca aponte para o Keycloak institucional.
set -euo pipefail

KC="${KC_URL:-http://localhost:8180}"
REALM="bcm-sdk"

ADMIN_TOKEN=$(curl -sf -X POST "$KC/realms/master/protocol/openid-connect/token" \
  -d client_id=admin-cli -d grant_type=password -d username=admin -d password=admin \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")
AUTH=(-H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json")

create_role() {
  local code
  code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$KC/admin/realms/$REALM/roles" "${AUTH[@]}" \
    -d "{\"name\":\"$1\",\"description\":\"$2\"}")
  echo "role $1 -> HTTP $code (201 criado, 409 já existia)"
}

create_user() {
  local user=$1 role=$2 id role_json
  curl -s -o /dev/null -X POST "$KC/admin/realms/$REALM/users" "${AUTH[@]}" \
    -d "{\"username\":\"$user\",\"enabled\":true,\"emailVerified\":true,\"firstName\":\"$user\",\"lastName\":\"DEV\",\"email\":\"$user@dev.local\",\"credentials\":[{\"type\":\"password\",\"value\":\"$user\",\"temporary\":false}]}"
  id=$(curl -s "$KC/admin/realms/$REALM/users?username=$user&exact=true" "${AUTH[@]}" \
    | python3 -c "import sys,json; print(json.load(sys.stdin)[0]['id'])")
  role_json=$(curl -s "$KC/admin/realms/$REALM/roles/$role" "${AUTH[@]}")
  curl -s -o /dev/null -X POST "$KC/admin/realms/$REALM/users/$id/role-mappings/realm" "${AUTH[@]}" -d "[$role_json]"
  echo "user $user -> $role"
}

create_role SICIM_ADMIN     "SICIM: administração total"
create_role SICIM_APPROVER  "SICIM: aprova e desativa imóveis"
create_role SICIM_REGISTRAR "SICIM: cadastra e edita imóveis"
create_role SICIM_VIEWER    "SICIM: somente consulta"

create_user sicim-admin     SICIM_ADMIN
create_user sicim-aprovador SICIM_APPROVER
create_user sicim-cadastro  SICIM_REGISTRAR
create_user sicim-consulta  SICIM_VIEWER
