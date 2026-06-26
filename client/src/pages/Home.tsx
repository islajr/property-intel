import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Search, ArrowRight, TrendingDown, Clock, BarChart3 } from 'lucide-react';
import { listings, market } from '../api';
import ListingCard from '../components/listing/ListingCard';
import Skeleton from '../components/primitives/Skeleton';
import Badge from '../components/primitives/Badge';

export default function Home() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');

  // Fetch recent listings
  const { data: recentData, isLoading: recentLoading } = useQuery({
    queryKey: ['listings', 'recent'],
    queryFn: () => listings.search({ sort: 'newest', limit: 6 }),
  });

  // Fetch neighbourhoods
  const { data: neighbourhoodsData, isLoading: neighbourhoodsLoading } = useQuery({
    queryKey: ['market', 'neighbourhoods'],
    queryFn: () => market.getNeighbourhoods({ limit: 50 }),
  });

  // Fetch market overview stats (defaults to Ajah / general Lagos benchmark)
  const { data: statsData, isLoading: statsLoading } = useQuery({
    queryKey: ['market', 'stats', 'Ajah'],
    queryFn: () => market.getStats('Ajah'),
  });

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/listings?q=${encodeURIComponent(searchQuery.trim())}`);
    } else {
      navigate('/listings');
    }
  };

  // Compute total active listings
  const totalActiveListings = neighbourhoodsData?.data
    ? neighbourhoodsData.data.reduce((acc, curr) => acc + curr.activeListingCount, 0)
    : 14280; // Fallback to spec default if loading/empty

  // Get top 6 trending neighbourhoods by active listings count
  const trendingNeighbourhoods = neighbourhoodsData?.data
    ? [...neighbourhoodsData.data]
        .sort((a, b) => b.activeListingCount - a.activeListingCount)
        .slice(0, 6)
    : [];

  return (
    <div className="home-page-container">
      {/* Hero Strip */}
      <section className="hero-strip">
        <div className="hero-content container">
          <div className="hero-text-block">
            <h1 className="hero-title text-3xl">
              Real-time property market data for the Nigerian residential market.
            </h1>
            <p className="hero-subtitle text-base">
              Lagos <span className="text-tertiary">·</span>{' '}
              <span className="font-numeric">{totalActiveListings}</span> active listings{' '}
              <span className="text-tertiary">·</span> Updated today
            </p>
          </div>

          <form onSubmit={handleSearchSubmit} className="hero-search-form">
            <div className="hero-search-wrapper">
              <Search className="hero-search-icon" size={20} />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search by neighbourhood, property type..."
                className="hero-search-input"
              />
              <button type="submit" className="hero-search-btn">
                Search
              </button>
            </div>
          </form>
        </div>
      </section>

      {/* Market Snapshot Strip */}
      <section className="market-snapshot-section container">
        <h2 className="section-title text-lg">Market Snapshot (Ajah Benchmark)</h2>
        <div className="market-snapshot-grid">
          {statsLoading ? (
            <>
              <Skeleton height={100} borderRadius={12} />
              <Skeleton height={100} borderRadius={12} />
              <Skeleton height={100} borderRadius={12} />
            </>
          ) : (
            <>
              {/* Tile 1: Median Price */}
              <div className="stat-tile">
                <span className="stat-label text-sm">Median Price</span>
                <span className="stat-value text-2xl font-numeric">
                  {statsData?.formattedMedianPrice || '—'}
                </span>
                <div className="stat-comparison">
                  {statsData?.vsLastWeek?.medianPriceChangePct !== null &&
                  statsData?.vsLastWeek?.medianPriceChangePct !== undefined ? (
                    <Badge
                      variant={
                        statsData.vsLastWeek.medianPriceChangePct >= 0
                          ? 'signal-up'
                          : 'signal-down'
                      }
                      size="compact"
                    >
                      {statsData.vsLastWeek.medianPriceChangePct >= 0 ? '+' : ''}
                      {statsData.vsLastWeek.medianPriceChangePct.toFixed(1)}% WoW
                    </Badge>
                  ) : (
                    <span className="text-tertiary text-xs">—</span>
                  )}
                  <span className="text-tertiary text-xs">vs last week</span>
                </div>
              </div>

              {/* Tile 2: Price Drops */}
              <div className="stat-tile">
                <span className="stat-label text-sm">Price Drops</span>
                <div className="stat-value-with-icon">
                  <TrendingDown className="stat-tile-icon text-secondary" size={20} />
                  <span className="stat-value text-2xl font-numeric">
                    {statsData?.priceReducedCount ?? '—'}
                  </span>
                </div>
                <div className="stat-comparison">
                  <Badge variant="neutral" size="compact">
                    This Week
                  </Badge>
                  <span className="text-tertiary text-xs">reductions recorded</span>
                </div>
              </div>

              {/* Tile 3: Avg Days on Market */}
              <div className="stat-tile">
                <span className="stat-label text-sm">Avg Days on Market</span>
                <div className="stat-value-with-icon">
                  <Clock className="stat-tile-icon text-secondary" size={20} />
                  <span className="stat-value text-2xl font-numeric">
                    {statsData?.avgDaysOnMarket?.toFixed(0) || '—'}
                  </span>
                </div>
                <div className="stat-comparison">
                  {statsData?.vsLastWeek?.daysOnMarketChangePct !== null &&
                  statsData?.vsLastWeek?.daysOnMarketChangePct !== undefined ? (
                    <Badge
                      variant={
                        statsData.vsLastWeek.daysOnMarketChangePct <= 0
                          ? 'signal-up'
                          : 'signal-down'
                      }
                      size="compact"
                    >
                      {statsData.vsLastWeek.daysOnMarketChangePct >= 0 ? '+' : ''}
                      {statsData.vsLastWeek.daysOnMarketChangePct.toFixed(1)}% WoW
                    </Badge>
                  ) : (
                    <span className="text-tertiary text-xs">—</span>
                  )}
                  <span className="text-tertiary text-xs">days listing lifetime</span>
                </div>
              </div>
            </>
          )}
        </div>
      </section>

      {/* Trending Neighbourhoods */}
      <section className="trending-neighbourhoods-section container">
        <div className="section-header">
          <h2 className="section-title text-lg">Trending Neighbourhoods</h2>
          <Link to="/market" className="section-header-link text-sm">
            View all markets <ArrowRight size={14} />
          </Link>
        </div>
        
        <div className="trending-grid">
          {neighbourhoodsLoading ? (
            Array(6)
              .fill(0)
              .map((_, i) => <Skeleton key={i} height={120} borderRadius={12} />)
          ) : (
            trendingNeighbourhoods.map((n) => (
              <Link 
                key={n.neighbourhood} 
                to={`/market/${encodeURIComponent(n.neighbourhood)}`}
                className="trending-card"
              >
                <div className="trending-card-title-row">
                  <h3 className="trending-card-name text-md truncate-ellipsis">
                    {n.neighbourhood}
                  </h3>
                  <span className="trending-card-city text-xs">{n.city}</span>
                </div>
                <div className="trending-card-stats">
                  <div className="trending-stat-group">
                    <span className="trending-stat-label">Active</span>
                    <span className="trending-stat-val font-numeric">{n.activeListingCount}</span>
                  </div>
                  <div className="trending-stat-group">
                    <span className="trending-stat-label">Median</span>
                    <span className="trending-stat-val font-numeric text-amber">{n.formattedMedianPrice}</span>
                  </div>
                </div>
              </Link>
            ))
          )}
        </div>
      </section>

      {/* Recent Listings */}
      <section className="recent-listings-section container">
        <div className="section-header">
          <h2 className="section-title text-lg">Recent Listings</h2>
          <Link to="/listings" className="section-header-link text-sm">
            Browse all listings <ArrowRight size={14} />
          </Link>
        </div>

        <div className="listings-grid">
          {recentLoading ? (
            Array(6)
              .fill(0)
              .map((_, i) => <Skeleton key={i} height={200} borderRadius={12} />)
          ) : (
            recentData?.data.map((listing) => (
              <ListingCard
                key={listing.id}
                listing={listing}
                onClick={() => navigate(`/listings/${listing.id}`)}
              />
            ))
          )}
        </div>
      </section>

      {/* Callout Strip */}
      <section className="callout-strip container">
        <div className="callout-content">
          <h2 className="callout-title text-lg">Set an alert. Get notified of matching listings.</h2>
          <p className="callout-desc text-sm text-secondary">
            Save your criteria and receive email updates when new scraped property listings match your target.
          </p>
          <Link to="/alerts" className="callout-btn">
            Create Alert
          </Link>
        </div>
      </section>
    </div>
  );
}
