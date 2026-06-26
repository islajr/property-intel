import { useParams, Link, useNavigate } from 'react-router-dom';
import { useQuery, useInfiniteQuery } from '@tanstack/react-query';
import { market, listings } from '../api';
import { PricePercentilesBar } from '../components/market/PricePercentilesBar';
import { TrendChart } from '../components/market/TrendChart';
import ListingCard from '../components/listing/ListingCard';
import Badge from '../components/primitives/Badge';
import Skeleton from '../components/primitives/Skeleton';
import Button from '../components/primitives/Button';
import { ArrowLeft, MapPin, AlertCircle, Home, Clock, TrendingUp } from 'lucide-react';
import { formatNaira } from '../utils/format';

export default function Neighbourhood() {
  const { name } = useParams<{ name: string }>();
  const navigate = useNavigate();

  // 1. Fetch Neighbourhood Stats
  const { 
    data: stats, 
    isLoading: statsLoading, 
    isError: statsError, 
    refetch: refetchStats 
  } = useQuery({
    queryKey: ['market', 'stats', name],
    queryFn: () => market.getStats(name!),
    enabled: !!name,
  });

  // 2. Fetch Neighbourhood Trends (12 weeks)
  const { 
    data: trendData, 
    isLoading: trendLoading, 
    isError: trendError, 
    refetch: refetchTrends 
  } = useQuery({
    queryKey: ['market', 'trends', name],
    queryFn: () => market.getTrends(name!),
    enabled: !!name,
  });

  // 3. Fetch Listings pre-filtered by this neighbourhood
  const {
    data: listingsData,
    isLoading: listingsLoading,
    isError: listingsError,
    hasNextPage,
    fetchNextPage,
    isFetchingNextPage,
    refetch: refetchListings,
  } = useInfiniteQuery({
    queryKey: ['listings', 'neighbourhood', name],
    queryFn: ({ pageParam }) =>
      listings.search({
        neighbourhood: name,
        sort: 'newest',
        limit: 6,
        cursor: pageParam,
      }),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (lastPage) =>
      lastPage.meta.has_more ? lastPage.meta.next_cursor || undefined : undefined,
    enabled: !!name,
  });

  const allListings = listingsData?.pages.flatMap((page) => page.data) || [];

  const handleRetryAll = () => {
    refetchStats();
    refetchTrends();
    refetchListings();
  };

  const isAnyLoading = statsLoading || trendLoading || listingsLoading;
  const isAnyError = statsError || trendError || listingsError;

  if (isAnyLoading && allListings.length === 0 && !stats) {
    return (
      <div className="neighbourhood-page-container container py-8">
        <div className="mb-6">
          <Skeleton width={120} height={20} />
        </div>
        <Skeleton width="40%" height={36} className="mb-6" />
        
        {/* Stats tiles loader */}
        <div className="market-summary-grid grid mb-8">
          <Skeleton height={100} />
          <Skeleton height={100} />
          <Skeleton height={100} />
        </div>
        
        <Skeleton width="100%" height={160} className="mb-6" />
        <Skeleton width="100%" height={320} className="mb-8" />
        <Skeleton width="30%" height={28} className="mb-6" />
        <div className="listings-grid grid">
          <Skeleton height={200} />
          <Skeleton height={200} />
          <Skeleton height={200} />
        </div>
      </div>
    );
  }

  if (isAnyError && !stats) {
    return (
      <div className="neighbourhood-page-container container py-12 text-center">
        <AlertCircle size={40} className="text-secondary mx-auto mb-4" />
        <h2 className="text-lg font-semibold mb-2">Unable to load data</h2>
        <p className="text-secondary mb-6">We encountered an error fetching the market statistics for this area.</p>
        <Button variant="primary" onClick={handleRetryAll}>
          Retry
        </Button>
      </div>
    );
  }

  // Resolve WoW Change Badge
  const wowChange = stats?.vsLastWeek?.medianPriceChangePct;
  const renderWoWBadge = () => {
    if (wowChange === null || wowChange === undefined) {
      return <Badge variant="neutral">—</Badge>;
    }
    const pctStr = `${wowChange > 0 ? '+' : ''}${wowChange.toFixed(1)}%`;
    if (wowChange > 0) {
      return <Badge variant="signal-up">{pctStr} WoW</Badge>;
    }
    if (wowChange < 0) {
      return <Badge variant="signal-down">{pctStr} WoW</Badge>;
    }
    return <Badge variant="neutral">0.0% WoW</Badge>;
  };

  return (
    <div className="neighbourhood-page-container container py-8">
      {/* Back Link */}
      <div className="mb-6">
        <Link to="/market" className="inline-flex items-center text-xs text-secondary hover:text-primary no-underline font-numeric">
          <ArrowLeft size={14} style={{ marginRight: 'var(--space-1)' }} />
          BACK TO MARKET
        </Link>
      </div>

      {/* Title block */}
      <header className="mb-8">
        <div className="flex items-center gap-2 text-secondary text-sm mb-2">
          <MapPin size={16} />
          <span>{stats?.city || 'Lagos'}, Nigeria</span>
        </div>
        <h1 className="page-title text-2xl font-bold tracking-tight text-primary">
          {name} Market Profile
        </h1>
      </header>

      {/* Stats header (3 tiles) */}
      {stats && (
        <section className="market-summary-grid grid mb-8">
          <div className="summary-tile bg-raised border border-default rounded-lg p-5">
            <div className="flex items-center gap-3 mb-2">
              <Home size={18} className="text-secondary" />
              <span className="tile-label text-xs text-secondary uppercase tracking-wider font-semibold">
                Active Listings
              </span>
            </div>
            <span className="tile-value text-2xl font-bold font-numeric text-primary">
              {stats.activeListingCount}
            </span>
          </div>

          <div className="summary-tile bg-raised border border-default rounded-lg p-5">
            <div className="flex items-center justify-between gap-3 mb-2">
              <div className="flex items-center gap-3">
                <TrendingUp size={18} className="text-secondary" />
                <span className="tile-label text-xs text-secondary uppercase tracking-wider font-semibold">
                  Median Price
                </span>
              </div>
              {renderWoWBadge()}
            </div>
            <span className="tile-value text-2xl font-bold font-numeric text-amber">
              {stats.formattedMedianPrice || formatNaira(stats.medianPriceKobo)}
            </span>
          </div>

          <div className="summary-tile bg-raised border border-default rounded-lg p-5">
            <div className="flex items-center gap-3 mb-2">
              <Clock size={18} className="text-secondary" />
              <span className="tile-label text-xs text-secondary uppercase tracking-wider font-semibold">
                Avg Days on Market
              </span>
            </div>
            <span className="tile-value text-2xl font-bold font-numeric text-primary">
              {stats.avgDaysOnMarket !== null && stats.avgDaysOnMarket !== undefined 
                ? `${stats.avgDaysOnMarket.toFixed(1)} days` 
                : '—'}
            </span>
          </div>
        </section>
      )}

      {/* Percentiles and Trends */}
      <section className="neighbourhood-charts-section mb-12">
        {stats?.pricePercentiles && (
          <PricePercentilesBar percentiles={stats.pricePercentiles} />
        )}
        
        {trendData?.weeks && trendData.weeks.length > 0 && (
          <TrendChart weeks={trendData.weeks} />
        )}
      </section>

      {/* Active Listings Grid */}
      <section className="neighbourhood-listings-section border-t border-default pt-8">
        <h2 className="section-title text-lg font-bold tracking-tight text-primary mb-6">
          Active Listings in {name}
        </h2>

        {allListings.length === 0 ? (
          <div className="alerts-empty-state text-center py-16 bg-raised border border-default border-dashed rounded-lg p-8">
            <div className="inline-flex items-center justify-center p-3 rounded-full bg-subtle text-amber mb-4 border border-strong">
              <AlertCircle size={24} />
            </div>
            <h3 className="text-md font-bold text-primary mb-1">No listings found</h3>
            <p className="text-sm text-secondary max-w-sm mx-auto mb-6">
              No active properties are indexed for this neighbourhood currently. Try checking another neighbourhood or browsing all listings.
            </p>
            <Link to="/listings">
              <Button variant="primary">Browse All Listings</Button>
            </Link>
          </div>
        ) : (
          <>
            <div className="listings-grid grid mb-8">
              {allListings.map((listing) => (
                <ListingCard
                  key={listing.id}
                  listing={listing}
                  onClick={() => navigate(`/listings/${listing.id}`)}
                />
              ))}
            </div>

            {/* Pagination */}
            {hasNextPage && (
              <div className="pagination-container text-center">
                <Button
                  variant="secondary"
                  onClick={() => fetchNextPage()}
                  isLoading={isFetchingNextPage}
                  className="load-more-btn"
                >
                  Load More Listings
                </Button>
              </div>
            )}
          </>
        )}
      </section>
    </div>
  );
}
