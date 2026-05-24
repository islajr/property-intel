CREATE TABLE public.idempotency_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    state VARCHAR(50) NOT NULL,
    response_body TEXT,
    content_type VARCHAR(255),
    status_code INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_endpoint_idempotency_key UNIQUE (endpoint, idempotency_key)
);

CREATE INDEX idx_idempotency_key_endpoint ON public.idempotency_records(idempotency_key, endpoint);
