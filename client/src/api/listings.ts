import client from './client';
import type { ListingData, ListingDetailResponse, ListingSearchParams, PaginatedResponse } from '../types/api';

export const search = async (params: ListingSearchParams): Promise<PaginatedResponse<ListingData>> => {
  const response = await client.get<PaginatedResponse<ListingData>>('/listings', { params });
  return response.data;
};

export const getById = async (id: string | number): Promise<ListingDetailResponse> => {
  const response = await client.get<ListingDetailResponse>(`/listings/${id}`);
  return response.data;
};

export interface NearbyRequest {
  lat: number;
  lng: number;
  radius_metres: number;
  limit?: number;
}

export const nearby = async (body: NearbyRequest): Promise<ListingData[]> => {
  const response = await client.post<ListingData[]>('/listings/nearby', body);
  return response.data;
};
