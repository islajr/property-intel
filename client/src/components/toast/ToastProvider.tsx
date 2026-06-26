import { createContext, useContext, useState, useCallback } from 'react';
import type { ReactNode } from 'react';
import { X, CheckCircle, AlertCircle, Info, AlertTriangle } from 'lucide-react';

export interface Toast {
  id: string;
  message: string;
  description?: string;
  type: 'success' | 'error' | 'info' | 'warning';
}

interface ToastContextType {
  addToast: (message: string, type?: Toast['type'], description?: string) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}

interface ToastProviderProps {
  children: ReactNode;
}

export function ToastProvider({ children }: ToastProviderProps) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback((message: string, type: Toast['type'] = 'info', description?: string) => {
    const id = Math.random().toString(36).substring(2, 9);
    
    setToasts((prev) => {
      const newToast: Toast = { id, message, type };
      if (description !== undefined) {
        newToast.description = description;
      }
      const newToasts = [...prev, newToast];
      if (newToasts.length > 3) {
        return newToasts.slice(newToasts.length - 3);
      }
      return newToasts;
    });

    // Auto-dismiss after 5s unless it's an error
    if (type !== 'error') {
      setTimeout(() => {
        removeToast(id);
      }, 5000);
    }
  }, [removeToast]);

  return (
    <ToastContext.Provider value={{ addToast, removeToast }}>
      {children}
      <div 
        className="toast-container" 
        style={{
          position: 'fixed',
          bottom: '24px',
          right: '24px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px',
          zIndex: 2000,
          pointerEvents: 'none'
        }}
        role="log"
        aria-live="polite"
      >
        {toasts.map((toast) => {
          let AccentColor = 'var(--color-amber-400)';
          let IconComponent = Info;

          if (toast.type === 'success') {
            AccentColor = 'var(--color-signal-up)';
            IconComponent = CheckCircle;
          } else if (toast.type === 'error') {
            AccentColor = 'var(--color-signal-down)';
            IconComponent = AlertCircle;
          } else if (toast.type === 'warning') {
            AccentColor = 'var(--color-dom-active)';
            IconComponent = AlertTriangle;
          }

          return (
            <div
              key={toast.id}
              className={`toast-item toast-${toast.type}`}
              style={{
                width: '360px',
                background: 'var(--color-bg-overlay)',
                border: '1px solid var(--color-border-strong)',
                borderRadius: '12px',
                padding: 'var(--space-4) var(--space-5)',
                display: 'flex',
                gap: '12px',
                position: 'relative',
                overflow: 'hidden',
                pointerEvents: 'auto',
                boxShadow: '0 4px 12px rgba(0, 0, 0, 0.4)',
                animation: 'slideInToast 200ms cubic-bezier(0.2, 0.0, 0.0, 1.0)'
              }}
            >
              {/* Left border indicator */}
              <div 
                style={{
                  position: 'absolute',
                  left: 0,
                  top: 0,
                  bottom: 0,
                  width: '3px',
                  backgroundColor: AccentColor
                }}
              />

              <div style={{ color: AccentColor, flexShrink: 0, marginTop: '2px' }}>
                <IconComponent size={18} />
              </div>

              <div style={{ flexGrow: 1, display: 'flex', flexDirection: 'column', gap: '4px', textAlign: 'left' }}>
                <p 
                  className="text-sm font-semibold" 
                  style={{ margin: 0, color: 'var(--color-text-primary)' }}
                >
                  {toast.message}
                </p>
                {toast.description && (
                  <p 
                    className="text-xs" 
                    style={{ margin: 0, color: 'var(--color-text-secondary)', lineHeight: '1.4' }}
                  >
                    {toast.description}
                  </p>
                )}
              </div>

              <button
                onClick={() => removeToast(toast.id)}
                aria-label="Dismiss notification"
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--color-text-tertiary)',
                  cursor: 'pointer',
                  padding: '2px',
                  display: 'flex',
                  alignSelf: 'flex-start',
                  marginTop: '2px'
                }}
                className="hover:text-primary"
              >
                <X size={16} />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}
