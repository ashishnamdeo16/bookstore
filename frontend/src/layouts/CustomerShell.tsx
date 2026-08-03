import { useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { Navbar } from '../components/layout/Navbar';
import { Sidebar, type NavItem } from '../components/layout/Sidebar';

const CUSTOMER_NAV: NavItem[] = [
  { to: '/dashboard', label: 'Dashboard', end: true },
  { to: '/books', label: 'Browse Books' },
  { to: '/cart', label: 'Cart' },
  { to: '/orders', label: 'My Orders' },
  { to: '/profile', label: 'Profile' },
];

function headingFromPath(pathname: string): string {
  if (pathname.startsWith('/books')) return 'Browse Books';
  if (pathname.startsWith('/cart')) return 'Cart';
  if (pathname.startsWith('/orders')) return 'My Orders';
  if (pathname.startsWith('/profile')) return 'Profile';
  return 'Customer Dashboard';
}

export function CustomerShell() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  async function handleLogout() {
    setLoggingOut(true);
    try {
      await logout();
      navigate('/login', { replace: true });
    } finally {
      setLoggingOut(false);
    }
  }

  return (
    <div className="portal-shell">
      {sidebarOpen ? (
        <button
          type="button"
          className="portal-backdrop"
          aria-label="Close menu"
          onClick={() => setSidebarOpen(false)}
        />
      ) : null}
      <Sidebar
        title="Customer Portal"
        items={CUSTOMER_NAV}
        open={sidebarOpen}
        onNavigate={() => setSidebarOpen(false)}
      />
      <div className="portal-content">
        <Navbar
          heading={headingFromPath(location.pathname)}
          onMenuClick={() => setSidebarOpen(true)}
          onLogout={() => void handleLogout()}
          loggingOut={loggingOut}
        />
        <main className="portal-main">
          <div className="portal-main__inner animate-rise">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
