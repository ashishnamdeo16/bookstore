import { useMemo, useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { Button } from '../components/ui/Button';

export function AppShell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  const initials = useMemo(() => {
    if (!user?.email) return 'U';
    return user.email.charAt(0).toUpperCase();
  }, [user?.email]);

  async function handleLogout() {
    setLoggingOut(true);
    try {
      await logout();
      navigate('/login', { replace: true });
    } finally {
      setLoggingOut(false);
      setMobileOpen(false);
    }
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-header__inner">
          <Link to="/books" className="brand-mark brand-mark--compact">
            <span className="brand-mark__logo" aria-hidden="true">
              B
            </span>
            Bookstore
          </Link>

          <nav className="app-nav app-nav--desktop" aria-label="Primary">
            <NavLink to="/books" className={({ isActive }) => (isActive ? 'is-active' : '')}>
              Books
            </NavLink>
            <NavLink to="/profile" className={({ isActive }) => (isActive ? 'is-active' : '')} end>
              Profile
            </NavLink>
            <NavLink
              to="/profile/edit"
              className={({ isActive }) => (isActive ? 'is-active' : '')}
            >
              Edit profile
            </NavLink>
          </nav>

          <div className="app-header__meta">
            {user ? (
              <div className="user-chip" title={user.email}>
                <span className="user-chip__email">{user.email}</span>
                <span className="role-badge role-badge--quiet">{user.role}</span>
                <span className="user-chip__avatar" aria-hidden="true">
                  {initials}
                </span>
              </div>
            ) : null}
            <Button
              variant="ghost"
              className="app-header__logout"
              onClick={() => void handleLogout()}
              loading={loggingOut}
            >
              Sign out
            </Button>
            <button
              type="button"
              className="app-header__menu"
              aria-expanded={mobileOpen}
              aria-controls="mobile-nav"
              onClick={() => setMobileOpen((open) => !open)}
            >
              Menu
            </button>
          </div>
        </div>

        {mobileOpen ? (
          <nav id="mobile-nav" className="app-nav app-nav--mobile" aria-label="Mobile">
            <NavLink
              to="/books"
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) => (isActive ? 'is-active' : '')}
            >
              Books
            </NavLink>
            <NavLink
              to="/profile"
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) => (isActive ? 'is-active' : '')}
              end
            >
              Profile
            </NavLink>
            <NavLink
              to="/profile/edit"
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) => (isActive ? 'is-active' : '')}
            >
              Edit profile
            </NavLink>
            <button
              type="button"
              className="app-nav__logout"
              onClick={() => void handleLogout()}
              disabled={loggingOut}
            >
              {loggingOut ? 'Signing out…' : 'Sign out'}
            </button>
          </nav>
        ) : null}
      </header>

      <main className="app-main">
        <div className="app-main__inner animate-rise">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
