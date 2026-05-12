package io.propertyintel.api.market;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<Market, String> {

    Optional<Market> findByNeighbourhood(String neighbourhood);
    List<Market> findByCity(String city);
    List<Market> findByCityAndNeighbourhood(String city, String neighbourhood);

    @Modifying
    @Transactional
    @Query(value = """
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
          GROUP BY l.city, l.neighbourhood
          ON CONFLICT (id) DO UPDATE SET
                avg_days_on_market = EXCLUDED.avg_days_on_market,
                computed_at = EXCLUDED.computed_at,
                active_listing_count = EXCLUDED.active_listing_count,
                new_listings_count = EXCLUDED.new_listings_count,
                price_reduced_this_week = EXCLUDED.price_reduced_count,
                median_price_kobo = EXCLUDED.median_price_kobo,
                p25 = EXCLUDED.p25,
                p75 = EXCLUDED.p75,
                p90 = EXCLUDED.p90
    """, nativeQuery = true)
    void refreshMarketSnapshot();
}
