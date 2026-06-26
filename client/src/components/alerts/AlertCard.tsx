import { useState, useEffect, useRef } from 'react';
import type { Alert } from '../../types/api';
import { formatDate, formatNaira } from '../../utils/format';
import Button from '../primitives/Button';
import { Trash2 } from 'lucide-react';

export interface AlertCardProps {
  alert: Alert;
  onDelete: (id: number) => Promise<void>;
}

// Format property types for display
const formatPropertyType = (type: string | null | undefined): string => {
  if (!type) return 'All Types';
  return type.replace(/_/g, ' ').toUpperCase();
};

export default function AlertCard({ alert, onDelete }: AlertCardProps) {
  const [isConfirming, setIsConfirming] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Clear timeout on unmount
  useEffect(() => {
    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, []);

  /**
   * Triggers the alert deletion flow. Clicking once activates a 3-second inline
   * confirmation state ("CONFIRM DELETE?"). Clicking again within 3 seconds executes
   * the deletion; otherwise, the state resets back to "DELETE".
   */
  const handleDeleteClick = async (e: React.MouseEvent) => {
    e.stopPropagation();
    
    if (!isConfirming) {
      setIsConfirming(true);
      // Revert after 3 seconds if not confirmed
      timerRef.current = setTimeout(() => {
        setIsConfirming(false);
      }, 3000);
    } else {
      // Confirmed, execute deletion
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
      setDeleting(true);
      try {
        await onDelete(alert.id);
      } catch (err) {
        setDeleting(false);
        setIsConfirming(false);
      }
    }
  };

  const getCriteriaSummary = () => {
    const parts = [];
    if (alert.propertyType) {
      parts.push(formatPropertyType(alert.propertyType));
    } else {
      parts.push('All Property Types');
    }
    
    if (alert.minBedrooms !== null && alert.minBedrooms !== undefined) {
      parts.push(`${alert.minBedrooms}+ Beds`);
    }

    return parts.join(' · ');
  };

  return (
    <article className="alert-card bg-raised border border-default rounded-lg p-6 mb-4 flex flex-col justify-between">
      <div>
        <header className="flex justify-between items-center mb-4">
          <div className="flex items-center gap-2">
            <span 
              className="alert-status-dot w-2 h-2 rounded-full" 
              style={{ background: alert.isActive ? 'var(--color-signal-up)' : 'var(--color-text-tertiary)' }}
              aria-hidden="true" 
            />
            <span className="alert-status-label text-xs font-semibold tracking-wider text-secondary uppercase font-numeric">
              {alert.isActive ? 'ACTIVE' : 'INACTIVE'}
            </span>
          </div>

          <Button
            onClick={handleDeleteClick}
            variant={isConfirming ? 'destructive' : 'ghost'}
            size="sm"
            isLoading={deleting}
            className={`alert-delete-btn flex items-center gap-1.5 transition-all text-xs font-numeric ${
              isConfirming ? 'px-3' : 'text-secondary hover:text-secondary-destructive'
            }`}
          >
            {!deleting && <Trash2 size={12} />}
            {isConfirming ? 'CONFIRM DELETE?' : 'DELETE'}
          </Button>
        </header>

        <h3 className="alert-neighbourhood-title text-md font-bold text-primary mb-2">
          {alert.neighbourhood}
        </h3>

        <div className="alert-criteria text-xs text-secondary mb-1">
          {getCriteriaSummary()}
        </div>

        {alert.maxPriceKobo !== null && alert.maxPriceKobo !== undefined ? (
          <div className="alert-price text-xs text-secondary">
            Max Price:{' '}
            <strong className="text-primary font-numeric">
              {formatNaira(alert.maxPriceKobo)}
            </strong>
          </div>
        ) : (
          <div className="alert-price text-xs text-tertiary">
            No Price Limit
          </div>
        )}
      </div>

      <footer className="alert-card-footer border-t border-default pt-4 mt-6">
        <span className="alert-date text-xs text-secondary font-numeric">
          Created {formatDate(alert.createdAt)}
        </span>
      </footer>
    </article>
  );
}
