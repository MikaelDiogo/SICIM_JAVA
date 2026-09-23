# SICIM — Documento de Contexto

> Leia este documento antes de abrir o código. Ele responde **o que** é o sistema, **por que** ele
> existe e **onde** ele se encaixa na plataforma da Prefeitura de Crateús.

## 1. O que é o SICIM

O **SICIM (Sistema de Controle de Imóveis Municipais de Crateús)** controla o patrimônio imobiliário
do município: cada terreno, prédio ou espaço que a Prefeitura possui ou ocupa (próprio, alugado,
cedido, em comodato, usufruto ou permissão de uso).

Para cada imóvel o sistema guarda:

- **identificação jurídica**: matrícula (`MAT-YYYY-NNNNN`), cartório e descrição cartorial;
- **localização**: endereço, CEP e coordenada geográfica dentro dos limites de Crateús;
- **características físicas**: área total e área construída;
- **uso público**: categoria (administrativo, educação, saúde, assistência social, cultura, outro)
  e finalidade pública;
- **posse**: tipo de posse e, quando não é próprio, o contrato (vigência, valores, cedente/locador,
  processo administrativo);
- **valor contábil**: valor original, ano de aquisição, depreciação acumulada e valor líquido;
- **ciclo de aprovação**: quem cadastrou, quem aprovou, quando, e se o imóvel está ativo.

## 2. Por que ele existe

- Dar à gestão municipal uma visão única e georreferenciada do patrimônio imobiliário.
- Garantir que nenhum imóvel entre no cadastro oficial sem **aprovação** de um responsável.
- Controlar contratos de imóveis de terceiros (aluguéis e cessões), que geram despesa.
- Calcular a **depreciação** conforme a política contábil do município.
- Manter **trilha de auditoria** de toda alteração (LGPD e controle interno/externo).

## 3. Histórico do projeto

| Fase | Descrição |
| ---- | --------- |
| v0 — NestJS | Backend próprio em NestJS + TypeORM + PostGIS, com login próprio (bcrypt), tabelas de usuários e de órgãos gestores. Front em React + Mantine. Publicado em Render/Vercel para apresentação. |
| v1 — Módulo BCM (este repositório) | O backend é portado para um **módulo Java** dentro do BCM, seguindo o SDK 0.1.0 e a documentação da Seplati, no mesmo modelo usado no SAGED. O front é reaproveitado como SPA fina. |

Repositórios de origem: `MikaelDiogo/SICIM_BackEnd` (NestJS) e `MikaelDiogo/SICIM_FrontEnd` (React).

## 4. Onde o SICIM se encaixa na plataforma

```
            ┌───────────────────────── Keycloak (IdP) ─────────────────────────┐
            │  emite JWT com realm roles SICIM_*                               │
            └──────────────┬───────────────────────────────────────────────────┘
                           │ Bearer JWT
┌──────────────┐   HTTPS   ▼
│ SPA SICIM    │ ───────▶ ┌──────────────────────── BCM (monólito Spring) ───────────────────────┐
│ React+Mantine│          │  identity · organization · geography · saged · **sicim** · ...       │
│ <pvh-header> │          │                                                                        │
└──────────────┘          │  sicim: api → application → domain ← infrastructure                   │
                          │                     │                                                  │
                          │                     ├── OutboxRecorder ──▶ shared.outbox ──▶ RabbitMQ  │
                          └─────────────────────┼──────────────────────────────────────────────────┘
                                                ▼
                                    BDM (PostgreSQL/PostGIS) — schema `sicim`
```

- **BCM (Backend Central Municipal)** — monólito modular Spring Boot; único componente que acessa o banco.
- **BDM** — PostgreSQL/PostGIS central. O SICIM ocupa **somente** o schema `sicim`.
- **SICIM é um Módulo**, não um satélite: é código Java plugado dentro do BCM. No laptop do time, o
  papel do BCM é feito pelo **Dev Host** do SDK.
- **Keycloak** autentica; o SICIM apenas autoriza (roles do JWT).
- **Outbox/RabbitMQ** — cada alteração de imóvel vira um evento (`Property.PropertyApproved` etc.)
  que outros domínios podem consumir.

