import { RegisterForm } from '../features/auth/RegisterForm';
import { AuthLayout } from '../layouts/AuthLayout';

export function RegisterPage() {
  return (
    <AuthLayout>
      <RegisterForm />
    </AuthLayout>
  );
}
