/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

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
    email TEXT NOT NULL
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
-- ORGANIZATION
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS org(
    id BIGSERIAL PRIMARY KEY,
    org_id UUID NOT NULL UNIQUE,
    billing_id TEXT,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

-- -----------------------------------------------------------------------
-- DATA PROVIDER
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS provider(
    id BIGSERIAL PRIMARY KEY,
    provider_id UUID NOT NULL UNIQUE,
    name TEXT NOT NULL,
    org_id BIGINT REFERENCES org(id) NOT NULL,
    pub_key TEXT NOT NULL UNIQUE,
    sign_key BYTEA NOT NULL UNIQUE,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

-- -----------------------------------------------------------------------
-- USER PROFILE
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS profile(
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    org_id BIGINT REFERENCES org(id) NOT NULL,
    email TEXT NOT NULL UNIQUE,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);


-- -----------------------------------------------------------------------
-- API KEY
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS api_key(
    id BIGSERIAL PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    label TEXT,
    profile_id BIGINT REFERENCES profile(id) ON DELETE CASCADE,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL
);


-- -----------------------------------------------------------------------
-- CONFIRM
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS confirm(
    id BIGSERIAL PRIMARY KEY,
    token TEXT UNIQUE NOT NULL,
    action TEXT NOT NULL,
    properties TEXT,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

-- -----------------------------------------------------------------------
-- DATA SUBSCRIPTIONS
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS subscription(
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT REFERENCES profile(id) NOT NULL,
    query TEXT NOT NULL,
    status TEXT NOT NULL,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

-- -----------------------------------------------------------------------
-- OCEAN QUERIES
-- -----------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ocean(
    id BIGSERIAL PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE,
--     subscription_id BIGINT REFERENCES subscription(id) NOT NULL,
    status TEXT NOT NULL,
    type TEXT NOT NULL,
    execution_arn TEXT NOT NULL,
    result_uri TEXT,
    result TEXT,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);


-- -----------------------------------------------------------------------
-- DATA PROVIDER - USER
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS provider_user(
    id BIGSERIAL PRIMARY KEY,
    address bytea NOT NULL,
    custom_id TEXT NOT NULL,
    public_key BYTEA NOT NULL UNIQUE,
    provider_id BIGINT REFERENCES provider(id) NOT NULL,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (provider_id, address)
);
CREATE INDEX ON provider_user (provider_id, custom_id);
CREATE INDEX ON provider_user (provider_id, address);
