-- Coluna geoespacial do imóvel (PostGIS), derivada de latitude/longitude.
-- No-op em Postgres sem a extensão PostGIS (ex.: o postgres:16-alpine do Dev Host): o bloco DO
-- só executa o DDL dependente de "geometry" se a extensão estiver disponível no servidor, então
-- não quebra o bootRun local. Ativa automaticamente quando o Postgres (BDM ou Dev Host trocado
-- para postgis/postgis) tiver PostGIS instalado. Ver NOTA-TECNICA.md.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'postgis') THEN
        CREATE EXTENSION IF NOT EXISTS postgis;

        ALTER TABLE sicim.properties ADD COLUMN IF NOT EXISTS geom geometry(Point,4326)
            GENERATED ALWAYS AS (
                ST_SetSRID(ST_MakePoint(longitude::double precision, latitude::double precision), 4326)
            ) STORED;

        CREATE INDEX IF NOT EXISTS idx_sicim_properties_geom ON sicim.properties USING GIST (geom);
    END IF;
END $$;
