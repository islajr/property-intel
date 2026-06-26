import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { SlidersHorizontal, RotateCcw, X } from 'lucide-react';
import { market } from '../../api';
import Input from '../primitives/Input';
import Select from '../primitives/Select';
import Button from '../primitives/Button';
import type { ListingSearchParams, PropertyType } from '../../types/api';

export interface FilterSidebarProps {
  currentFilters: ListingSearchParams;
  onApply: (filters: Partial<ListingSearchParams>) => void;
  onReset: () => void;
  isOpenMobile?: boolean;
  onCloseMobile?: () => void;
}

const PROPERTY_TYPE_OPTIONS = [
  { value: '', label: 'All Property Types' },
  { value: 'FLAT', label: 'Flat / Apartment' },
  { value: 'DETACHED_BUILDING', label: 'Detached Building' },
  { value: 'SERVICE_APARTMENT', label: 'Service Apartment' },
  { value: 'TERRACED_HOUSE', label: 'Terraced House' },
  { value: 'LAND', label: 'Land' },
  { value: 'COMMERCIAL', label: 'Commercial' },
];

const BEDROOM_OPTIONS = [
  { value: '', label: 'Any' },
  { value: '1', label: '1' },
  { value: '2', label: '2' },
  { value: '3', label: '3' },
  { value: '4', label: '4' },
  { value: '5', label: '5+' },
];

