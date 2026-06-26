export interface NeighbourhoodContextCardProps {
  neighbourhood: string;
  listingPrice: number;
}

export function NeighbourhoodContextCard({ neighbourhood }: NeighbourhoodContextCardProps) {
  return (
    <div className="neighbourhood-context-card bg-raised border border-default rounded-lg p-4 mb-4">
      <h3 className="text-sm font-semibold mb-2">Neighbourhood Context ({neighbourhood})</h3>
      <p className="text-xs text-secondary">Loading statistics...</p>
    </div>
  );
}
