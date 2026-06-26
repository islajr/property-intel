import { HTMLAttributes } from 'react';

export interface SkeletonProps extends HTMLAttributes<HTMLDivElement> {
  width?: string | number;
  height?: string | number;
  borderRadius?: string | number;
}

export function Skeleton({
  width = '100%',
  height = '20px',
  borderRadius = '4px',
  className = '',
  style,
  ...props
}: SkeletonProps) {
  const customStyle = {
    width: typeof width === 'number' ? `${width}px` : width,
    height: typeof height === 'number' ? `${height}px` : height,
    borderRadius: typeof borderRadius === 'number' ? `${borderRadius}px` : borderRadius,
    ...style,
  };

  return (
    <div
      className={`skeleton-block ${className}`}
      style={customStyle}
      {...props}
    />
  );
}

export default Skeleton;
