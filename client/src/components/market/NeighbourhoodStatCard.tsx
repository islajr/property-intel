import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { market } from '../../api';
import { formatNaira } from '../../utils/format';
import Badge from '../primitives/Badge';
import Skeleton from '../primitives/Skeleton';

export interface NeighbourhoodStatCardProps {
  name: string;
  city?: string;
  // Optional pre-loaded stats to avoid duplicate fetching
  initialStats?: any;
}

export function NeighbourhoodStatCard({ name, city, initialStats }: NeighbourhoodStatCardProps) {
  const navigate = useNavigate();

  // Fetch stats if not provided
  const { data: stats, isLoading, isError } = useQuery({
    queryKey: ['market', 'stats', name],
    queryFn: () => market.getStats(name),
    enabled: !initialStats && !!name,
    initialData: initialStats,
  });

  const handleCardClick = () => {
    navigate(`/market/${encodeURIComponent(name)}`);
  };

  if (isLoading) {
    return (
      <div className="neighbourhood-stat-card loading bg-raised border border-default rounded-lg p-6">
        <div className="flex justify-between mb-4">
          <Skeleton width="50%" height={18} />
          <Skeleton width="20%" height={18} />
        </div>
        <Skeleton width="40%" height={24} className="mb-4" />
        <Skeleton width="80%" height={16} className="mb-2" />
        <Skeleton width="60%" height={16} />
      </div>
    );
  }

  if (isError || !stats) {
    return (
      <div className="neighbourhood-stat-card error bg-raised border border-default rounded-lg p-6 flex flex-col justify-between">
        <div>
          <div className="flex justify-between items-start mb-2">
            <h3 className="text-md font-semibold text-primary truncate-ellipsis">{name}</h3>
            <span className="text-xs text-secondary">{city || 'Lagos'}</span>
          </div>
          <div className="border-t border-default my-3" />
          <p className="text-xs text-secondary">Stats currently unavailable for this area.</p>
        </div>
        <button 
          onClick={handleCardClick}
          className="text-xs text-amber font-numeric mt-4 text-left hover:underline"
        >
          VIEW DETAILS →
        </button>
      </div>
    );
  }

  const medianPrice = stats.medianPriceKobo;
  const activeCount = stats.activeListingCount;
  const avgDom = stats.avgDaysOnMarket;
  const newCount = stats.newListingsCount;
  const reducedCount = stats.priceReducedCount;

  // Resolve WoW Change Badge
  const wowChange = stats.vsLastWeek?.medianPriceChangePct;
  const renderWoWBadge = () => {
    if (wowChange === null || wowChange === undefined) {
      return <Badge variant="neutral" size="compact">—</Badge>;
    }
    const pctStr = `${wowChange > 0 ? '+' : ''}${wowChange.toFixed(1)}%`;
    if (wowChange > 0) {
      return <Badge variant="signal-up" size="compact">{pctStr}</Badge>;
    }
    if (wowChange < 0) {
      return <Badge variant="signal-down" size="compact">{pctStr}</Badge>;
    }
    return <Badge variant="neutral" size="compact">0.0%</Badge>;
  };

  return (
    <article 
      className="neighbourhood-stat-card bg-raised border border-default hover:border-strong rounded-lg p-6 cursor-pointer transition-all flex flex-col justify-between"
      onClick={handleCardClick}
    >
      <div>
        <header className="flex justify-between items-start mb-2">
          <h3 className="text-md font-bold text-primary truncate-ellipsis" title={name}>
            {name}
          </h3>
          <span className="text-xs text-secondary font-numeric uppercase tracking-wider">
            {city || stats.city || 'Lagos'}
          </span>
        </header>

        <div className="border-t border-default my-3" />

        <div className="median-price-row flex justify-between items-baseline mb-4">
          <div>
            <span className="text-xs text-secondary block mb-1">Median Price</span>
            <span className="text-xl font-bold font-numeric text-primary">
              {stats.formattedMedianPrice || formatNaira(medianPrice)}
            </span>
          </div>
          {renderWoWBadge()}
        </div>

        <div className="secondary-stats-row flex flex-col gap-2 text-xs text-secondary border-t border-default pt-3">
          <div className="flex justify-between items-center font-numeric">
            <span>Active Listings</span>
            <span className="text-primary font-semibold">{activeCount} active</span>
          </div>
          <div className="flex justify-between items-center font-numeric">
            <span>Avg Days on Market</span>
            <span className="text-primary font-semibold">
              {avgDom !== null && avgDom !== undefined ? `${avgDom.toFixed(1)} days` : '—'}
            </span>
          </div>
          <div className="flex justify-between items-center font-numeric">
            <span>New Listings (7d)</span>
            <span className="text-primary font-semibold">{newCount} new</span>
          </div>
          <div className="flex justify-between items-center font-numeric">
            <span>Price Reduced</span>
            <span className="text-primary font-semibold">{reducedCount} reduced</span>
          </div>
        </div>
      </div>

      {/* Sparkline placeholder for B-13 */}
      <div className="sparkline-container-stub mt-4" style={{ height: 30 }} />
    </article>
  );
}
