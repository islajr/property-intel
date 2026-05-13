-- 1. Ensure schema existence
CREATE SCHEMA IF NOT EXISTS market;

-- 2. Define the table
CREATE TABLE market.neighbourhood_snapshots(
    id VARCHAR(255) PRIMARY KEY,
    city VARCHAR(255),
    neighbourhood VARCHAR(255),
    snapshot_week DATE,
    avg_days_on_market NUMERIC(6, 1),
    computed_at TIMESTAMPTZ,
    active_listing_count INTEGER,
    new_listings_count INTEGER,
    price_reduced_count INTEGER,
    median_price_kobo DOUBLE PRECISION,
    p25 DOUBLE PRECISION,
    p75 DOUBLE PRECISION,
    P90 DOUBLE PRECISION
);

-- 3. Create index
CREATE INDEX idx_snapshots_neighbourhood_week
    ON  market.neighbourhood_snapshots(neighbourhood, snapshot_week DESC);

-- 4. Seed the table with initial data
INSERT INTO market.neighbourhood_snapshots WITH weekly_reductions AS (
    SELECT listing_id
    FROM raw_data.listing_history
    WHERE event_type = 'PRICE_CHANGE'
      AND new_value < old_value
      AND event_date > CURRENT_DATE - INTERVAL '7 days'
    GROUP BY listing_id
) SELECT
      md5(l.neighbourhood || date_trunc('week', CURRENT_DATE)::DATE::text) AS id,
      l.city,
      l.neighbourhood,
      date_trunc('week', CURRENT_DATE)::DATE AS snapshot_week,
      ROUND(AVG(EXTRACT(EPOCH FROM (l.last_health_check_at - l.first_seen_at)) / 86400.0)::NUMERIC, 1) AS avg_days_on_market,
      NOW() AS computed_at,
      COUNT(l.id) FILTER (WHERE l.listing_status = 'ACTIVE') AS active_listing_count,
      COUNT(l.id) FILTER (WHERE l.first_seen_at >= NOW() - INTERVAL '7 days') AS new_listings_count,
      COUNT(r.listing_id) AS price_reduced_count,
      percentile_cont(0.50) WITHIN GROUP (ORDER BY l.price_kobo) AS median_price_kobo,
      percentile_cont(0.25) WITHIN GROUP (ORDER BY l.price_kobo) AS p25,
      percentile_cont(0.75) WITHIN GROUP (ORDER BY l.price_kobo) AS p75,
      percentile_cont(0.90) WITHIN GROUP (ORDER BY l.price_kobo) AS p90
FROM raw_data.scraped_listings l
         LEFT JOIN weekly_reductions r ON l.id = r.listing_id
WHERE l.city IS NOT NULL
    AND l.neighbourhood IS NOT NULL
GROUP BY l.city, l.neighbourhood
ON CONFLICT (id) DO NOTHING;
