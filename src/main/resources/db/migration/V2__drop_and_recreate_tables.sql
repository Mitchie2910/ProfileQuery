-- Ensure schema exists
CREATE SCHEMA IF NOT EXISTS api;

-- Drop existing table (CASCADE handles indexes automatically)
DROP TABLE IF EXISTS api.mapping CASCADE;

-- Recreate table
CREATE TABLE api.mapping (
    id UUID PRIMARY KEY,

    name VARCHAR(255) NOT NULL UNIQUE,

    gender VARCHAR(50),

    gender_probability DOUBLE PRECISION,

    age INTEGER,

    age_group VARCHAR(50),

    country_id VARCHAR(2),

    country_name VARCHAR(255),

    country_probability DOUBLE PRECISION,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);


CREATE INDEX idx_mapping_gender ON api.mapping(gender);
CREATE INDEX idx_mapping_country_id ON api.mapping(country_id);
CREATE INDEX idx_mapping_age ON api.mapping(age);
CREATE INDEX idx_mapping_age_group ON api.mapping(age_group);