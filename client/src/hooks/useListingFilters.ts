import { useSearchParams } from 'react-router-dom';
import { useMemo } from 'react';
import type { ListingSearchParams, PropertyType } from '../types/api';

export const useListingFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const filters = useMemo((): ListingSearchParams => {
    const q = searchParams.get('q') || undefined;
    const neighbourhood = searchParams.get('neighbourhood') || undefined;
    const type = (searchParams.get('type') as PropertyType) || undefined;
    
    const min_beds_raw = searchParams.get('min_beds');
    const min_beds = min_beds_raw ? parseInt(min_beds_raw, 10) : undefined;
    
    const max_beds_raw = searchParams.get('max_beds');
    const max_beds = max_beds_raw ? parseInt(max_beds_raw, 10) : undefined;
    
    const min_price_raw = searchParams.get('min_price');
    const min_price = min_price_raw ? parseInt(min_price_raw, 10) : undefined;
    
    const max_price_raw = searchParams.get('max_price');
    const max_price = max_price_raw ? parseInt(max_price_raw, 10) : undefined;
    
    const max_days_raw = searchParams.get('max_days');
    const max_days = max_days_raw ? parseInt(max_days_raw, 10) : undefined;
    
    const price_reduced = searchParams.get('price_reduced') === 'true' ? true : undefined;
    const sort = (searchParams.get('sort') as any) || 'newest';
    const include_inactive = searchParams.get('include_inactive') === 'true' ? true : undefined;

    const result: ListingSearchParams = {};
    if (q !== undefined) result.q = q;
    if (neighbourhood !== undefined) result.neighbourhood = neighbourhood;
    if (type !== undefined) result.type = type;
    if (min_beds !== undefined) result.min_beds = min_beds;
    if (max_beds !== undefined) result.max_beds = max_beds;
    if (min_price !== undefined) result.min_price = min_price;
    if (max_price !== undefined) result.max_price = max_price;
    if (max_days !== undefined) result.max_days = max_days;
    if (price_reduced !== undefined) result.price_reduced = price_reduced;
    if (sort !== undefined) result.sort = sort;
    if (include_inactive !== undefined) result.include_inactive = include_inactive;
    
    return result;
  }, [searchParams]);

  const setFilters = (newFilters: Partial<ListingSearchParams>) => {
    const params = new URLSearchParams();
    
    // Copy existing search params first
    searchParams.forEach((value, key) => {
      params.set(key, value);
    });

    Object.entries(newFilters).forEach(([key, value]) => {
      if (value === undefined || value === null || value === '' || value === false) {
        params.delete(key);
      } else {
        params.set(key, String(value));
      }
    });
    
    setSearchParams(params);
  };

  const clearFilters = () => {
    setSearchParams(new URLSearchParams());
  };

  return { filters, setFilters, clearFilters };
};

export default useListingFilters;
