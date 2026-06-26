import { useState, useEffect, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import { AreaChart, Area, ResponsiveContainer } from 'recharts';
import { market } from '../../api';

export interface SparklineProps {
  neighbourhood: string;
}

export function Sparkline({ neighbourhood }: SparklineProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [isVisible, setIsVisible] = useState(false);

  // Intersection Observer to trigger fetching only when card enters viewport
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry && entry.isIntersecting) {
          setIsVisible(true);
          observer.disconnect(); // Load once and stay loaded
        }
      },
      { threshold: 0.1 }
    );

    if (containerRef.current) {
      observer.observe(containerRef.current);
    }

    return () => {
      observer.disconnect();
    };
  }, []);

  const { data: trendData, isLoading } = useQuery({
    queryKey: ['market', 'trends', 'sparkline', neighbourhood],
    queryFn: () => market.getTrends(neighbourhood),
    enabled: isVisible && !!neighbourhood,
  });

  // Extract the last 4 weeks for the sparkline trend
  const get4WeekData = () => {
    if (!trendData?.weeks || trendData.weeks.length === 0) return [];
    
    // Sort oldest first
    const sorted = [...trendData.weeks].sort(
      (a, b) => new Date(a.week_start).getTime() - new Date(b.week_start).getTime()
    );
    
    // Slice last 4 weeks
    return sorted.slice(-4).map((w) => ({
      price: w.median_price_kobo,
    }));
  };

  const chartData = get4WeekData();

  // Determine trend color (green if positive, rose if negative over the 4-week window)
  const getTrendColor = () => {
    if (chartData.length < 2) return 'var(--color-signal-neutral)';
    const first = chartData[0]?.price ?? 0;
    const last = chartData[chartData.length - 1]?.price ?? 0;
    return last >= first ? 'var(--color-signal-up)' : 'var(--color-signal-down)';
  };

  const color = getTrendColor();

  return (
    <div 
      ref={containerRef} 
      className="sparkline-wrapper flex items-center justify-between gap-4 mt-4 pt-3 border-t border-default"
      style={{ minHeight: 30 }}
    >
      <span className="text-xs text-secondary font-numeric">4-Week Price Trend</span>
      
      <div style={{ width: 60, height: 30 }}>
        {isLoading || !isVisible ? (
          <div className="w-full h-full bg-subtle animate-pulse rounded" />
        ) : chartData.length === 0 ? (
          <span className="text-xs text-tertiary font-numeric">no data</span>
        ) : (
          <ResponsiveContainer>
            <AreaChart data={chartData} margin={{ top: 2, right: 2, left: 2, bottom: 2 }}>
              <defs>
                <linearGradient id={`gradient-${neighbourhood}`} x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor={color} stopOpacity={0.2} />
                  <stop offset="95%" stopColor={color} stopOpacity={0.0} />
                </linearGradient>
              </defs>
              <Area
                type="monotone"
                dataKey="price"
                stroke={color}
                strokeWidth={1.5}
                fillOpacity={1}
                fill={`url(#gradient-${neighbourhood})`}
                dot={false}
              />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}
