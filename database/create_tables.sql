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
-- APP INFO
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_info(
    app_info_id BIGSERIAL PRIMARY KEY,
    app_id UUID NOT NULL UNIQUE,
    app_name TEXT NOT NULL,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);


-- -----------------------------------------------------------------------
-- USER INFO
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_info(
    user_info_id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

-- -----------------------------------------------------------------------
-- APP_USER JOIN TABLE
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_user(
    app_user_id BIGSERIAL PRIMARY KEY,
    app_info_id BIGINT REFERENCES app_info(app_info_id) NOT NULL,
    user_info_id BIGINT REFERENCES user_info(user_info_id) NOT NULL,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    UNIQUE(app_info_id, user_info_id)
);

-- -----------------------------------------------------------------------
-- API KEY
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS api_key(
    api_key_id UUID PRIMARY KEY,
    secret_hash BYTEA,
    app_info_id BIGINT REFERENCES app_info(app_info_id),
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL
);
