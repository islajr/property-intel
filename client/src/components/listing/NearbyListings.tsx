export interface NearbyListingsProps {
  lat: number;
  lng: number;
}

export function NearbyListings({ lat, lng }: NearbyListingsProps) {
  return (
    <div className="nearby-listings bg-raised border border-default rounded-lg p-4">
      <h3 className="text-sm font-semibold mb-2">Nearby Listings ({lat}, {lng})</h3>
      <p className="text-xs text-secondary">Loading nearby properties...</p>
    </div>
  );
}
