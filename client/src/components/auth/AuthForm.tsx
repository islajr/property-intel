import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

export interface AuthFormProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
}

export default function AuthForm({ title, subtitle, children }: AuthFormProps) {
  return (
    <div className="auth-page-wrapper flex items-center justify-center py-16 px-4">
      <div className="auth-card bg-raised border border-default rounded-lg p-8 w-full max-w-sm">
        {/* Brand header */}
        <header className="auth-card-header text-center mb-6">
          <Link to="/" className="auth-brand-logo inline-flex items-center justify-center mb-4 no-underline">
            <span className="logo-pi font-numeric text-xl font-bold tracking-wider mr-2 text-amber">PI</span>
            <span className="logo-text text-sm font-semibold tracking-wide text-primary">Property Intel</span>
          </Link>
          <h2 className="auth-card-title text-lg font-bold text-primary">{title}</h2>
          {subtitle && <p className="auth-card-subtitle text-xs text-secondary mt-1">{subtitle}</p>}
        </header>

        {/* Form content */}
        {children}
      </div>
    </div>
  );
}
