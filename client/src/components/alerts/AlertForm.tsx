import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { market, alerts } from '../../api';
import type { PropertyType } from '../../types/api';
import Input from '../primitives/Input';
import Select from '../primitives/Select';
import Button from '../primitives/Button';
import { PlusCircle } from 'lucide-react';

export interface AlertFormProps {
  onSuccess: () => void;
  onCancel?: () => void;
}

const PROPERTY_TYPE_OPTIONS = [
  { value: '', label: 'All Property Types' },
  { value: 'FLAT', label: 'Flat / Apartment' },
  { value: 'DETACHED_BUILDING', label: 'Detached Building' },
  { value: 'SERVICE_APARTMENT', label: 'Service Apartment' },
  { value: 'TERRACED_HOUSE', label: 'Terraced House' },
  { value: 'LAND', label: 'Land' },
  { value: 'COMMERCIAL', label: 'Commercial' },
];

const BEDROOM_OPTIONS = [
  { value: '', label: 'Any Bedrooms' },
  { value: '1', label: '1+ Beds' },
  { value: '2', label: '2+ Beds' },
  { value: '3', label: '3+ Beds' },
  { value: '4', label: '4+ Beds' },
  { value: '5', label: '5+ Beds' },
];

// RFC 4122 v4 compliant UUID generator
const generateUUID = (): string => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
};

export default function AlertForm({ onSuccess, onCancel }: AlertFormProps) {
  // Idempotency Key state
  const [idempotencyKey, setIdempotencyKey] = useState('');

  // Generate idempotency key on mount
  useEffect(() => {
    setIdempotencyKey(generateUUID());
  }, []);

  // Form states
  const [neighbourhood, setNeighbourhood] = useState('');
  const [propertyType, setPropertyType] = useState<string>('');
  const [minBedrooms, setMinBedrooms] = useState<string>('');
  const [maxPriceNaira, setMaxPriceNaira] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch neighbourhoods to populate dropdown
  const { data: neighbourhoodsData } = useQuery({
    queryKey: ['market', 'neighbourhoods-dropdown'],
    queryFn: () => market.getNeighbourhoods({ limit: 50 }),
  });

  const neighbourhoodOptions = [
    { value: '', label: 'Select Neighbourhood *' },
    ...(neighbourhoodsData?.data.map((n) => ({
      value: n.neighbourhood,
      label: n.neighbourhood,
    })) || []),
  ];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!neighbourhood) {
      setError('Please select a neighbourhood.');
      return;
    }

    setLoading(true);
    setError(null);

    // Formulate payload (converting Naira to kobo for backend)
    const maxPriceKobo = maxPriceNaira ? Math.round(parseFloat(maxPriceNaira) * 100) : null;
    const bedrooms = minBedrooms ? parseInt(minBedrooms, 10) : null;
    const propType = (propertyType || null) as PropertyType | null;

    const requestBody = {
      neighbourhood,
      maxPriceKobo,
      minBedrooms: bedrooms,
      propertyType: propType,
    };

    try {
      await alerts.create(requestBody, idempotencyKey);
      // Re-generate idempotency key for next alert
      setIdempotencyKey(generateUUID());
      // Reset form
      setNeighbourhood('');
      setPropertyType('');
      setMinBedrooms('');
      setMaxPriceNaira('');
      
      onSuccess();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to create alert. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="alert-create-form bg-raised border border-default rounded-lg p-6 mb-6">
      <h3 className="section-title text-sm uppercase tracking-wider font-semibold mb-4 text-primary">
        Create New Alert
      </h3>

      {error && (
        <div className="error-banner text-xs mb-4">
          <span>{error}</span>
        </div>
      )}

      <div className="flex flex-col gap-4">
        <Select
          value={neighbourhood}
          onChange={(e) => setNeighbourhood(e.target.value)}
          options={neighbourhoodOptions}
          disabled={loading}
          required
        />

        <div className="form-row grid grid-cols-2 gap-4">
          <Select
            value={propertyType}
            onChange={(e) => setPropertyType(e.target.value)}
            options={PROPERTY_TYPE_OPTIONS}
            disabled={loading}
          />

          <Select
            value={minBedrooms}
            onChange={(e) => setMinBedrooms(e.target.value)}
            options={BEDROOM_OPTIONS}
            disabled={loading}
          />
        </div>

        <Input
          type="number"
          label="Maximum Price (₦)"
          placeholder="e.g. 50000000"
          value={maxPriceNaira}
          onChange={(e) => setMaxPriceNaira(e.target.value)}
          disabled={loading}
          helperText="Leave empty for no limit"
        />

        <div className="flex gap-2 justify-end mt-4">
          {onCancel && (
            <Button 
              type="button" 
              variant="ghost" 
              onClick={onCancel}
              disabled={loading}
            >
              Cancel
            </Button>
          )}
          <Button 
            type="submit" 
            variant="primary" 
            isLoading={loading}
            className="flex items-center gap-1.5"
          >
            <PlusCircle size={14} />
            Create Alert
          </Button>
        </div>
      </div>
    </form>
  );
}
