export function formatDate(dateString: string | null | undefined): string {
  if (!dateString) return '—';
  const parts = dateString.split('-');
  if (parts.length !== 3) return dateString;
  const year = parts[0];
  const monthIndex = parseInt(parts[1] || '0', 10) - 1;
  const day = parseInt(parts[2] || '0', 10);
  
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const monthName = months[monthIndex] || '';
  
  return `${day} ${monthName} ${year}`;
}

export function formatNaira(kobo: number | null | undefined): string {
  if (kobo === null || kobo === undefined) return '—';
  const naira = Math.round(kobo / 100);
  return `₦${naira.toLocaleString('en-US')}`;
}

export function formatNairaShort(kobo: number | null | undefined): string {
  if (kobo === null || kobo === undefined) return '—';
  const naira = kobo / 100;
  if (naira >= 1_000_000_000) {
    return `₦${(naira / 1_000_000_000).toFixed(1).replace(/\.0$/, '')}B`;
  }
  if (naira >= 1_000_000) {
    return `₦${(naira / 1_000_000).toFixed(1).replace(/\.0$/, '')}M`;
  }
  if (naira >= 1_000) {
    return `₦${(naira / 1_000).toFixed(1).replace(/\.0$/, '')}K`;
  }
  return `₦${naira.toFixed(0)}`;
}

export function formatPercent(value: number | null | undefined): string {
  if (value === null || value === undefined) return '—';
  const sign = value > 0 ? '+' : '';
  return `${sign}${value.toFixed(1)}%`;
}
