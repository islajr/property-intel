CREATE SCHEMA IF NOT EXISTS raw_data;

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE raw_data.scraped_listings (
    id                      BIGINT PRIMARY KEY,
    external_id             TEXT NOT NULL,
    source                  TEXT NOT NULL,
    url                     TEXT NOT NULL,
    title                   TEXT,
    description             TEXT,
    price_kobo              BIGINT,
    price_parse_failed      BOOLEAN NOT NULL DEFAULT FALSE,
    price_type              TEXT,
    property_type           TEXT,
    bedrooms                INTEGER,
    bathrooms               INTEGER,
    floor_area_sqm          NUMERIC(10,2),
    floor_area_source       TEXT,
    raw_address             TEXT,
    neighbourhood           TEXT,
    neighbourhood_normalised BOOLEAN NOT NULL DEFAULT FALSE,
    city                    TEXT,
    lat                     DOUBLE PRECISION,
    lng                     DOUBLE PRECISION,
    geocoded                BOOLEAN NOT NULL DEFAULT FALSE,
    agent_name              TEXT,
    diaspora_targeted       BOOLEAN NOT NULL DEFAULT FALSE,
    listing_status          TEXT,
    suspected_sold          BOOLEAN NOT NULL DEFAULT FALSE,
    missed_run_count        INTEGER NOT NULL DEFAULT 0,
    first_seen_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_health_check_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    search_vector           TSVECTOR,
    UNIQUE (source, external_id)
);

CREATE TABLE raw_data.listing_history (
    id          BIGSERIAL PRIMARY KEY,
    listing_id  BIGINT NOT NULL REFERENCES raw_data.scraped_listings(id) ON DELETE CASCADE,
    old_value   BIGINT,
    new_value   BIGINT,
    event_type  TEXT NOT NULL,
    event_date  DATE NOT NULL DEFAULT CURRENT_DATE,
    notes       TEXT
);
