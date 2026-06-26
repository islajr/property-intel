import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { SlidersHorizontal, RotateCcw } from 'lucide-react';
import { market } from '../../api';
import Input from '../primitives/Input';
import Select from '../primitives/Select';
import Button from '../primitives/Button';
import type { ListingSearchParams, PropertyType } from '../../types/api';

export interface FilterSidebarProps {
  currentFilters: ListingSearchParams;
  onApply: (filters: Partial<ListingSearchParams>) => void;
  onReset: () => void;
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

export function FilterSidebar({ currentFilters, onApply, onReset }: FilterSidebarProps) {
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
  };

  return (
    <aside className="filter-sidebar">
      <div className="filter-sidebar-header">
        <SlidersHorizontal size={18} />
        <span className="text-md">Filters</span>
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
  );
}