## 5. Dados que o SICIM **não** possui

| Dado | Dono | Como o SICIM referencia |
| ---- | ---- | ----------------------- |
| Usuário / perfil | Keycloak + identity | claim `sub` do JWT (`created_by`, `approved_by`) |
| Órgão gestor (secretaria) | organization | `managing_unit_id` (UUID) |
| Bairro | geography | `neighborhood_id` (UUID, opcional na v1) |

Regra: o SICIM guarda apenas o **UUID**; nunca copia o nome da secretaria como fonte da verdade e
nunca faz JOIN com outro schema.

## 6. Atores e perfis

| Perfil (realm role) | Quem é na prática | Pode |
| ------------------- | ----------------- | ---- |
| `SICIM_VIEWER`      | Consulta (controle interno, secretarias) | listar e detalhar |
| `SICIM_REGISTRAR`   | Setor de patrimônio | + cadastrar e editar |
| `SICIM_APPROVER`    | Gestor/responsável pelo patrimônio | consultar, aprovar e desativar |
| `SICIM_ADMIN`       | Administração do sistema | tudo, incluindo depreciação e auditoria |

## 7. Ciclo de vida de um imóvel

```
  cadastro            aprovação              desativação
 ─────────▶ PENDING_APPROVAL ─────────▶ APPROVED ─────────▶ INACTIVE
                  │                                           ▲
                  └────────────── desativação ────────────────┘

 INACTIVE: não pode ser aprovado nem editado (409). Nunca é apagado fisicamente.
```

`DRAFT` existe no enum por compatibilidade com o NestJS, mas nenhum fluxo da v1 o utiliza.

## 8. Glossário

| Termo (PT)             | Código (EN)                    | Significado |
| ---------------------- | ------------------------------ | ----------- |
| Imóvel                 | `Property`                     | Agregado principal |
| Matrícula              | `registrationNumber`           | `MAT-YYYY-NNNNN`, única |
| Cartório               | `notaryOffice`                 | Cartório de registro |
| Descrição cartorial    | `notarialDescription`          | Texto da matrícula |
| Órgão gestor           | `managingUnitId`               | Secretaria responsável (UUID da plataforma) |
| Unidade orçamentária   | `budgetUnit`                   | Código orçamentário |
| Categoria de uso       | `usageCategory`                | Define a taxa de depreciação |
| Tipo de posse          | `possessionType`               | Próprio, alugado, cedido, comodato, usufruto, permissão de uso |
| Contrato de posse      | `possessionContract`           | Obrigatório se a posse não é própria |
| Valor original         | `originalValue`                | Valor de aquisição |
| Depreciação acumulada  | `accumulatedDepreciation`      | Valor original × taxa anual × anos, limitada ao valor original |
| Valor líquido contábil | `netBookValue`                 | Original − depreciação, nunca negativo |
| Histórico              | `property_history`             | Auditoria append-only com antes/depois |
| Ciclo de vida          | `lifecycle_status`             | Soft-delete padrão BDM |

## 9. Taxas de depreciação vigentes

| Categoria | Taxa anual |
| --------- | ---------- |
| ADMINISTRATIVE | 2,0% |
| EDUCATIONAL | 2,0% |
| HEALTH | 2,5% |
| SOCIAL_ASSISTANCE | 2,0% |
| CULTURAL | 1,5% |
| OTHER | 2,0% |

Fonte no código: `domain/DepreciationCalculator.ANNUAL_RATE`. Alterar a política = alterar só ali.

## 10. Documentos relacionados

- `REGRAS.md` — regras obrigatórias (plataforma, arquitetura, código, Git).
- `CONSTRUCAO.md` — como o módulo foi construído e como evoluí-lo.
- `API.md` — contrato HTTP. `KEYCLOAK-DEV.md` — roles. `FRONT-MIGRACAO.md` — SPA.
- `NOTA-TECNICA.md` — nota de entrega para a Seplati.
- Documentação oficial: <https://api.pontodatec.com.br/institutional/v1/docs/index.html>
