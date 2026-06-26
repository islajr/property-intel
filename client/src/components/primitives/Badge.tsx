import { HTMLAttributes } from 'react';

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: 'neutral' | 'amber' | 'signal-up' | 'signal-down' | 'dom-fresh' | 'dom-active' | 'dom-stale';
  size?: 'standard' | 'compact';
}

export function Badge({
  variant = 'neutral',
  size = 'standard',
  children,
  className = '',
  ...props
}: BadgeProps) {
  return (
    <span
      className={`badge badge-${variant} badge-${size} ${className}`}
      {...props}
    >
      {children}
    </span>
  );
}

export default Badge;
