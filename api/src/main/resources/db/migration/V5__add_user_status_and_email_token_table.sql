-- 1. Update auths.users to accommodate user status
ALTER TABLE auths.users
    ADD COLUMN user_status TEXT
        CHECK (user_status IN ('UNVERIFIED', 'SUSPENDED', 'ACTIVE'))
        NOT NULL DEFAULT 'UNVERIFIED';

-- 2. Create e-mail verification relation
CREATE TABLE IF NOT EXISTS auths.email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL NOT NULL REFERENCES auths.users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMPTZ
)