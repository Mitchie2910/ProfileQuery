CREATE SCHEMA IF NOT EXISTS api;

CREATE TABLE api.mapping (
    id UUID PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    gender VARCHAR(50),

    gender_probability DOUBLE PRECISION,

    sample_size BIGINT,

    age INTEGER,
    age_group VARCHAR(50),

    country_id VARCHAR(10),

    -- 2 decimal places enforced
    country_probability NUMERIC(10, 2),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_mapping_gender ON api.mapping(gender);
CREATE INDEX idx_mapping_country_id ON api.mapping(country_id);
CREATE INDEX idx_mapping_age ON api.mapping(age);
CREATE INDEX idx_mapping_age_group ON api.mapping(age_group);