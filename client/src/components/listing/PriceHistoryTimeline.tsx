import type { PriceHistoryResponse } from '../../types/api';
import { formatDate, formatNaira, formatNairaShort } from '../../utils/format';

export interface PriceHistoryTimelineProps {
  history: PriceHistoryResponse[];
  currentPrice: number;
  currentPriceFormatted: string;
  status: string;
}

export default function PriceHistoryTimeline({
  history,
  currentPrice,
  currentPriceFormatted,
  status,
}: PriceHistoryTimelineProps) {
  // Sort history chronologically (oldest first)
  const sortedHistory = [...history].sort(
    (a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime()
  );

  const getEventLabel = (type: string) => {
    switch (type.toUpperCase()) {
      case 'LISTED':
        return 'LISTED';
      case 'PRICE_CHANGE':
        return 'PRICE CHANGE';
      case 'RELISTED':
        return 'RELISTED';
      case 'REMOVED':
        return 'REMOVED';
      default:
        return type;
    }
  };

  // Helper to format price change details
  const renderPriceChangeDetail = (oldVal: number | null, newVal: number | null) => {
    if (oldVal === null || newVal === null) return null;
    const diff = newVal - oldVal;
    if (diff === 0) return null;

    const diffAbs = Math.abs(diff);
    const diffFormatted = formatNairaShort(diffAbs);
    const pct = Math.round((diffAbs / oldVal) * 100);

    if (diff < 0) {
      return (
        <span className="price-change-indicator price-reduced font-numeric text-xs">
          ▼ {diffFormatted} ({pct}%)
        </span>
      );
    } else {
      return (
        <span className="price-change-indicator price-increased font-numeric text-xs">
          ▲ {diffFormatted} ({pct}%)
        </span>
      );
    }
  };

  // Add the current status if not already covered or if active
  const showCurrentState = status.toUpperCase() === 'ACTIVE';

  return (
    <div className="price-history-timeline-container">
      <h3 className="section-title text-sm uppercase tracking-wider font-semibold">Price History</h3>
      
      <div className="timeline-list">
        {sortedHistory.map((event, idx) => {
          const isLast = idx === sortedHistory.length - 1 && !showCurrentState;
          
          return (
            <div key={idx} className="timeline-item">
              <div className="timeline-marker">
                <div className="timeline-dot" aria-hidden="true" />
                {!isLast && <div className="timeline-connector" aria-hidden="true" />}
              </div>
              
              <div className="timeline-content">
                <div className="timeline-header">
                  <span className="timeline-label font-numeric text-xs font-semibold">
                    {getEventLabel(event.eventType)}
                  </span>
                  
                  {event.eventType.toUpperCase() === 'PRICE_CHANGE' && 
                    renderPriceChangeDetail(event.oldValue, event.newValue)
                  }
                </div>
                
                <div className="timeline-details">
                  <span className="timeline-price font-numeric text-md">
                    {formatNaira(event.newValue)}
                  </span>
                  <span className="timeline-date font-numeric text-xs text-secondary">
                    {formatDate(event.eventDate)}
                  </span>
                </div>
              </div>
            </div>
          );
        })}

        {showCurrentState && (
          <div className="timeline-item current-state">
            <div className="timeline-marker">
              <div className="timeline-dot current" aria-hidden="true" />
            </div>
            
            <div className="timeline-content">
              <div className="timeline-header">
                <span className="timeline-label font-numeric text-xs font-semibold current">
                  CURRENT
                </span>
              </div>
              
              <div className="timeline-details">
                <span className="timeline-price font-numeric text-md current">
                  {currentPriceFormatted || formatNaira(currentPrice)}
                </span>
                <span className="timeline-date font-numeric text-xs text-secondary">
                  Today
                </span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
