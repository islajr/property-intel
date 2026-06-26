import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ExternalLink, ArrowLeft, User, MapPin } from 'lucide-react';
import { listings } from '../api';
import PriceHistoryTimeline from '../components/listing/PriceHistoryTimeline';
import PriceHistoryChart from '../components/listing/PriceHistoryChart';
import { NeighbourhoodContextCard } from '../components/listing/NeighbourhoodContextCard';
import { NearbyListings } from '../components/listing/NearbyListings';
import MapView from '../components/listing/MapView';
import Badge from '../components/primitives/Badge';
import Skeleton from '../components/primitives/Skeleton';
import Button from '../components/primitives/Button';

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

export default function ListingDetail() {
  const { id } = useParams<{ id: string }>();

  const { data: listing, isLoading, isError, refetch } = useQuery({
    queryKey: ['listings', id],
    queryFn: () => listings.getById(id!),
    enabled: !!id,
  });

  if (isLoading) {
    return (
      <div className="listing-detail-container container py-8">
        <div className="mb-6">
          <Skeleton width={120} height={20} />
        </div>
        <div className="detail-layout">
          <div className="detail-main">
            <Skeleton width="90%" height={40} className="mb-4" />
            <Skeleton width="40%" height={28} className="mb-6" />
            <Skeleton width="100%" height={80} className="mb-8" />
            <Skeleton width="100%" height={200} className="mb-6" />
            <Skeleton width="100%" height={300} />
          </div>
          <div className="detail-side">
            <Skeleton width="100%" height={160} className="mb-6" />
            <Skeleton width="100%" height={160} className="mb-6" />
            <Skeleton width="100%" height={240} />
          </div>
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="listing-detail-container container py-12 text-center">
        <h2 className="text-lg font-semibold mb-2">Unable to connect</h2>
        <p className="text-secondary mb-6">We encountered an error loading the listing details. Check your internet connection and try again.</p>
        <div style={{ display: 'flex', gap: 'var(--space-2)', justifyContent: 'center', alignItems: 'center' }}>
          <Button variant="primary" onClick={() => refetch()}>
            Retry
          </Button>
          <Link to="/listings">
            <Button variant="secondary">Back to Listings</Button>
          </Link>
        </div>
      </div>
    );
  }

  if (!listing) {
    return (
      <div className="listing-detail-container container py-12 text-center">
        <h2 className="text-lg font-semibold mb-2">Listing not found</h2>
        <p className="text-secondary mb-6">The property listing you are looking for does not exist or has been removed. Check the URL or try searching again.</p>
        <Link to="/listings" className="inline-block">
          <Button variant="primary">
            <ArrowLeft size={16} style={{ marginRight: 'var(--space-2)' }} />
            Back to Listings
          </Button>
        </Link>
      </div>
    );
  }

  // Calculate days on market (DOM)
  const getDOM = (): number | null => {
    if (listing.listingHistory) {
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

  // Check if we render chart (4 or more price history events)
  const showChart = listing.listingHistory && listing.listingHistory.length >= 4;

  return (
    <div className="listing-detail-container container py-8">
      {/* Back Link */}
      <div className="mb-6">
        <Link to="/listings" className="back-to-listings-btn inline-flex items-center text-xs text-secondary hover:text-primary no-underline font-numeric">
          <ArrowLeft size={14} style={{ marginRight: 'var(--space-1)' }} />
          BACK TO LISTINGS
        </Link>
      </div>

      <div className="detail-layout">
        {/* Left Column: Primary Content */}
        <main className="detail-main">
          {/* Header section with Title & Badges */}
          <div className="detail-header-block mb-6">
            <div className="flex flex-wrap items-center gap-3 mb-2">
              <Badge variant={listing.listingStatus === 'ACTIVE' ? 'amber' : 'neutral'}>
                {listing.listingStatus}
              </Badge>
              {domValue !== null && (
                <Badge variant={getDOMBadgeVariant(domValue)}>
                  {getDOMBadgeLabel(domValue)} (DOM: {domValue})
                </Badge>
              )}
            </div>
            
            <h1 className="detail-title text-2xl font-bold tracking-tight text-primary mb-3">
              {listing.title}
            </h1>
            
            <div className="detail-location flex items-center text-secondary text-sm">
              <MapPin size={16} className="text-secondary mr-1" />
              <span>{listing.neighbourhood}, {listing.city}</span>
            </div>
          </div>

          {/* Pricing Info */}
          <div className="detail-price-block border-y border-default py-6 mb-8 flex flex-wrap justify-between items-center gap-4">
            <div>
              <span className="text-xs text-secondary uppercase tracking-wider font-semibold">Listing Price</span>
              <h2 className="detail-price text-3xl font-bold font-numeric text-amber mt-1">
                {listing.priceFormatted}
              </h2>
            </div>
            <a 
              href={listing.url} 
              target="_blank" 
              rel="noopener noreferrer" 
              className="view-external-btn no-underline"
            >
              <Button variant="primary">
                <ExternalLink size={16} className="mr-2" />
                View Original Listing
              </Button>
            </a>
          </div>

          {/* Specs Details */}
          <section className="detail-specs-card bg-raised border border-default rounded-lg p-6 mb-8">
            <h3 className="section-title text-sm uppercase tracking-wider font-semibold mb-4 text-secondary">
              Property Specifications
            </h3>
            
            <div className="detail-specs-grid">
              <div className="detail-spec-tile border border-default rounded-lg p-4 text-center">
                <span className="tile-label text-xs text-secondary uppercase block mb-1">Type</span>
                <span className="tile-value text-md font-semibold text-primary">
                  {formatPropertyType(listing.propertyType)}
                </span>
              </div>
              <div className="detail-spec-tile border border-default rounded-lg p-4 text-center">
                <span className="tile-label text-xs text-secondary uppercase block mb-1">Bedrooms</span>
                <span className="tile-value text-lg font-semibold text-primary font-numeric">
                  {listing.bedrooms !== null ? listing.bedrooms : '—'}
                </span>
              </div>
              <div className="detail-spec-tile border border-default rounded-lg p-4 text-center">
                <span className="tile-label text-xs text-secondary uppercase block mb-1">Bathrooms</span>
                <span className="tile-value text-lg font-semibold text-primary font-numeric">
                  {listing.bathrooms !== null ? listing.bathrooms : '—'}
                </span>
              </div>
            </div>
          </section>

          {/* Price History Timeline & Chart */}
          <section className="detail-history-section border-t border-default pt-8 mb-8">
            <div className="timeline-and-chart-container">
              <div className="mb-6">
                <PriceHistoryTimeline 
                  history={listing.listingHistory || []}
                  currentPrice={listing.priceKobo}
                  currentPriceFormatted={listing.priceFormatted}
                  status={listing.listingStatus}
                />
              </div>

              {showChart && (
                <div className="mt-8">
                  <PriceHistoryChart history={listing.listingHistory || []} />
                </div>
              )}
            </div>
          </section>
        </main>

        {/* Right Column: Supplementary Sidebar */}
        <aside className="detail-side">
          {/* Neighbourhood analytics */}
          <NeighbourhoodContextCard 
            neighbourhood={listing.neighbourhood}
            listingPrice={listing.priceKobo}
          />

          {/* Minimap (280px tall, centred on coordinates, no controls) */}
          {listing.lat !== null && listing.lng !== null && (
            <div className="detail-map-card bg-raised border border-default rounded-lg overflow-hidden mb-6" style={{ height: 280 }}>
              <MapView
                listings={[listing]}
                center={[listing.lat, listing.lng]}
                zoom={14}
                interactive={false}
              />
            </div>
          )}

          {/* Brokerage/source overview */}
          <div className="detail-brokerage-card bg-raised border border-default rounded-lg p-6 mb-6">
            <h3 className="section-title text-sm uppercase tracking-wider font-semibold mb-4 text-secondary">
              Source details
            </h3>
            
            <div className="flex items-center gap-3 mb-4">
              <div className="source-avatar bg-subtle border border-strong rounded-full p-2.5 text-amber">
                <User size={20} />
              </div>
              <div>
                <h4 className="text-sm font-semibold text-primary">
                  {formatSource(listing.source)}
                </h4>
                <p className="text-xs text-secondary font-numeric">
                  REF: #{listing.id}
                </p>
              </div>
            </div>

            <div className="border-t border-default pt-4">
              <div className="flex justify-between items-center text-xs mb-2">
                <span className="text-secondary">Source Platform</span>
                <span className="text-primary font-semibold">{formatSource(listing.source)}</span>
              </div>
              <div className="flex justify-between items-center text-xs">
                <span className="text-secondary">First Indexed</span>
                <span className="text-primary font-numeric">
                  {listing.listingHistory && listing.listingHistory.length > 0
                    ? listing.listingHistory[0]?.eventDate.split('-').reverse().join('/')
                    : '—'}
                </span>
              </div>
            </div>
          </div>

          {/* Nearby Listings Stub */}
          <NearbyListings lat={listing.lat} lng={listing.lng} />
        </aside>
      </div>
    </div>
  );
}
