-- Registro local de órgãos gestores, mantido pelo SICIM enquanto a API de organization da
-- plataforma não existe (ver NOTA-TECNICA.md item 2 — exceção documentada à regra 1.6 de
-- REGRAS.md: até a integração real, o SICIM é a fonte da verdade para managing_unit_id).
CREATE TABLE sicim.managing_units (
    id                UUID PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    acronym           VARCHAR(20)  NOT NULL,
    type              VARCHAR(32)  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL,
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255),
    org_id            UUID,
    source            VARCHAR(64),
    sensitivity       VARCHAR(32)  NOT NULL DEFAULT 'INTERNAL',
    lifecycle_status  VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    version           BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT ck_sicim_managing_units_type CHECK (type IN ('SECRETARIAT', 'AUTARCHY', 'FOUNDATION')),
    CONSTRAINT ck_sicim_managing_units_lifecycle CHECK
        (lifecycle_status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED', 'DELETED'))
);

CREATE INDEX idx_sicim_managing_units_lifecycle ON sicim.managing_units (lifecycle_status);

-- Sigla única só entre os ativos: um órgão desativado pode manter a sigla histórica sem
-- travar o cadastro de um novo órgão com a mesma sigla (ex.: secretaria extinta e recriada).
CREATE UNIQUE INDEX uq_sicim_managing_units_acronym_active ON sicim.managing_units (acronym)
    WHERE lifecycle_status = 'ACTIVE';
