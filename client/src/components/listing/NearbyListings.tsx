import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { listings } from '../../api';
import { formatNaira } from '../../utils/format';
import Skeleton from '../primitives/Skeleton';
import { BedDouble, Bath } from 'lucide-react';

export interface NearbyListingsProps {
  lat: number;
  lng: number;
}

export function NearbyListings({ lat, lng }: NearbyListingsProps) {
  const navigate = useNavigate();

  const { data: nearbyData, isLoading, isError } = useQuery({
    queryKey: ['listings', 'nearby', lat, lng],
    queryFn: () => listings.nearby({ lat, lng, radius_metres: 5000, limit: 3 }),
    enabled: lat !== undefined && lng !== undefined,
  });

  if (isLoading) {
    return (
      <div className="nearby-listings bg-raised border border-default rounded-lg p-6">
        <Skeleton width="50%" height={16} className="mb-4" />
        <div className="flex flex-col gap-3">
          <Skeleton width="100%" height={60} />
          <Skeleton width="100%" height={60} />
          <Skeleton width="100%" height={60} />
        </div>
      </div>
    );
  }

  if (isError || !nearbyData || nearbyData.length === 0) {
    return (
      <div className="nearby-listings bg-raised border border-default rounded-lg p-6">
        <h3 className="section-title text-sm uppercase tracking-wider font-semibold mb-2 text-secondary">
          Nearby Properties
        </h3>
        <p className="text-xs text-secondary">No nearby properties found within 5km.</p>
      </div>
    );
  }

  return (
    <div className="nearby-listings bg-raised border border-default rounded-lg p-6">
      <h3 className="section-title text-sm uppercase tracking-wider font-semibold mb-4 text-secondary">
        Nearby Properties
      </h3>
      
      <div className="flex flex-col gap-3">
        {nearbyData.map((item) => (
          <div 
            key={item.id} 
            className="nearby-card border border-default rounded-lg p-3 hover:border-strong cursor-pointer transition-colors"
            onClick={() => navigate(`/listings/${item.id}`)}
          >
            <h4 className="nearby-card-title text-xs font-semibold truncate-ellipsis text-primary mb-2" title={item.title}>
              {item.title}
            </h4>
            
            <div className="flex justify-between items-center">
              <span className="nearby-card-price font-numeric text-xs font-bold text-amber">
                {item.priceFormatted || formatNaira(item.priceKobo)}
              </span>
              
              <div className="nearby-card-specs flex items-center gap-2 text-xs text-secondary">
                {item.bedrooms !== null && (
                  <span className="flex items-center gap-1 font-numeric">
                    <BedDouble size={12} />
                    {item.bedrooms}
                  </span>
                )}
                {item.bathrooms !== null && (
                  <span className="flex items-center gap-1 font-numeric">
                    <Bath size={12} />
                    {item.bathrooms}
                  </span>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
