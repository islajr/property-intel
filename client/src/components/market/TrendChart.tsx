import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, Tooltip, CartesianGrid, Line, Legend } from 'recharts';
import { TrendWeek } from '../../types/api';
import { formatDate, formatNaira, formatNairaShort } from '../../utils/format';

export interface TrendChartProps {
  weeks: TrendWeek[];
}

export function TrendChart({ weeks }: TrendChartProps) {
  // Sort weeks chronologically (oldest first) and map data
  const chartData = [...weeks]
    .sort((a, b) => new Date(a.week_start).getTime() - new Date(b.week_start).getTime())
    .map(w => ({
      ...w,
      // Left axis (median price in Naira)
      priceNaira: w.median_price_kobo / 100,
    }));

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="custom-chart-tooltip bg-raised border border-strong rounded p-3 shadow-lg">
          <p className="tooltip-date text-xs text-secondary font-numeric mb-2">
            Week of {formatDate(data.week_start)}
          </p>
          <div className="flex flex-col gap-1.5">
            <div className="flex justify-between gap-6 items-center text-xs">
              <span className="text-secondary">Median Price:</span>
              <span className="font-numeric font-bold text-amber">
                {formatNaira(data.median_price_kobo)}
              </span>
            </div>
            {data.active_listings !== undefined && (
              <div className="flex justify-between gap-6 items-center text-xs">
                <span className="text-secondary">Active Listings:</span>
                <span className="font-numeric font-bold text-primary">
                  {data.active_listings}
                </span>
              </div>
            )}
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="trend-chart-card bg-raised border border-default rounded-lg p-6 mb-6">
      <header className="flex justify-between items-baseline mb-6">
        <h3 className="section-title text-sm uppercase tracking-wider font-semibold text-secondary">
          12-Week Pricing & Listing Trend
        </h3>
        <span className="text-xs text-secondary font-numeric">Price vs Volume</span>
      </header>

      <div style={{ width: '100%', height: 300 }}>
        <ResponsiveContainer>
          <AreaChart
            data={chartData}
            margin={{ top: 10, right: 5, left: -10, bottom: 5 }}
          >
            <defs>
              <linearGradient id="trendPriceGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="var(--color-amber-400)" stopOpacity={0.25} />
                <stop offset="95%" stopColor="var(--color-amber-400)" stopOpacity={0.0} />
              </linearGradient>
            </defs>

            <CartesianGrid 
              stroke="var(--color-border)" 
              vertical={false} 
              strokeDasharray="0" 
            />

            <XAxis
              dataKey="week_start"
              tickFormatter={(tick) => {
                const parts = tick.split('-');
                if (parts.length === 3) {
                  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
                  const monthIndex = parseInt(parts[1] || '0', 10) - 1;
                  const day = parseInt(parts[2] || '0', 10);
                  const monthName = months[monthIndex] || '';
                  return `${day} ${monthName}`;
                }
                return tick;
              }}
              tickLine={false}
              axisLine={false}
              tick={{ fill: 'var(--color-text-secondary)', fontSize: 10, fontFamily: 'var(--font-numeric)' }}
            />

            {/* Left YAxis: Median Price */}
            <YAxis
              yAxisId="left"
              tickFormatter={(tick) => formatNairaShort(tick * 100)}
              tickLine={false}
              axisLine={false}
              tick={{ fill: 'var(--color-text-secondary)', fontSize: 10, fontFamily: 'var(--font-numeric)' }}
            />

            {/* Right YAxis: Active Listings */}
            <YAxis
              yAxisId="right"
              orientation="right"
              tickLine={false}
              axisLine={false}
              tick={{ fill: 'var(--color-text-secondary)', fontSize: 10, fontFamily: 'var(--font-numeric)' }}
            />

            <Tooltip
              content={<CustomTooltip />}
              cursor={{ stroke: 'var(--color-border-strong)', strokeWidth: 1 }}
            />

            {/* Price Area (Fill + Line) */}
            <Area
              yAxisId="left"
              type="monotone"
              dataKey="priceNaira"
              stroke="var(--color-amber-400)"
              strokeWidth={2}
              fillOpacity={1}
              fill="url(#trendPriceGradient)"
              dot={false}
              activeDot={{ r: 6, fill: 'var(--color-amber-400)', stroke: 'var(--color-bg-raised)', strokeWidth: 1.5 }}
              animationDuration={600}
              name="Median Price"
            />

            {/* Active Count Line */}
            <Line
              yAxisId="right"
              type="monotone"
              dataKey="active_listings"
              stroke="var(--color-text-secondary)"
              strokeWidth={1.5}
              strokeDasharray="4 4"
              dot={false}
              activeDot={{ r: 4, fill: 'var(--color-text-secondary)', stroke: 'var(--color-bg-raised)', strokeWidth: 1 }}
              animationDuration={600}
              name="Active Listings"
            />

            <Legend 
              verticalAlign="bottom" 
              height={36} 
              iconType="circle" 
              iconSize={8}
              wrapperStyle={{ fontSize: '11px', fontFamily: 'var(--font-ui)', color: 'var(--color-text-secondary)' }}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
