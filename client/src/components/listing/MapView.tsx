import { useEffect, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import MarkerClusterGroup from 'react-leaflet-cluster';
import type { ListingData } from '../../types/api';
import { formatNaira } from '../../utils/format';
import 'leaflet/dist/leaflet.css';

// Fix Leaflet's default marker icon issue in React environments
// By deleting the default options and defining them inline or using custom icons
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

export interface MapViewProps {
  listings: ListingData[];
  activeListingId?: number | null;
  onMarkerHover?: (id: number | null) => void;
  onMarkerClick?: (id: number) => void;
  center?: [number, number];
  zoom?: number;
  interactive?: boolean;
}

// Custom amber pin creator
const createAmberIcon = (isActive: boolean) => {
  const pinColor = isActive ? 'var(--color-amber-300)' : 'var(--color-amber-400)';
  const width = isActive ? 30 : 24;
  const height = isActive ? 40 : 32;
  
  const svgHtml = `
    <svg width="${width}" height="${height}" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="${pinColor}"/>
    </svg>
  `;

  return L.divIcon({
    html: svgHtml,
    className: `custom-map-pin-${isActive ? 'active' : 'default'}`,
    iconSize: [width, height],
    iconAnchor: [width / 2, height],
    popupAnchor: [0, -height],
  });
};

// Component to dynamically fit map bounds to display all listings
function RecenterMap({ listings }: { listings: ListingData[] }) {
  const map = useMap();
  
  useEffect(() => {
    if (listings.length === 0) return;
    
    const bounds = L.latLngBounds(listings.map(l => [l.lat, l.lng]));
    map.fitBounds(bounds, { padding: [40, 40] });
  }, [listings, map]);

  return null;
}

export default function MapView({
  listings,
  activeListingId,
  onMarkerHover,
  onMarkerClick,
  center = [6.4281, 3.4219], // Default center around Victoria Island, Lagos
  zoom = 12,
  interactive = true,
}: MapViewProps) {
  
  const activeIcon = useMemo(() => createAmberIcon(true), []);
  const defaultIcon = useMemo(() => createAmberIcon(false), []);

  return (
    <div className="map-view-container w-full h-full relative" style={{ minHeight: '300px', height: '100%' }}>
      <MapContainer
        center={center}
        zoom={zoom}
        style={{ width: '100%', height: '100%', borderRadius: 'inherit' }}
        zoomControl={interactive}
        scrollWheelZoom={interactive}
        dragging={interactive}
        doubleClickZoom={interactive}
      >
        {/* CartoDB Dark Matter tile layer for premium dark terminal style */}
        <TileLayer
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
        />

        {/* Recenter bounds on listings changes */}
        {listings.length > 0 && <RecenterMap listings={listings} />}

        <MarkerClusterGroup
          chunkedLoading
          maxClusterRadius={60}
          iconCreateFunction={(cluster: any) => {
            const count = cluster.getChildCount();
            return L.divIcon({
              html: `<div class="custom-map-cluster font-numeric text-xs font-bold flex items-center justify-center rounded-full shadow" style="width: 32px; height: 32px; background-color: var(--color-amber-400); color: var(--color-text-inverse); border: 2px solid var(--color-bg-base);">${count}</div>`,
              className: 'custom-cluster-icon',
              iconSize: [32, 32],
            });
          }}
        >
          {listings.map((item) => {
            const isActive = item.id === activeListingId;
            
            return (
              <Marker
                key={item.id}
                position={[item.lat, item.lng]}
                icon={isActive ? activeIcon : defaultIcon}
                eventHandlers={{
                  mouseover: () => {
                    if (onMarkerHover) onMarkerHover(item.id);
                  },
                  mouseout: () => {
                    if (onMarkerHover) onMarkerHover(null);
                  },
                  click: () => {
                    if (onMarkerClick) onMarkerClick(item.id);
                  },
                }}
              >
                {interactive && (
                  <Popup className="custom-map-popup">
                    <div className="p-1 font-ui">
                      <h4 className="text-xs font-bold text-primary mb-1 truncate-ellipsis max-w-[180px]">
                        {item.title}
                      </h4>
                      <p className="text-xs font-numeric font-semibold text-amber">
                        {item.priceFormatted || formatNaira(item.priceKobo)}
                      </p>
                    </div>
                  </Popup>
                )}
              </Marker>
            );
          })}
        </MarkerClusterGroup>
      </MapContainer>
    </div>
  );
}
