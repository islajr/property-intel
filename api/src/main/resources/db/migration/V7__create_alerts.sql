CREATE TABLE IF NOT EXISTS auths.alerts (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                   UUID NOT NULL REFERENCES auths.users(id) ON DELETE CASCADE,
    neighbourhood             TEXT NOT NULL,
    max_price_kobo           BIGINT,
    min_bedrooms              INTEGER,
    property_type             TEXT,
    is_active                 BOOLEAN NOT NULL DEFAULT TRUE,
    alert_unsubscribe_token   UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_alerts_user ON auths.alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_alerts_active_neighbourhood ON auths.alerts(neighbourhood) WHERE is_active = TRUE;
