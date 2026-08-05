import { useMemo } from 'react';
import { useAuth } from '../../auth/AuthProvider';
import { Button } from '../ui/Button';
import { ThemeToggle } from '../ui/ThemeToggle';
import { IconMenu } from './NavIcons';

interface NavbarProps {
  heading: string;
  onMenuClick: () => void;
  onLogout: () => void;
  loggingOut?: boolean;
}

export function Navbar({ heading, onMenuClick, onLogout, loggingOut }: NavbarProps) {
  const { user } = useAuth();
  const initials = useMemo(() => user?.email?.charAt(0).toUpperCase() ?? 'U', [user?.email]);

  return (
    <header className="portal-navbar">
      <div className="portal-navbar__left">
        <button
          type="button"
          className="portal-navbar__menu"
          onClick={onMenuClick}
          aria-label="Open menu"
        >
          <IconMenu />
        </button>
        <div className="portal-navbar__titles">
          <p className="portal-navbar__eyebrow">Bookstore</p>
          <h1 className="portal-navbar__heading">{heading}</h1>
        </div>
      </div>
      <div className="portal-navbar__right">
        {user ? (
          <div className="user-chip user-chip--navbar" title={user.email}>
            <span className="user-chip__avatar" aria-hidden="true">
              {initials}
            </span>
            <span className="user-chip__meta">
              <span className="user-chip__email">{user.email}</span>
              <span className="role-badge role-badge--quiet">{user.role}</span>
            </span>
          </div>
        ) : null}
        <div className="portal-navbar__actions">
          <ThemeToggle />
          <Button variant="ghost" size="sm" onClick={onLogout} loading={loggingOut}>
            Sign out
          </Button>
        </div>
      </div>
    </header>
  );
}
