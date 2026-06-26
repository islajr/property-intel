import client from './client';
import { NeighbourhoodSummary, NeighbourhoodStatsResponse, NeighbourhoodTrendResponse } from '../types/api';

export interface NeighbourhoodSearchParams {
  sort_by?: 'neighbourhood' | 'new_listings' | 'price_reduced' | 'median_price' | 'active_listings';
  limit?: number;
  cursor?: string;
}

export const getNeighbourhoods = async (params?: NeighbourhoodSearchParams): Promise<NeighbourhoodSummary> => {
  const response = await client.get<NeighbourhoodSummary>('/market/neighbourhoods', { params });
  return response.data;
};

export const getStats = async (neighbourhood: string): Promise<NeighbourhoodStatsResponse> => {
  const response = await client.get<NeighbourhoodStatsResponse>(`/market/${encodeURIComponent(neighbourhood)}/stats`);
  return response.data;
};

export const getTrends = async (neighbourhood: string): Promise<NeighbourhoodTrendResponse> => {
  const response = await client.get<NeighbourhoodTrendResponse>(`/market/neighbourhoods/${encodeURIComponent(neighbourhood)}/trends`);
  return response.data;
};
