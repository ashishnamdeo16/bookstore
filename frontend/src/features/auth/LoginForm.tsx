import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/AuthProvider';
import { homePathForRole } from '../../auth/roles';
import { getUserFromToken } from '../../auth/jwt';
import { tokenStorage } from '../../auth/tokenStorage';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { PasswordField } from '../../components/ui/PasswordField';
import { ApiError } from '../../types/api';
import { validateLogin } from './validateForms';

export function LoginForm() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [apiError, setApiError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setApiError(null);

    const nextErrors = validateLogin({ email, password });
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    setLoading(true);
    try {
      await login({ email: email.trim(), password });
      const role = getUserFromToken(tokenStorage.getAccessToken() ?? '')?.role;
      navigate(homePathForRole(role), { replace: true });
    } catch (err) {
      if (err instanceof ApiError) {
        setApiError(err.message || 'Invalid email or password.');
      } else {
        setApiError('Unable to sign in. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit} noValidate>
      <div className="auth-form__header">
        <p className="auth-form__eyebrow">Welcome back</p>
        <h1 className="auth-form__title">Sign in</h1>
        <p className="auth-form__subtitle">Enter your credentials to open your shelf.</p>
      </div>

      {apiError ? <Alert variant="error">{apiError}</Alert> : null}

      <div className="auth-form__stack">
        <Input
          label="Email"
          name="email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          error={errors.email}
          required
        />

        <PasswordField
          label="Password"
          name="password"
          autoComplete="current-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          error={errors.password}
          required
        />
      </div>

      <Button type="submit" loading={loading} fullWidth size="lg" className="auth-form__submit">
        Sign in
      </Button>

      <p className="auth-form__footer">
        New here? <Link to="/register">Create an account</Link>
      </p>
    </form>
  );
}