export function FilterSidebar({
  currentFilters,
  onApply,
  onReset,
  isOpenMobile = false,
  onCloseMobile,
}: FilterSidebarProps) {
  // Fetch neighbourhoods to populate select list
  const { data: neighbourhoodsData } = useQuery({
    queryKey: ['market', 'neighbourhoods'],
    queryFn: () => market.getNeighbourhoods({ limit: 50 }),
  });

  const neighbourhoodOptions = [
    { value: '', label: 'All Neighbourhoods' },
    ...(neighbourhoodsData?.data.map((n) => ({
      value: n.neighbourhood,
      label: n.neighbourhood,
    })) || []),
  ];

  // Local state for interactive controls before apply
  const [q, setQ] = useState(currentFilters.q || '');
  const [neighbourhood, setNeighbourhood] = useState(currentFilters.neighbourhood || '');
  const [type, setType] = useState(currentFilters.type || '');
  const [minBeds, setMinBeds] = useState(currentFilters.min_beds?.toString() || '');
  const [maxBeds, setMaxBeds] = useState(currentFilters.max_beds?.toString() || '');
  
  // Convert kobo back to Naira for display
  const [minPrice, setMinPrice] = useState(
    currentFilters.min_price ? (currentFilters.min_price / 100).toString() : ''
  );
  const [maxPrice, setMaxPrice] = useState(
    currentFilters.max_price ? (currentFilters.max_price / 100).toString() : ''
  );
  const [maxDays, setMaxDays] = useState(currentFilters.max_days?.toString() || '');
  const [priceReduced, setPriceReduced] = useState(!!currentFilters.price_reduced);
  const [includeInactive, setIncludeInactive] = useState(!!currentFilters.include_inactive);

  // Swipe-down-to-close touch logic
  const [touchStart, setTouchStart] = useState<number | null>(null);
  const [touchCurrent, setTouchCurrent] = useState<number | null>(null);

  const handleTouchStart = (e: React.TouchEvent) => {
    const touch = e.targetTouches[0];
    if (touch) {
      setTouchStart(touch.clientY);
    }
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    if (touchStart === null) return;
    const touch = e.targetTouches[0];
    if (touch) {
      const currentY = touch.clientY;
      if (currentY > touchStart) {
        setTouchCurrent(currentY);
      }
    }
  };

  const handleTouchEnd = () => {
    if (touchStart === null || touchCurrent === null) return;
    const diff = touchCurrent - touchStart;
    if (diff > 100 && onCloseMobile) {
      onCloseMobile();
    }
    setTouchStart(null);
    setTouchCurrent(null);
  };

  const translateStyle = touchStart !== null && touchCurrent !== null && touchCurrent > touchStart
    ? { transform: `translateY(${touchCurrent - touchStart}px)`, transition: 'none' }
    : undefined;

  // Keep local state in sync when URL parameters change externally (e.g. Back button or chip delete)
  useEffect(() => {
    setQ(currentFilters.q || '');
    setNeighbourhood(currentFilters.neighbourhood || '');
    setType(currentFilters.type || '');
    setMinBeds(currentFilters.min_beds?.toString() || '');
    setMaxBeds(currentFilters.max_beds?.toString() || '');
    setMinPrice(currentFilters.min_price ? (currentFilters.min_price / 100).toString() : '');
    setMaxPrice(currentFilters.max_price ? (currentFilters.max_price / 100).toString() : '');
    setMaxDays(currentFilters.max_days?.toString() || '');
    setPriceReduced(!!currentFilters.price_reduced);
    setIncludeInactive(!!currentFilters.include_inactive);
  }, [currentFilters]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const filtersToApply: Partial<ListingSearchParams> = {};
    if (q.trim()) filtersToApply.q = q.trim();
    if (neighbourhood) filtersToApply.neighbourhood = neighbourhood;
    if (type) filtersToApply.type = type as PropertyType;
    if (minBeds) filtersToApply.min_beds = parseInt(minBeds, 10);
    if (maxBeds) filtersToApply.max_beds = parseInt(maxBeds, 10);
    if (minPrice) filtersToApply.min_price = parseFloat(minPrice) * 100;
    if (maxPrice) filtersToApply.max_price = parseFloat(maxPrice) * 100;
    if (maxDays) filtersToApply.max_days = parseInt(maxDays, 10);
    if (priceReduced) filtersToApply.price_reduced = true;
    if (includeInactive) filtersToApply.include_inactive = true;

    onApply(filtersToApply);
    onCloseMobile?.();
  };

  const handleReset = () => {
    setQ('');
    setNeighbourhood('');
    setType('');
    setMinBeds('');
    setMaxBeds('');
    setMinPrice('');
    setMaxPrice('');
    setMaxDays('');
    setPriceReduced(false);
    setIncludeInactive(false);
    onReset();
    onCloseMobile?.();
  };

  return (
    <>
      <div
        className={`mobile-sheet-overlay ${isOpenMobile ? 'active' : ''}`}
        onClick={onCloseMobile}
        aria-hidden="true"
      />
      <aside 
        className={`filter-sidebar ${isOpenMobile ? 'mobile-sheet-open' : ''}`}
        style={translateStyle}
      >
        {/* Drag handle for mobile bottom sheet */}
        <div
          className="mobile-sheet-drag-area mobile-only"
          onTouchStart={handleTouchStart}
          onTouchMove={handleTouchMove}
          onTouchEnd={handleTouchEnd}
          style={{ width: '100%', height: '16px', display: 'flex', justifyContent: 'center', alignItems: 'center', cursor: 'grab', marginBottom: '-8px' }}
        >
          <div
            className="mobile-sheet-handle"
            style={{ width: '36px', height: '4px', backgroundColor: 'var(--color-border-strong)', borderRadius: '2px' }}
          />
        </div>

        <div className="filter-sidebar-header flex justify-between items-center w-full" style={{ display: 'flex', width: '100%' }}>
          <div className="flex items-center gap-2" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
            <SlidersHorizontal size={18} />
            <span className="text-md">Filters</span>
          </div>
          {onCloseMobile && (
            <button
              type="button"
              className="mobile-sheet-close-btn mobile-only p-1 text-secondary hover:text-primary focus:outline-none"
              onClick={onCloseMobile}
              aria-label="Close filters"
              style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', padding: 0 }}
            >
              <X size={20} />
            </button>
          )}
        </div>
      <form onSubmit={handleSubmit} className="filter-form">
        <Input
          label="Search text"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Keyword..."
        />

        <Select
          label="Neighbourhood"
          value={neighbourhood}
          onChange={(e) => setNeighbourhood(e.target.value)}
          options={neighbourhoodOptions}
        />

        <Select
          label="Property Type"
          value={type}
          onChange={(e) => setType(e.target.value)}
          options={PROPERTY_TYPE_OPTIONS}
        />

        <div className="form-row-2">
          <Select
            label="Min Beds"
            value={minBeds}
            onChange={(e) => setMinBeds(e.target.value)}
            options={BEDROOM_OPTIONS}
          />
          <Select
            label="Max Beds"
            value={maxBeds}
            onChange={(e) => setMaxBeds(e.target.value)}
            options={BEDROOM_OPTIONS}
          />
        </div>

        <div className="form-row-2">
          <Input
            label="Min Price (₦)"
            type="number"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            placeholder="Min"
          />
          <Input
            label="Max Price (₦)"
            type="number"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            placeholder="Max"
          />
        </div>

        <Input
          label="Max Days Listed"
          type="number"
          value={maxDays}
          onChange={(e) => setMaxDays(e.target.value)}
          placeholder="Any"
        />

        <div className="checkbox-group">
          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={priceReduced}
              onChange={(e) => setPriceReduced(e.target.checked)}
              className="checkbox-input"
            />
            <span>Price reduced only</span>
          </label>

          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={includeInactive}
              onChange={(e) => setIncludeInactive(e.target.checked)}
              className="checkbox-input"
            />
            <span>Include inactive listings</span>
          </label>
        </div>

        <div className="filter-actions">
          <Button type="submit" variant="primary" className="apply-btn">
            Apply Filters
          </Button>
          <Button type="button" variant="ghost" onClick={handleReset} className="reset-btn">
            <RotateCcw size={14} />
            Reset
          </Button>
        </div>
      </form>
    </aside>
  </>
  );
}
