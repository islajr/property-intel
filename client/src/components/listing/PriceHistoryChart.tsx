import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip } from 'recharts';
import { PriceHistoryResponse } from '../../types/api';
import { formatDate, formatNaira, formatNairaShort } from '../../utils/format';

export interface PriceHistoryChartProps {
  history: PriceHistoryResponse[];
}

export default function PriceHistoryChart({ history }: PriceHistoryChartProps) {
  // Sort history chronologically (oldest first)
  const chartData = [...history]
    .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime())
    .map(event => ({
      ...event,
      // For Recharts to parse numeric y-axis correctly
      priceNaira: event.newValue ? event.newValue / 100 : 0,
    }));

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="custom-chart-tooltip bg-raised border border-strong rounded p-3 shadow-lg">
          <p className="tooltip-date text-xs text-secondary font-numeric mb-1">
            {formatDate(data.eventDate)}
          </p>
          <p className="tooltip-price text-sm font-bold font-numeric text-primary mb-1">
            {formatNaira(data.newValue)}
          </p>
          <p className="tooltip-event text-xs font-semibold text-amber uppercase tracking-wider">
            {data.eventType.replace('_', ' ')}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="price-history-chart-wrapper bg-raised border border-default rounded-lg p-4 mb-6">
      <h3 className="section-title text-sm uppercase tracking-wider font-semibold mb-4 text-secondary">
        Price Trend Timeline
      </h3>
      <div style={{ width: '100%', height: 200 }}>
        <ResponsiveContainer>
          <LineChart
            data={chartData}
            margin={{ top: 10, right: 15, left: -5, bottom: 5 }}
          >
            <XAxis
              dataKey="eventDate"
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
            <YAxis
              tickFormatter={(tick) => formatNairaShort(tick * 100)}
              tickLine={false}
              axisLine={false}
              tick={{ fill: 'var(--color-text-secondary)', fontSize: 10, fontFamily: 'var(--font-numeric)' }}
            />
            <Tooltip
              content={<CustomTooltip />}
              cursor={{ stroke: 'var(--color-border-strong)', strokeWidth: 1, strokeDasharray: '3 3' }}
            />
            <Line
              type="monotone"
              dataKey="priceNaira"
              stroke="var(--color-amber-400)"
              strokeWidth={2}
              dot={{ r: 4, fill: 'var(--color-amber-400)', stroke: 'var(--color-bg-raised)', strokeWidth: 1 }}
              activeDot={{ r: 6, fill: 'var(--color-amber-300)', stroke: 'var(--color-bg-raised)', strokeWidth: 1 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
