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
-- USER INFO
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_info(
    uid UUID PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

-- -----------------------------------------------------------------------
-- API ID
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS api_id(
     api_id UUID PRIMARY KEY,
     user_id TEXT REFERENCES user_info(uid) NOT NULL,
     is_valid BOOLEAN NOT NULL DEFAULT FALSE,
     created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
     modified_utc TIMESTAMP WITH TIME ZONE NOT NULL,
);
