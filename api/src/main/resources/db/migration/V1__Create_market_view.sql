CREATE MATERIALIZED VIEW raw_data.market_snapshot_view AS WITH weekly_reductions AS (
    SELECT listing_id
    FROM raw_data.listing_history
    WHERE event_type = 'PRICE_CHANGE'
        AND new_value < old_value
        AND event_date > CURRENT_DATE - INTERVAL '7 days'
    GROUP BY listing_id
) SELECT
             md5(l.city || l.neighbourhood) AS id,
             l.city,
             l.neighbourhood,

             COUNT(l.id) FILTER (WHERE l.listing_status = 'ACTIVE') AS active_listing_count,
             COUNT(l.id) FILTER (WHERE l.first_seen_at >= NOW() - INTERVAL '7 days') AS new_listings_this_week,
             COUNT(r.listing_id) AS price_reduced_this_week,

             percentile_cont(0.50) WITHIN GROUP (ORDER BY l.price_kobo) AS median_price_kobo,
      percentile_cont(0.25) WITHIN GROUP (ORDER BY l.price_kobo) AS p25,
             percentile_cont(0.75) WITHIN GROUP (ORDER BY l.price_kobo) AS p75,
             percentile_cont(0.90) WITHIN GROUP (ORDER BY l.price_kobo) AS p90

  FROM raw_data.scraped_listings l
             LEFT JOIN weekly_reductions r ON l.id = r.listing_id
  GROUP BY l.city, l.neighbourhood;

CREATE UNIQUE INDEX idx_market_snapshots_id ON raw_data.market_snapshot_view (id);
