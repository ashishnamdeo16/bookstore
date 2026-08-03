import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { Spinner } from '../components/ui/Spinner';
import { useAuth } from './AuthProvider';
import { homePathForRole } from './roles';

export function ProtectedRoute() {
  const { isAuthenticated, isInitializing } = useAuth();
  const location = useLocation();

  if (isInitializing) {
    return (
      <div className="boot-screen" role="status" aria-live="polite">
        <Spinner label="Loading your account" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}

export function GuestRoute() {
  const { user, isAuthenticated, isInitializing } = useAuth();

  if (isInitializing) {
    return (
      <div className="boot-screen" role="status" aria-live="polite">
        <Spinner label="Loading" />
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to={homePathForRole(user?.role)} replace />;
  }

  return <Outlet />;
}
