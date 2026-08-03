import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { Spinner } from '../components/ui/Spinner';
import { useAuth } from './AuthProvider';
import { homePathForRole, normalizeRole, type AppRole } from './roles';

interface RoleGuardProps {
  allow: AppRole[];
}

export function RoleGuard({ allow }: RoleGuardProps) {
  const { user, isAuthenticated, isInitializing } = useAuth();
  const location = useLocation();

  if (isInitializing) {
    return (
      <div className="boot-screen" role="status" aria-live="polite">
        <Spinner label="Loading" />
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  const role = normalizeRole(user.role);
  if (!role || !allow.includes(role)) {
    return <Navigate to={homePathForRole(user.role)} replace />;
  }

  return <Outlet />;
}
