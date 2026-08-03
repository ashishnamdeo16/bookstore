import { useMemo } from 'react';
import { useAuth } from '../../auth/AuthProvider';
import { Button } from '../ui/Button';

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
        <button type="button" className="portal-navbar__menu" onClick={onMenuClick} aria-label="Open menu">
          Menu
        </button>
        <h1 className="portal-navbar__heading">{heading}</h1>
      </div>
      <div className="portal-navbar__right">
        {user ? (
          <div className="user-chip" title={user.email}>
            <span className="user-chip__email">{user.email}</span>
            <span className="role-badge role-badge--quiet">{user.role}</span>
            <span className="user-chip__avatar" aria-hidden="true">
              {initials}
            </span>
          </div>
        ) : null}
        <Button variant="ghost" onClick={onLogout} loading={loggingOut}>
          Sign out
        </Button>
      </div>
    </header>
  );
}
