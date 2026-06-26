import { useState } from 'react';
import { MarketPercentiles } from '../../types/api';
import { formatNaira, formatNairaShort } from '../../utils/format';

export interface PricePercentilesBarProps {
  percentiles: MarketPercentiles;
}

export function PricePercentilesBar({ percentiles }: PricePercentilesBarProps) {
  const [hoveredPercentile, setHoveredPercentile] = useState<string | null>(null);

  // Extrapolate P10 since it's not provided by the backend DTO
  // p25 is generally higher than p10. Let's make p10 = p25 - 0.75 * (p50 - p25)
  const p25 = percentiles.p25;
  const p50 = percentiles.p50; // Median
  const p75 = percentiles.p75;
  const p90 = percentiles.p90;
  
  const p10 = Math.max(Math.round(p25 * 0.6), Math.round(p25 - (p50 - p25) * 0.75));

  const totalRange = p90 - p10;
  
  // Calculate relative positions on a 0-100% scale
  const getPct = (val: number) => {
    if (totalRange <= 0) return 0;
    return Math.min(100, Math.max(0, ((val - p10) / totalRange) * 100));
  };

  const markers = [
    { label: 'P10', val: p10, pct: 0 },
    { label: 'P25', val: p25, pct: getPct(p25) },
    { label: 'P50', val: p50, pct: getPct(p50), isMedian: true },
    { label: 'P75', val: p75, pct: getPct(p75) },
    { label: 'P90', val: p90, pct: 100 },
  ];

  return (
    <div className="price-percentiles-container bg-raised border border-default rounded-lg p-6 mb-6">
      <header className="mb-6 flex justify-between items-baseline">
        <h3 className="section-title text-sm uppercase tracking-wider font-semibold text-secondary">
          Price Distribution (Percentiles)
        </h3>
        <span className="text-xs text-secondary font-numeric">P10 – P90 range</span>
      </header>

      {/* Visual Bar & Markers */}
      <div className="percentiles-bar-wrapper relative pt-8 pb-8 px-4">
        {/* Price Labels (Top) */}
        <div className="percentile-labels-top absolute top-0 left-4 right-4 h-6">
          {markers.map((marker) => (
            <div 
              key={marker.label}
              className={`percentile-label-price absolute text-xs font-numeric font-bold transition-all duration-150 ${
                hoveredPercentile === marker.label ? 'text-amber scale-105' : 'text-secondary'
              }`}
              style={{ 
                left: `${marker.pct}%`, 
                transform: `translateX(-50%) ${hoveredPercentile === marker.label ? 'scale(1.05)' : 'scale(1)'}` 
              }}
            >
              {formatNairaShort(marker.val)}
            </div>
          ))}
        </div>

        {/* The Gradient Bar */}
        <div className="percentiles-gradient-bar h-2 rounded-full relative bg-subtle">
          <div 
            className="percentiles-gradient-fill h-full rounded-full"
            style={{
              background: `linear-gradient(to right, var(--color-signal-up) 0%, var(--color-amber-300) 50%, var(--color-signal-down) 100%)`
            }}
          />

          {/* Interactive Marker Dots on the Bar */}
          {markers.map((marker) => (
            <button
              key={marker.label}
              onMouseEnter={() => setHoveredPercentile(marker.label)}
              onMouseLeave={() => setHoveredPercentile(null)}
              className={`percentile-marker-dot absolute rounded-full top-1/2 -translate-y-1/2 focus:outline-none transition-all duration-150 ${
                marker.isMedian 
                  ? 'w-5 h-5 bg-amber border-2 border-raised z-10 shadow-md' 
                  : 'w-3 h-3 bg-raised border-2 border-secondary hover:border-amber hover:scale-125'
              }`}
              style={{ 
                left: `${marker.pct}%`,
                borderColor: marker.isMedian 
                  ? 'var(--color-bg-raised)' 
                  : hoveredPercentile === marker.label ? 'var(--color-amber-400)' : 'var(--color-border-strong)'
              }}
              aria-label={`${marker.label} Percentile Price: ${formatNaira(marker.val)}`}
            />
          ))}
        </div>

        {/* Percentile Markers Labels (Bottom) */}
        <div className="percentile-labels-bottom absolute bottom-0 left-4 right-4 h-6">
          {markers.map((marker) => (
            <div 
              key={marker.label}
              className={`percentile-label-name absolute text-xs font-numeric font-semibold transition-colors duration-150 ${
                hoveredPercentile === marker.label ? 'text-amber' : 'text-tertiary'
              }`}
              style={{ left: `${marker.pct}%`, transform: 'translateX(-50%)' }}
            >
              {marker.label}
            </div>
          ))}
        </div>
      </div>

      {/* Explanatory text under the bar (Plain language interpretation) */}
      <footer className="percentile-interpretation border-t border-default pt-4 mt-2">
        <p className="text-sm text-secondary leading-relaxed">
          Most active listings in this neighbourhood are priced between{' '}
          <strong className="text-primary font-numeric">{formatNaira(p25)}</strong> (P25) and{' '}
          <strong className="text-primary font-numeric">{formatNaira(p75)}</strong> (P75), with a median price of{' '}
          <strong className="text-amber font-numeric">{formatNaira(p50)}</strong> (P50).
        </p>
      </footer>
    </div>
  );
}
