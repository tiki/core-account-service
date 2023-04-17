/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

-- -----------------------------------------------------------------------
-- ONE-TIME PASSWORD
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS otp(
    otp_hashed TEXT PRIMARY KEY,
    issued_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    email TEXT
);

-- -----------------------------------------------------------------------
-- REFRESH TOKEN
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh(
    jti UUID PRIMARY KEY,
    issued_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

-- -----------------------------------------------------------------------
-- ORG INFO
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS org_info(
    org_info_id BIGSERIAL PRIMARY KEY,
    org_id UUID NOT NULL UNIQUE,
    billing_id TEXT,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

-- -----------------------------------------------------------------------
-- APP INFO
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_info(
    app_info_id BIGSERIAL PRIMARY KEY,
    app_id UUID NOT NULL UNIQUE,
    app_name TEXT NOT NULL,
    org_info_id BIGINT REFERENCES org_info(org_info_id) NOT NULL,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

-- -----------------------------------------------------------------------
-- USER INFO
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_info(
    user_info_id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    org_info_id BIGINT REFERENCES org_info(org_info_id) NOT NULL,
    email TEXT NOT NULL UNIQUE,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

-- -----------------------------------------------------------------------
-- API KEY
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS api_key(
    api_key_id UUID PRIMARY KEY,
    secret_hash TEXT,
    app_info_id BIGINT REFERENCES app_info(app_info_id) ON DELETE CASCADE,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL
);
