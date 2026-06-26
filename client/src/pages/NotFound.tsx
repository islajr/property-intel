import { Link } from 'react-router-dom';
import Button from '../components/primitives/Button';
import { AlertCircle } from 'lucide-react';

export default function NotFound() {
  return (
    <div 
      className="container" 
      style={{ 
        minHeight: '80vh', 
        display: 'flex', 
        flexDirection: 'column', 
        alignItems: 'center', 
        justifyContent: 'center', 
        gap: 'var(--space-4)',
        textAlign: 'center'
      }}
    >
      <div 
        className="inline-flex items-center justify-center border border-strong"
        style={{ 
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '12px',
          borderRadius: '50%',
          backgroundColor: 'var(--color-bg-subtle)',
          color: 'var(--color-amber-400)',
          marginBottom: 'var(--space-2)'
        }}
      >
        <AlertCircle size={32} />
      </div>
      <h1 className="text-3xl font-numeric" style={{ margin: 0, fontSize: '3rem', fontWeight: 'bold' }}>404</h1>
      <h2 className="text-lg font-bold text-primary" style={{ margin: 0 }}>Page not found</h2>
      <p className="text-sm text-secondary max-w-sm" style={{ margin: 0, lineHeight: '1.5' }}>
        The page you are looking for does not exist, has been moved, or was typed incorrectly. Check the address and try again.
      </p>
      <Link to="/" style={{ marginTop: 'var(--space-4)' }}>
        <Button variant="primary">Return Home</Button>
      </Link>
    </div>
  );
}
