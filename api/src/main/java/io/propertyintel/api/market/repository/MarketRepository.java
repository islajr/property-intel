package io.propertyintel.api.market.repository;

import io.propertyintel.api.market.entity.Market;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<Market, String> {

    @Query(value = """
    SELECT * FROM market.neighbourhood_snapshots
    WHERE neighbourhood = :neighbourhood
    ORDER BY snapshot_week DESC LIMIT 1
    """, nativeQuery = true)
    Optional<Market> findLatestRecordByNeighbourhood(@Param("neighbourhood") String neighbourhood);

    @Query(value = """
    SELECT DISTINCT ON (neighbourhood) * FROM market.neighbourhood_snapshots
    ORDER BY neighbourhood, snapshot_week DESC
    """, countQuery = """
    SELECT COUNT(*) FROM (
        SELECT DISTINCT ON (neighbourhood) id
        FROM market.neighbourhood_snapshots
    ) AS total_count
    """, nativeQuery = true)
    Page<Market> findMarketSummary(Pageable pageable);

    @Query(value = """
    SELECT DISTINCT ON (neighbourhood) * FROM market.neighbourhood_snapshots
        WHERE (:idAfter IS NULL OR id > :idAfter)
    ORDER BY neighbourhood, snapshot_week DESC
    """, countQuery = """
    SELECT COUNT(*) FROM (
        SELECT DISTINCT ON (neighbourhood) id
        FROM market.neighbourhood_snapshots
    ) AS total_count
    """, nativeQuery = true)
    Page<Market> findMarketSummaryWithCursor(@Param("idAfter")String idAfter, Pageable pageable);

    @Query(value = """
    SELECT * FROM market.neighbourhood_snapshots
        WHERE neighbourhood = :neighbourhood
    ORDER BY snapshot_week DESC LIMIT 12
    """, nativeQuery = true)
    List<Market> findMarketTrends(String neighbourhood);

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
          WHERE l.city IS NOT NULL
              AND l.neighbourhood IS NOT NULL
          GROUP BY l.city, l.neighbourhood
          ON CONFLICT (id) DO NOTHING
    """, nativeQuery = true)
    void refreshMarketSnapshot();
}
