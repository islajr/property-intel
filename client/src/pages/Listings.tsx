import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useInfiniteQuery } from '@tanstack/react-query';
import { X, AlertCircle, Map as MapIcon, List as ListIcon, SlidersHorizontal } from 'lucide-react';
import { listings } from '../api';
import useListingFilters from '../hooks/useListingFilters';
import { FilterSidebar } from '../components/layout/FilterSidebar';
import ListingCard from '../components/listing/ListingCard';
import MapView from '../components/listing/MapView';
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
  
  // Mobile view toggle state: 'list' or 'map'
  const [viewMode, setViewMode] = useState<'list' | 'map'>('list');
  // Sync hover states between list cards and map markers
  const [hoveredListingId, setHoveredListingId] = useState<number | null>(null);
  const [selectedListingId, setSelectedListingId] = useState<number | null>(null);
  const [showFiltersMobile, setShowFiltersMobile] = useState(false);

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
    queryFn: ({ pageParam }) => {
      const params: any = { ...filters };
      if (pageParam !== undefined) {
        params.cursor = pageParam;
      }
      return listings.search(params);
    },
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

  // Sync scroll on map pin click
  const handleMarkerClick = (id: number) => {
    setSelectedListingId(id);
    const cardElement = document.getElementById(`listing-card-${id}`);
    if (cardElement) {
      cardElement.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      // Temporary flash highlight effect
      cardElement.classList.add('flash-highlight');
      setTimeout(() => {
        cardElement.classList.remove('flash-highlight');
      }, 2000);
    }
  };

  return (
    <div className="listings-page-container container">
      {/* Mobile view toggle floating button */}
      <div className="mobile-view-toggle mobile-only">
        <Button
          variant="primary"
          onClick={() => setViewMode(viewMode === 'list' ? 'map' : 'list')}
          className="rounded-full shadow-lg flex items-center gap-2"
          style={{ position: 'fixed', bottom: '24px', left: '50%', transform: 'translateX(-50%)', zIndex: 1000 }}
        >
          {viewMode === 'list' ? (
            <>
              <MapIcon size={16} />
              Show Map
            </>
          ) : (
            <>
              <ListIcon size={16} />
              Show List
            </>
          )}
        </Button>
      </div>

      <div className="listings-layout">
        {/* Left Column: Sidebar Filters */}
        <FilterSidebar
          currentFilters={filters}
          onApply={handleApplyFilters}
          onReset={handleResetFilters}
          isOpenMobile={showFiltersMobile}
          onCloseMobile={() => setShowFiltersMobile(false)}
        />

        {/* Right Column: Search Results & Map splitting */}
        <div className="listings-results">
          {/* Results Header: Count, Chips, Sort and Toggle */}
          <div className="results-header-container flex flex-wrap justify-between items-center gap-4 mb-4">
            <div className="results-header-info">
              <h1 className="results-count text-lg font-bold">
                {isLoading ? (
                  <Skeleton width={120} height={24} />
                ) : (
                  <span className="font-numeric">{totalCount} listings found</span>
                )}
              </h1>
              {activeChips.length > 0 && (
                <div className="active-chips-container flex flex-wrap gap-2 mt-2">
                  {activeChips.map((chip) => (
                    <span key={chip.key} className="filter-chip text-xs bg-subtle border border-strong rounded px-2.5 py-1 flex items-center gap-1.5 font-numeric">
                      {chip.label}
                      <button
                        onClick={() => handleRemoveChip(chip.key)}
                        className="chip-remove-btn text-secondary hover:text-primary focus:outline-none"
                        aria-label={`Remove filter ${chip.label}`}
                      >
                        <X size={12} />
                      </button>
                    </span>
                  ))}
                  <button onClick={clearFilters} className="clear-all-chips-btn text-xs text-amber font-semibold hover:underline">
                    Clear all
                  </button>
                </div>
              )}
            </div>

            <div className="results-header-actions" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
              <Button
                variant="secondary"
                onClick={() => setShowFiltersMobile(true)}
                className="mobile-only"
                style={{ height: '40px', display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}
              >
                <SlidersHorizontal size={16} />
                Filters
                {activeChips.length > 0 && (
                  <span 
                    className="font-numeric" 
                    style={{ 
                      marginLeft: 'var(--space-1)', 
                      padding: '2px 6px', 
                      fontSize: '11px', 
                      backgroundColor: 'var(--color-amber-400)', 
                      color: 'var(--color-text-inverse)', 
                      borderRadius: '50%', 
                      fontWeight: 'bold',
                      display: 'inline-flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      minWidth: '18px',
                      height: '18px'
                    }}
                  >
                    {activeChips.length}
                  </span>
                )}
              </Button>
              <Select
                value={filters.sort || 'newest'}
                onChange={handleSortChange}
                options={SORT_OPTIONS}
                className="sort-select"
              />
            </div>
          </div>

          {/* Results Content Split Panes */}
          {isLoading ? (
            <div className="listings-grid grid">
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
                <p className="text-md font-semibold">Unable to connect</p>
                <p className="text-sm text-secondary">
                  Unable to load property listings. Check your internet connection and try again.
                </p>
                <Button variant="secondary" size="sm" onClick={() => refetch()} style={{ marginTop: 'var(--space-2)' }}>
                  Retry
                </Button>
              </div>
            </div>
          ) : allListings.length === 0 ? (
            <div className="listings-empty-state text-center py-16 bg-raised border border-default border-dashed rounded-lg">
              <span className="empty-state-icon text-tertiary text-2xl" aria-hidden="true">
                🔍
              </span>
              <p className="text-md font-bold text-primary mt-4">No listings match your filters</p>
              <p className="text-sm text-secondary max-w-xs mx-auto mt-2">
                Try adjusting your search criteria or clearing some filters to find matches.
              </p>
              <Button variant="primary" onClick={clearFilters} style={{ marginTop: 'var(--space-4)' }}>
                Clear all filters
              </Button>
            </div>
          ) : (
            <div className="listings-split-pane-layout">
              {/* Left Pane: List Grid */}
              <div 
                className={`listings-list-pane flex-1 ${viewMode === 'map' ? 'mobile-hidden' : ''}`}
              >
                <div className="listings-grid grid">
                  {allListings.map((listing) => (
                    <div 
                      key={listing.id}
                      id={`listing-card-${listing.id}`}
                      className={`listing-card-interactive-wrapper transition-all duration-200 ${
                        selectedListingId === listing.id ? 'card-selected-highlight' : ''
                      }`}
                      onMouseEnter={() => setHoveredListingId(listing.id)}
                      onMouseLeave={() => setHoveredListingId(null)}
                    >
                      <ListingCard
                        listing={listing}
                        onClick={() => navigate(`/listings/${listing.id}`)}
                      />
                    </div>
                  ))}
                </div>

                {/* Load More Pagination */}
                {hasNextPage && (
                  <div className="pagination-container text-center mt-6">
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
              </div>

              {/* Right Pane: Map */}
              <div 
                className={`listings-map-pane ${viewMode === 'list' ? 'mobile-hidden' : ''}`}
                style={{ 
                  flex: '1.2', 
                  minHeight: '400px', 
                  height: 'calc(100vh - 220px)', 
                  position: 'sticky', 
                  top: '100px', 
                  borderRadius: '12px',
                  overflow: 'hidden',
                  border: '1px solid var(--color-border)'
                }}
              >
                <MapView
                  listings={allListings}
                  activeListingId={hoveredListingId}
                  onMarkerHover={setHoveredListingId}
                  onMarkerClick={handleMarkerClick}
                />
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
