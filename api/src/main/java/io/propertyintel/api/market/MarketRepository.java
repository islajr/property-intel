package io.propertyintel.api.market;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketRepository extends JpaRepository<Market, String> {

    Market findByNeighbourhood(String neighbourhood);
    List<Market> findByCity(String city);
    List<Market> findByCityAndNeighbourhood(String city, String neighbourhood);

    @Modifying
    @Transactional
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY raw_data.market_snapshot_view", nativeQuery = true)
    void refreshMarketSnapshot();
}
