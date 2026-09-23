-- SICIM — Sistema de Controle de Imóveis Municipais de Crateús
-- Módulo BCM `sicim` (SDK 0.1.0). DDL somente via Flyway.
-- Regras: identificadores em inglês; auditoria obrigatória; sem JOIN cross-schema;
-- órgão gestor e bairro referenciados por UUID de plataforma; soft-delete via lifecycle_status.

CREATE SCHEMA IF NOT EXISTS sicim;

-- Imóveis municipais
-- status: DRAFT | PENDING_APPROVAL | APPROVED | INACTIVE
-- managing_unit_id = UUID do órgão na plataforma (organization) — sem tabela local de órgãos
-- neighborhood_id  = UUID do bairro na plataforma (geography) — opcional até integração
CREATE TABLE sicim.properties (
    id                                     UUID PRIMARY KEY,
    registration_number                    VARCHAR(20)   NOT NULL UNIQUE,
    notary_office                          VARCHAR(255)  NOT NULL,
    notarial_description                   TEXT          NOT NULL,
    address_street                         VARCHAR(255)  NOT NULL,
    address_number                         VARCHAR(20)   NOT NULL,
    address_neighborhood                   VARCHAR(100)  NOT NULL,
    neighborhood_id                        UUID,
    address_zip_code                       VARCHAR(9)    NOT NULL,
    address_reference                      VARCHAR(255),
    total_area                             NUMERIC(12,2) NOT NULL,
    built_area                             NUMERIC(12,2) NOT NULL,
    latitude                               NUMERIC(9,6)  NOT NULL,
    longitude                              NUMERIC(9,6)  NOT NULL,
    managing_unit_id                       UUID          NOT NULL,
    budget_unit                            VARCHAR(100),
    usage_category                         VARCHAR(32)   NOT NULL,
    custom_category_name                   VARCHAR(100),
    possession_type                        VARCHAR(32)   NOT NULL,
    contract_start_date                    TIMESTAMPTZ,
    contract_end_date                      TIMESTAMPTZ,
    contract_monthly_value                 NUMERIC(12,2),
    contract_reference_value               NUMERIC(12,2),
    contract_grantor                       VARCHAR(255),
    contract_lessor                        VARCHAR(255),
    contract_administrative_process_number VARCHAR(100),
    acquisition_year                       INTEGER       NOT NULL,
    original_value                         NUMERIC(14,2) NOT NULL,
    accumulated_depreciation               NUMERIC(14,2) NOT NULL DEFAULT 0,
    public_purpose                         TEXT          NOT NULL,
    status                                 VARCHAR(32)   NOT NULL,
    approved_by                            VARCHAR(255),
    approved_at                            TIMESTAMPTZ,
    created_at                             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at                             TIMESTAMPTZ   NOT NULL,
    created_by                             VARCHAR(255),
    updated_by                             VARCHAR(255),
    org_id                                 UUID,
    source                                 VARCHAR(64),
    sensitivity                            VARCHAR(32)   NOT NULL DEFAULT 'INTERNAL',
    lifecycle_status                       VARCHAR(32)   NOT NULL DEFAULT 'ACTIVE',
    version                                BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT ck_sicim_properties_areas CHECK (total_area > 0 AND built_area > 0 AND built_area <= total_area),
    CONSTRAINT ck_sicim_properties_values CHECK (original_value > 0 AND accumulated_depreciation >= 0),
    CONSTRAINT ck_sicim_properties_status CHECK (status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'INACTIVE')),
    CONSTRAINT ck_sicim_properties_usage CHECK (usage_category IN
        ('ADMINISTRATIVE', 'EDUCATIONAL', 'HEALTH', 'SOCIAL_ASSISTANCE', 'CULTURAL', 'OTHER')),
    CONSTRAINT ck_sicim_properties_possession CHECK (possession_type IN
        ('OWNED', 'RENTED', 'GRANTED', 'LOAN', 'USUFRUCT', 'USE_PERMIT'))
);

CREATE INDEX idx_sicim_properties_managing_unit ON sicim.properties (managing_unit_id);
CREATE INDEX idx_sicim_properties_status        ON sicim.properties (status);
CREATE INDEX idx_sicim_properties_usage         ON sicim.properties (usage_category);
CREATE INDEX idx_sicim_properties_year          ON sicim.properties (acquisition_year);

-- Histórico append-only de alterações (substitui a tabela audit_logs do NestJS).
-- action: CREATE | UPDATE | APPROVE | DEACTIVATE | RECALCULATE_DEPRECIATION
-- Autor = created_by (sub do JWT). Sem IP de origem (minimização LGPD); correlation_id amarra ao log.
CREATE TABLE sicim.property_history (
    id               UUID PRIMARY KEY,
    property_id      UUID         NOT NULL REFERENCES sicim.properties(id),
    action           VARCHAR(64)  NOT NULL,
    data_before      JSONB,
    data_after       JSONB,
    correlation_id   VARCHAR(64),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL,
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255),
    org_id           UUID,
    source           VARCHAR(64),
    sensitivity      VARCHAR(32)  NOT NULL DEFAULT 'INTERNAL',
    lifecycle_status VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_sicim_property_history_property ON sicim.property_history (property_id);
CREATE INDEX idx_sicim_property_history_author   ON sicim.property_history (created_by);
