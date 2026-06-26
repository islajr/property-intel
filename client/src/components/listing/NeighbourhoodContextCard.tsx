import { useQuery } from '@tanstack/react-query';
import { market } from '../../api';
import { formatNaira, formatNairaShort } from '../../utils/format';
import Skeleton from '../primitives/Skeleton';

export interface NeighbourhoodContextCardProps {
  neighbourhood: string;
  listingPrice: number;
}

export function NeighbourhoodContextCard({ neighbourhood, listingPrice }: NeighbourhoodContextCardProps) {
  const { data: stats, isLoading, isError } = useQuery({
    queryKey: ['market', 'stats', neighbourhood],
    queryFn: () => market.getStats(neighbourhood),
    enabled: !!neighbourhood,
  });

  if (isLoading) {
    return (
      <div className="detail-context-card bg-raised border border-default rounded-lg p-6 mb-6">
        <Skeleton width="60%" height={16} className="mb-4" />
        <Skeleton width="100%" height={40} className="mb-4" />
        <Skeleton width="100%" height={20} />
      </div>
    );
  }

  if (isError || !stats) {
    return (
      <div className="detail-context-card bg-raised border border-default rounded-lg p-6 mb-6">
        <h3 className="section-title text-sm uppercase tracking-wider font-semibold mb-2 text-secondary">
          Neighbourhood context
        </h3>
        <p className="text-xs text-secondary">Stats currently unavailable for this area.</p>
      </div>
    );
  }

  // Calculate difference percentage
  const median = stats.medianPriceKobo;
  const diffPct = median > 0 ? Math.round(((listingPrice - median) / median) * 100) : 0;
  
  const getDiffText = () => {
    if (diffPct > 0) {
      return (
        <span className="price-diff-indicator text-signal-up font-semibold">
          {diffPct}% above median
        </span>
      );
    }
    if (diffPct < 0) {
      return (
        <span className="price-diff-indicator text-signal-down font-semibold">
          {Math.abs(diffPct)}% below median
        </span>
      );
    }
    return <span className="price-diff-indicator text-secondary font-semibold">Equal to median</span>;
  };

  const p25 = stats.pricePercentiles.p25;
  const p75 = stats.pricePercentiles.p75;

  return (
    <div className="detail-context-card bg-raised border border-default rounded-lg p-6 mb-6">
      <header className="flex justify-between items-center mb-4">
        <h3 className="section-title text-sm uppercase tracking-wider font-semibold text-secondary">
          Area Context
        </h3>
        <span className="context-location-badge text-xs font-semibold text-amber uppercase font-numeric">
          {neighbourhood}
        </span>
      </header>

      <div className="context-comparison mb-4">
        <div className="flex justify-between items-baseline mb-1">
          <span className="text-secondary text-xs">Area Median</span>
          <span className="font-numeric text-md font-bold text-primary">
            {formatNaira(median)}
          </span>
        </div>
        <div className="flex justify-between items-center text-xs">
          <span className="text-secondary">Compared to Median</span>
          {getDiffText()}
        </div>
      </div>

      <div className="context-percentiles border-t border-default pt-4">
        <span className="text-xs text-secondary uppercase tracking-wider block mb-3 font-semibold">
          Market Interquartile Range
        </span>
        
        <div className="percentile-range-bar-wrapper flex items-center justify-between text-xs font-numeric text-secondary mb-2">
          <span>{formatNairaShort(p25)}</span>
          <span className="text-tertiary">P25 – P75</span>
          <span>{formatNairaShort(p75)}</span>
        </div>
        
        {/* Visual range visualization bar */}
        <div className="percentile-visual-bar bg-subtle h-1.5 rounded-full overflow-hidden relative">
          <div 
            className="percentile-indicator-bar bg-amber-400 h-full absolute" 
            style={{ left: '25%', right: '25%' }}
          />
        </div>
      </div>
    </div>
  );
}
