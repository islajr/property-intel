export type PropertyType = 'FLAT' | 'DETACHED_BUILDING' | 'SERVICE_APARTMENT' | 'TERRACED_HOUSE' | 'LAND' | 'COMMERCIAL' | string;

export type ListingSource = 'propertypro' | 'privateproperty' | 'nigeriapropertycentre' | 'jiji' | string;

export interface PriceHistoryResponse {
  oldValue: number | null;
  newValue: number | null;
  eventType: 'LISTED' | 'PRICE_CHANGE' | 'RELISTED' | 'REMOVED' | string;
  eventDate: string;               // "YYYY-MM-DD"
}

export interface ListingData {
  id: number;
  source: ListingSource;
  url: string;
  title: string;
  priceKobo: number;
  priceFormatted: string;          // "₦45,000,000" — provided by API, not computed
  propertyType: PropertyType;
  bedrooms: number | null;
  bathrooms: number | null;
  neighbourhood: string;
  city: string;
  lat: number;
  lng: number;
  listingStatus: 'ACTIVE' | 'REMOVED' | string;
}

export interface ListingDetailResponse {
  id: number;
  source: ListingSource;
  url: string;
  title: string;
  priceKobo: number;
  priceFormatted: string;
  propertyType: PropertyType;
  bedrooms: number | null;
  bathrooms: number | null;
  neighbourhood: string;
  city: string;
  lat: number;
  lng: number;
  listingStatus: 'ACTIVE' | 'REMOVED' | string;
  listingHistory: PriceHistoryResponse[];
}

export interface MarketPercentiles {
  p25: number;
  p50: number;
  p75: number;
  p90: number;
}

export interface NeighbourhoodStatsResponse {
  neighbourhood: string;
  city: string;
  medianPriceKobo: number;
  formattedMedianPrice: string;
  activeListingCount: number;
  avgDaysOnMarket: number | null;
  newListingsCount: number;
  priceReducedCount: number;
  pricePercentiles: MarketPercentiles;
  vsLastWeek: {
    medianPriceChangePct: number | null;
    daysOnMarketChangePct: number | null;
  } | null;
}

export interface TrendWeek {
  week_start: string;              // "YYYY-MM-DD"
  median_price_kobo: number;
  active_listings: number;
  new_listings: number;
}

export interface NeighbourhoodTrendResponse {
  neighbourhood: string;
  weeks: TrendWeek[];
}

export interface NeighbourhoodSummaryData {
  neighbourhood: string;
  city: string;
  activeListingCount: number;
  medianPriceKobo: number;
  formattedMedianPrice: string;
}

export interface NeighbourhoodSummaryMeta {
  count: number;
  next_cursor: string | null;
  has_more: boolean;
}

export interface NeighbourhoodSummary {
  data: NeighbourhoodSummaryData[];
  meta: NeighbourhoodSummaryMeta;
}

export interface Alert {
  id: number;
  userId: number;
  neighbourhood: string;
  maxPriceKobo: number | null;
  minBedrooms: number | null;
  propertyType: PropertyType | null;
  createdAt: string;              // ISO 8601 timestamp
  isActive: boolean;
}

export interface AlertRequest {
  neighbourhood: string;
  maxPriceKobo: number | null;
  minBedrooms: number | null;
  propertyType: PropertyType | null;
}

export interface AuthResponse {
  accessToken: string;
  expiresIn: number;              // in seconds
}

export interface ApiError {
  error: string;
  message: string;
  path: string;
  timestamp: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  meta: {
    count: number;
    next_cursor: string | null;
    has_more: boolean;
  };
}

export interface ListingSearchParams {
  q?: string;
  neighbourhood?: string;
  type?: PropertyType;
  min_beds?: number;
  max_beds?: number;
  min_price?: number;            // in kobo
  max_price?: number;            // in kobo
  max_days?: number;
  price_reduced?: boolean;
  sort?: 'price_asc' | 'price_desc' | 'newest' | 'days_asc';
  cursor?: string;
  limit?: number;
  include_inactive?: boolean;
}
