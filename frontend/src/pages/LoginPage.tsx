import { useLocation } from 'react-router-dom';
import { LoginForm } from '../features/auth/LoginForm';
import { Alert } from '../components/ui/Alert';
import { AuthLayout } from '../layouts/AuthLayout';

interface LocationState {
  registered?: boolean;
  email?: string;
}

export function LoginPage() {
  const location = useLocation();
  const state = (location.state as LocationState | null) ?? null;

  return (
    <AuthLayout>
      {state?.registered ? (
        <Alert variant="success" title="Account created">
          Your account is ready{state.email ? ` for ${state.email}` : ''}. Sign in to continue.
        </Alert>
      ) : null}
      <LoginForm />
    </AuthLayout>
  );
}
