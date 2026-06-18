-- 1. CREATE INDEX ON USERS.EMAIL FIELD
CREATE INDEX IF NOT EXISTS idx_users_email ON auths.users(email);

-- 2. CREATE INDEX ON NEIGHBOURHOOD_SNAPSHOTS.NEIGHBOURHOOD FIELD
CREATE INDEX IF NOT EXISTS idx_snapshots_neighbourhood ON market.neighbourhood_snapshots(neighbourhood);

-- 3. CREATE INDEX ON EMAIL_VERIFICATION_TOKENS.TOKEN_HASH FIELD
CREATE INDEX IF NOT EXISTS idx_evt_token_hash ON auths.email_verification_tokens(token_hash);

-- 4. CREATE INDEX ON EMAIL_VERIFICATION_TOKENS.USER_ID FIELD
CREATE INDEX IF NOT EXISTS idx_evt_user_id ON auths.email_verification_tokens(user_id);