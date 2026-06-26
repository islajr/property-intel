import { useSearchParams } from 'react-router-dom';
import { useInfiniteQuery } from '@tanstack/react-query';
import { market } from '../api';
import { NeighbourhoodStatCard } from '../components/market/NeighbourhoodStatCard';
import Select from '../components/primitives/Select';
import Button from '../components/primitives/Button';
import Skeleton from '../components/primitives/Skeleton';
import { AlertCircle, BarChart3, TrendingUp, Home } from 'lucide-react';
import { formatNaira } from '../utils/format';

const SORT_OPTIONS = [
  { value: 'neighbourhood', label: 'Alphabetical' },
  { value: 'median_price', label: 'Median Price' },
  { value: 'active_listings', label: 'Active Volume' },
  { value: 'new_listings', label: 'New Listings' },
  { value: 'price_reduced', label: 'Price Reductions' },
];

export default function Market() {
  const [searchParams, setSearchParams] = useSearchParams();
  const sort = searchParams.get('sort') || 'neighbourhood';

  // Infinite query for neighbourhoods list
  const {
    data,
    isLoading,
    isError,
    hasNextPage,
    fetchNextPage,
    isFetchingNextPage,
    refetch,
  } = useInfiniteQuery({
    queryKey: ['market', 'neighbourhoods-list', sort],
    queryFn: ({ pageParam }) =>
      market.getNeighbourhoods({
        sort_by: sort as any,
        limit: 12,
        cursor: pageParam,
      }),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (lastPage) =>
      lastPage.meta.has_more ? lastPage.meta.next_cursor || undefined : undefined,
  });

  const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSearchParams({ sort: e.target.value });
  };

  const allNeighbourhoods = data?.pages.flatMap((page) => page.data) || [];
  const totalCount = data?.pages[0]?.meta.count ?? 0;

  // Calculate market-wide aggregates across currently loaded neighbourhoods
  const totalActiveListings = allNeighbourhoods.reduce((sum, item) => sum + item.activeListingCount, 0);
  const avgMedianPrice = allNeighbourhoods.length > 0 
    ? Math.round(allNeighbourhoods.reduce((sum, item) => sum + item.medianPriceKobo, 0) / allNeighbourhoods.length)
    : 0;

  return (
    <div className="market-page-container container py-8">
      {/* Header and Controls */}
      <header className="market-page-header flex flex-wrap justify-between items-center gap-4 mb-8">
        <div>
          <h1 className="page-title text-2xl font-bold tracking-tight text-primary">
            Market Intelligence
          </h1>
          <p className="text-sm text-secondary mt-1">
            Real-time neighbourhood price indicators and activity metrics.
          </p>
        </div>

        <div className="market-sort-control">
          <Select
            value={sort}
            onChange={handleSortChange}
            options={SORT_OPTIONS}
            className="sort-select"
          />
        </div>
      </header>

      {/* Summary Stat Row */}
      <section className="market-summary-grid grid mb-8">
        <div className="summary-tile bg-raised border border-default rounded-lg p-5">
          <div className="flex items-center gap-3 mb-2">
            <Home size={18} className="text-secondary" />
            <span className="tile-label text-xs text-secondary uppercase tracking-wider font-semibold">
              Tracked Markets
            </span>
          </div>
          <span className="tile-value text-2xl font-bold font-numeric text-primary">
            {isLoading ? <Skeleton width={60} height={32} /> : totalCount}
          </span>
        </div>

        <div className="summary-tile bg-raised border border-default rounded-lg p-5">
          <div className="flex items-center gap-3 mb-2">
            <BarChart3 size={18} className="text-secondary" />
            <span className="tile-label text-xs text-secondary uppercase tracking-wider font-semibold">
              Active Supply
            </span>
          </div>
          <span className="tile-value text-2xl font-bold font-numeric text-primary">
            {isLoading ? <Skeleton width={80} height={32} /> : `${totalActiveListings} listings`}
          </span>
        </div>

        <div className="summary-tile bg-raised border border-default rounded-lg p-5">
          <div className="flex items-center gap-3 mb-2">
            <TrendingUp size={18} className="text-secondary" />
            <span className="tile-label text-xs text-secondary uppercase tracking-wider font-semibold">
              Average Median Price
            </span>
          </div>
          <span className="tile-value text-2xl font-bold font-numeric text-amber">
            {isLoading ? <Skeleton width={120} height={32} /> : formatNaira(avgMedianPrice)}
          </span>
        </div>
      </section>

      {/* Main Grid Content */}
      {isLoading ? (
        <div className="neighbourhoods-grid grid">
          {Array(6)
            .fill(0)
            .map((_, i) => (
              <Skeleton key={i} height={200} borderRadius={12} />
            ))}
        </div>
      ) : isError ? (
        <div className="error-banner">
          <AlertCircle size={20} className="text-secondary" />
          <div>
            <p className="text-md">Unable to connect</p>
            <p className="text-sm text-secondary">
              Unable to load market data. Check your internet connection and try again.
            </p>
            <Button variant="secondary" size="sm" onClick={() => refetch()} style={{ marginTop: 'var(--space-2)' }}>
              Retry
            </Button>
          </div>
        </div>
      ) : allNeighbourhoods.length === 0 ? (
        <div className="market-empty-state text-center py-12">
          <p className="text-md text-secondary">No neighbourhoods tracked in this market.</p>
        </div>
      ) : (
        <>
          <div className="neighbourhoods-grid grid mb-8">
            {allNeighbourhoods.map((n) => (
              <NeighbourhoodStatCard 
                key={n.neighbourhood} 
                name={n.neighbourhood} 
                city={n.city}
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
                Load More Markets
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
