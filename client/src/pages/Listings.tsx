import { useNavigate } from 'react-router-dom';
import { useInfiniteQuery } from '@tanstack/react-query';
import { X, AlertCircle } from 'lucide-react';
import { listings } from '../api';
import useListingFilters from '../hooks/useListingFilters';
import { FilterSidebar } from '../components/layout/FilterSidebar';
import ListingCard from '../components/listing/ListingCard';
import Skeleton from '../components/primitives/Skeleton';
import Button from '../components/primitives/Button';
import Select from '../components/primitives/Select';
import type { ListingSearchParams } from '../types/api';

const SORT_OPTIONS = [
  { value: 'newest', label: 'Newest First' },
  { value: 'price_asc', label: 'Price: Low to High' },
  { value: 'price_desc', label: 'Price: High to Low' },
  { value: 'days_asc', label: 'Days on Market: Low to High' },
];

export default function Listings() {
  const navigate = useNavigate();
  const { filters, setFilters, clearFilters } = useListingFilters();

  // Infinite query for paginated listings
  const {
    data,
    isLoading,
    isError,
    hasNextPage,
    fetchNextPage,
    isFetchingNextPage,
    refetch,
  } = useInfiniteQuery({
    queryKey: ['listings', filters],
    queryFn: ({ pageParam }) =>
      listings.search({
        ...filters,
        cursor: pageParam,
      }),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (lastPage) =>
      lastPage.meta.has_more ? lastPage.meta.next_cursor || undefined : undefined,
  });

  const handleApplyFilters = (newFilters: Partial<ListingSearchParams>) => {
    setFilters(newFilters);
  };

  const handleResetFilters = () => {
    clearFilters();
  };

  const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilters({ sort: e.target.value as any });
  };

  // Flatten listings across pages
  const allListings = data?.pages.flatMap((page) => page.data) || [];
  const totalCount = data?.pages[0]?.meta.count ?? 0;

  // Generate active filter chips
  const activeChips = [];
  if (filters.q) activeChips.push({ key: 'q', label: `Search: "${filters.q}"` });
  if (filters.neighbourhood) activeChips.push({ key: 'neighbourhood', label: filters.neighbourhood });
  if (filters.type) {
    activeChips.push({
      key: 'type',
      label: `Type: ${filters.type.replace(/_/g, ' ')}`,
    });
  }
  if (filters.min_beds || filters.max_beds) {
    let label = 'Beds: ';
    if (filters.min_beds && filters.max_beds) {
      label += `${filters.min_beds} - ${filters.max_beds}`;
    } else if (filters.min_beds) {
      label += `${filters.min_beds}+`;
    } else {
      label += `Up to ${filters.max_beds}`;
    }
    activeChips.push({ key: 'beds', label });
  }
  if (filters.min_price || filters.max_price) {
    let label = 'Price: ';
    const minM = filters.min_price ? `${(filters.min_price / 100000000).toFixed(0)}M` : '0';
    const maxM = filters.max_price ? `${(filters.max_price / 100000000).toFixed(0)}M` : 'Any';
    label += `₦${minM} - ₦${maxM}`;
    activeChips.push({ key: 'price', label });
  }
  if (filters.max_days) activeChips.push({ key: 'max_days', label: `Listed: < ${filters.max_days} days` });
  if (filters.price_reduced) activeChips.push({ key: 'price_reduced', label: 'Price Reduced' });
  if (filters.include_inactive) activeChips.push({ key: 'include_inactive', label: 'Includes Inactive' });

  const handleRemoveChip = (key: string) => {
    if (key === 'beds') {
      setFilters({ min_beds: undefined, max_beds: undefined });
    } else if (key === 'price') {
      setFilters({ min_price: undefined, max_price: undefined });
    } else {
      setFilters({ [key]: undefined });
    }
  };

  return (
    <div className="listings-page-container container">
      <div className="listings-layout">
        {/* Left Column: Sidebar Filters */}
        <FilterSidebar
          currentFilters={filters}
          onApply={handleApplyFilters}
          onReset={handleResetFilters}
        />

        {/* Right Column: Search Results */}
        <div className="listings-results">
          {/* Results Header: Count, Chips, Sort and Toggle */}
          <div className="results-header-container">
            <div className="results-header-info">
              <h1 className="results-count text-lg">
                {isLoading ? (
                  <Skeleton width={120} height={24} />
                ) : (
                  <span className="font-numeric">{totalCount} listings found</span>
                )}
              </h1>
              {activeChips.length > 0 && (
                <div className="active-chips-container">
                  {activeChips.map((chip) => (
                    <span key={chip.key} className="filter-chip text-xs">
                      {chip.label}
                      <button
                        onClick={() => handleRemoveChip(chip.key)}
                        className="chip-remove-btn"
                        aria-label={`Remove filter ${chip.label}`}
                      >
                        <X size={12} />
                      </button>
                    </span>
                  ))}
                  <button onClick={clearFilters} className="clear-all-chips-btn text-xs text-amber">
                    Clear all
                  </button>
                </div>
              )}
            </div>

            <div className="results-header-actions">
              <Select
                value={filters.sort || 'newest'}
                onChange={handleSortChange}
                options={SORT_OPTIONS}
                className="sort-select"
              />
            </div>
          </div>

          {/* Results Content */}
          {isLoading ? (
            <div className="listings-grid">
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
                  Unable to load property listings. Check your internet connection and try again.
                </p>
                <Button variant="secondary" size="sm" onClick={() => refetch()} style={{ marginTop: 'var(--space-2)' }}>
                  Retry
                </Button>
              </div>
            </div>
          ) : allListings.length === 0 ? (
            <div className="listings-empty-state">
              <span className="empty-state-icon text-tertiary" aria-hidden="true">
                🔍
              </span>
              <p className="text-md">No listings match your filters</p>
              <p className="text-sm text-secondary">
                Try adjusting your search criteria or clearing some filters to find matches.
              </p>
              <Button variant="primary" onClick={clearFilters} style={{ marginTop: 'var(--space-4)' }}>
                Clear all filters
              </Button>
            </div>
          ) : (
            <>
              <div className="listings-grid">
                {allListings.map((listing) => (
                  <ListingCard
                    key={listing.id}
                    listing={listing}
                    onClick={() => navigate(`/listings/${listing.id}`)}
                  />
                ))}
              </div>

              {/* Load More Pagination */}
              {hasNextPage && (
                <div className="pagination-container">
                  <Button
                    variant="secondary"
                    onClick={() => fetchNextPage()}
                    isLoading={isFetchingNextPage}
                    className="load-more-btn"
                  >
                    Load More
                  </Button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
