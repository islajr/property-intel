import { forwardRef } from 'react';
import type { InputHTMLAttributes } from 'react';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, className = '', id, disabled, ...props }, ref) => {
    const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;

    return (
      <div className={`form-field-container ${error ? 'has-error' : ''} ${disabled ? 'is-disabled' : ''}`}>
        {label && (
          <label htmlFor={inputId} className="form-field-label">
            {label}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          disabled={disabled}
          className={`form-input ${className}`}
          aria-invalid={!!error}
          aria-describedby={error ? `${inputId}-error` : undefined}
          {...props}
        />
        {error ? (
          <span id={`${inputId}-error`} className="form-field-error" aria-live="polite">
            {error}
          </span>
        ) : helperText ? (
          <span className="form-field-helper">{helperText}</span>
        ) : null}
      </div>
    );
  }
);

Input.displayName = 'Input';
export default Input;
