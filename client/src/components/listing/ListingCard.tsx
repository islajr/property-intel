import { ExternalLink, BedDouble, Bath, Clock } from 'lucide-react';
import type { ListingData, ListingDetailResponse } from '../../types/api';
import Badge from '../primitives/Badge';
import { formatNairaShort } from '../../utils/format';

export interface ListingCardProps {
  listing: ListingData | ListingDetailResponse;
  daysOnMarket?: number;          // Optional explicit days on market
  onClick?: () => void;
}

// Format source names properly
const formatSource = (source: string): string => {
  switch (source.toLowerCase()) {
    case 'propertypro':
      return 'PropertyPro';
    case 'privateproperty':
      return 'Private Property';
    case 'nigeriapropertycentre':
      return 'Nigeria Property Centre';
    case 'jiji':
      return 'Jiji';
    default:
      return source;
  }
};

// Format property types for display
const formatPropertyType = (type: string): string => {
  return type.replace(/_/g, ' ').toUpperCase();
};

export function ListingCard({ listing, daysOnMarket, onClick }: ListingCardProps) {
  // Helper to resolve DOM (Days on Market)
  const getDOM = (): number | null => {
    if (daysOnMarket !== undefined) return daysOnMarket;
    
    // Check if listing has history (ListingDetailResponse)
    if ('listingHistory' in listing && listing.listingHistory) {
      const listedEvent = listing.listingHistory.find(
        (e) => e.eventType === 'LISTED'
      );
      if (listedEvent) {
        const listedDate = new Date(listedEvent.eventDate);
        const today = new Date();
        const diffTime = Math.abs(today.getTime() - listedDate.getTime());
        return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      }
    }
    return null;
  };

  const domValue = getDOM();

  const getDOMBadgeVariant = (dom: number) => {
    if (dom < 14) return 'dom-fresh';
    if (dom <= 45) return 'dom-active';
    return 'dom-stale';
  };

  const getDOMBadgeLabel = (dom: number) => {
    if (dom < 14) return 'Fresh';
    if (dom <= 45) return 'Active';
    return 'Stale';
  };

  // Parse price signal if history is available
  const getPriceSignal = () => {
    if (!('listingHistory' in listing) || !listing.listingHistory) return null;
    
    const priceChanges = listing.listingHistory.filter(
      (e) => e.eventType === 'PRICE_CHANGE'
    );
    
    if (priceChanges.length === 0) return null;
    
    // Most recent first
    const sortedChanges = [...priceChanges].sort(
      (a, b) => new Date(b.eventDate).getTime() - new Date(a.eventDate).getTime()
    );
    
    const latestChange = sortedChanges[0];
    if (!latestChange || latestChange.oldValue === null || latestChange.newValue === null) return null;
    
    const diff = latestChange.newValue - latestChange.oldValue;
    if (diff === 0) return null;
    
    const diffAbs = Math.abs(diff);
    const pct = Math.round((diffAbs / latestChange.oldValue) * 100);
    
    const changeDate = new Date(latestChange.eventDate);
    const today = new Date();
    const diffTime = Math.abs(today.getTime() - changeDate.getTime());
    const daysSince = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    return {
      isDrop: diff < 0,
      amount: diffAbs,
      percent: pct,
      daysSince,
    };
  };

  const priceSignal = getPriceSignal();

  // Safe spec display (use em-dash on null/missing values)
  const renderSpec = (val: number | null | undefined, suffix: string) => {
    if (val === null || val === undefined) {
      return (
        <span className="text-tertiary">
          — {suffix}
        </span>
      );
    }
    return <span className="font-numeric">{val} {suffix}</span>;
  };

  return (
    <article className="listing-card" onClick={onClick}>
      <header className="card-header">
        <div className="card-header-meta">
          <span className="property-type-tag">
            {formatPropertyType(listing.propertyType)}
          </span>
          <span className="card-header-divider">·</span>
          <span className="neighbourhood-tag truncate-ellipsis" title={listing.neighbourhood}>
            {listing.neighbourhood}
          </span>
        </div>
        <Badge variant={listing.listingStatus === 'ACTIVE' ? 'amber' : 'neutral'} size="compact">
          {listing.listingStatus}
        </Badge>
      </header>

      <section className="card-specs">
        <div className="spec-item">
          <BedDouble size={16} aria-hidden="true" />
          {renderSpec(listing.bedrooms, 'BED')}
        </div>
        <div className="spec-divider">·</div>
        <div className="spec-item">
          <Bath size={16} aria-hidden="true" />
          {renderSpec(listing.bathrooms, 'BATH')}
        </div>
        <div className="spec-divider">·</div>
        <div className="spec-item">
          <span className="font-numeric text-tertiary">— m²</span>
        </div>
      </section>

      <section className="card-price-block">
        <h3 className="card-price font-numeric">
          {listing.priceFormatted}
        </h3>
        {priceSignal && (
          <div className={`price-signal-container ${priceSignal.isDrop ? 'reduced' : 'increased'} text-xs font-numeric`}>
            <span className="price-signal-badge">
              {priceSignal.isDrop ? '▼' : '▲'} {formatNairaShort(priceSignal.amount)} ({priceSignal.percent}%)
            </span>
            <span className="price-signal-time text-secondary"> {priceSignal.daysSince}d ago</span>
          </div>
        )}
      </section>

      <footer className="card-footer">
        <div className="card-dom-container">
          {domValue !== null ? (
            <div className="dom-badge-group">
              <Clock size={16} className="text-secondary" aria-hidden="true" />
              <span className="text-sm">Listed {domValue} days ago</span>
              <Badge variant={getDOMBadgeVariant(domValue)} size="compact">
                {getDOMBadgeLabel(domValue)}
              </Badge>
            </div>
          ) : (
            <div className="dom-badge-group">
              <Clock size={16} className="text-tertiary" aria-hidden="true" />
              <span className="text-tertiary text-sm">—</span>
            </div>
          )}
        </div>
        <div className="card-source-container">
          <span className="source-label text-sm">
            {formatSource(listing.source)}
          </span>
          <a 
            href={listing.url} 
            target="_blank" 
            rel="noopener noreferrer" 
            className="external-source-link"
            onClick={(e) => e.stopPropagation()}
            aria-label={`Open listing details on ${formatSource(listing.source)}`}
          >
            <ExternalLink size={14} />
          </a>
        </div>
      </footer>
    </article>
  );
}

export default ListingCard;
