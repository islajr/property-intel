import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { login } from '../api/auth';
import AuthForm from '../components/auth/AuthForm';
import Input from '../components/primitives/Input';
import Button from '../components/primitives/Button';
import { AlertCircle } from 'lucide-react';

export default function Login() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { setToken } = useAuthStore();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const redirectTarget = searchParams.get('redirect') || '/';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please fill in all fields.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const res = await login({ email, password });
      setToken(res.accessToken, res.expiresIn);
      navigate(redirectTarget, { replace: true });
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Invalid email or password. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthForm title="Welcome back" subtitle="Sign in to manage your property alerts">
      <form onSubmit={handleSubmit} className="auth-form flex flex-col gap-4">
        {error && (
          <div className="error-banner text-xs">
            <AlertCircle size={16} className="text-secondary flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <Input
          type="email"
          label="Email address"
          placeholder="name@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          disabled={loading}
          autoComplete="email"
          required
        />

        <Input
          type="password"
          label="Password"
          placeholder="••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          disabled={loading}
          autoComplete="current-password"
          required
        />

        <Button 
          type="submit" 
          variant="primary" 
          isLoading={loading}
          className="w-full justify-center mt-2"
        >
          Sign In
        </Button>

        <footer className="auth-footer text-center mt-4">
          <p className="text-xs text-secondary">
            Don't have an account?{' '}
            <Link to={`/register?redirect=${encodeURIComponent(redirectTarget)}`} className="text-amber hover:underline no-underline">
              Register
            </Link>
          </p>
        </footer>
      </form>
    </AuthForm>
  );
}
