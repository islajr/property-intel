import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { register } from '../api/auth';
import AuthForm from '../components/auth/AuthForm';
import Input from '../components/primitives/Input';
import Button from '../components/primitives/Button';
import { AlertCircle } from 'lucide-react';
import { useToast } from '../components/toast/ToastProvider';

export default function Register() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { setToken } = useAuthStore();
  const { addToast } = useToast();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const redirectTarget = searchParams.get('redirect') || '/';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password || !confirmPassword) {
      setError('Please fill in all fields.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const res = await register({ email, password });
      setToken(res.accessToken, res.expiresIn);
      addToast('Account created', 'success');
      navigate(redirectTarget, { replace: true });
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Registration failed. Please try again.';
      setError(msg);
      addToast('Registration failed', 'error', msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthForm title="Create an account" subtitle="Get notified when properties match your preferences">
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
          autoComplete="new-password"
          required
        />

        <Input
          type="password"
          label="Confirm password"
          placeholder="••••••••"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          disabled={loading}
          autoComplete="new-password"
          required
        />

        <Button 
          type="submit" 
          variant="primary" 
          isLoading={loading}
          className="w-full justify-center mt-2"
        >
          Register
        </Button>

        <footer className="auth-footer text-center mt-4">
          <p className="text-xs text-secondary">
            Already have an account?{' '}
            <Link to={`/login?redirect=${encodeURIComponent(redirectTarget)}`} className="text-amber hover:underline no-underline">
              Sign In
            </Link>
          </p>
        </footer>
      </form>
    </AuthForm>
  );
}
