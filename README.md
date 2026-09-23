# SICIM — Módulo BCM (SDK 0.1.0)

**SICIM** (Sistema de Controle de Imóveis Municipais de Crateús) portado do backend NestJS
([SICIM_BackEnd](https://github.com/MikaelDiogo/SICIM_BackEnd)) para um **módulo Java** do
Backend Central Municipal (BCM), seguindo a documentação oficial
<https://api.pontodatec.com.br/institutional/v1/docs/index.html> e o `bcm-module-sdk-0.1.0`.

> Tipo de aplicação: **Módulo** (não satélite). O front
> ([SICIM_FrontEnd](https://github.com/MikaelDiogo/SICIM_FrontEnd)) é reaproveitado como SPA fina.

## Identidade

| Item                              | Valor                       |
| --------------------------------- | --------------------------- |
| Module id / pasta Gradle / schema | `sicim`                     |
| Pacote                            | `br.gov.crateus.bcm.sicim`  |
| API                               | `/api/v1/sicim/...`         |
| SDK                               | `0.1.0`                     |

## Como rodar no Dev Host

Este repositório é a pasta do módulo. Clone-o **dentro** da pasta extraída do SDK:

```bash
cd bcm-module-sdk-0.1.0
git clone https://github.com/MikaelDiogo/<repo-sicim>.git sicim
```

`settings.gradle.kts` do SDK:

```kotlin
include("bcm-sdk-api")
include("bcm-dev-host")
include("module-skeleton")
include("sicim")
```

`bcm-dev-host/build.gradle.kts` — troque o skeleton por:

```kotlin
implementation(project(":sicim"))
```

Suba:

```bash
docker compose up -d
./gradlew :bcm-dev-host:bootRun
curl -s http://localhost:8080/api/v1/sicim/hello
./gradlew :sicim:test
```

Ver `docs/` para Keycloak, contrato da API, nota técnica e UAT.
