-- Só notarial_description, address_zip_code, latitude/longitude e managing_unit_id continuam
-- obrigatórios no cadastro (ver REGRAS.md — decisão de negócio: os demais campos podem ser
-- completados depois via PATCH, inclusive após a aprovação). As CHECK constraints existentes
-- (ck_sicim_properties_areas/values/usage/possession) já toleram NULL corretamente em Postgres
-- (uma expressão que avalia NULL não viola a constraint), então não precisam ser reescritas.
ALTER TABLE sicim.properties
    ALTER COLUMN registration_number DROP NOT NULL,
    ALTER COLUMN notary_office DROP NOT NULL,
    ALTER COLUMN address_street DROP NOT NULL,
    ALTER COLUMN address_number DROP NOT NULL,
    ALTER COLUMN address_neighborhood DROP NOT NULL,
    ALTER COLUMN total_area DROP NOT NULL,
    ALTER COLUMN built_area DROP NOT NULL,
    ALTER COLUMN usage_category DROP NOT NULL,
    ALTER COLUMN possession_type DROP NOT NULL,
    ALTER COLUMN acquisition_year DROP NOT NULL,
    ALTER COLUMN original_value DROP NOT NULL,
    ALTER COLUMN public_purpose DROP NOT NULL;
