import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ThemeToggle } from '../components/ui/ThemeToggle';

interface AuthLayoutProps {
  children: ReactNode;
}

export function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <div className="auth-layout">
      <div className="auth-layout__theme">
        <ThemeToggle />
      </div>

      <aside className="auth-layout__brand-panel" aria-hidden="false">
        <div className="auth-layout__brand-content animate-fade-in">
          <Link to="/login" className="auth-brand">
            <span className="auth-brand__mark" aria-hidden="true">
              B
            </span>
            <span className="auth-brand__name">Bookstore</span>
          </Link>
          <p className="auth-layout__tagline">
            A quieter shelf for finding your next favorite read.
          </p>
        </div>
        <div className="auth-layout__atmosphere" aria-hidden="true" />
      </aside>

      <main className="auth-layout__main">
        <div className="auth-layout__mobile-brand">
          <Link to="/login" className="auth-brand auth-brand--compact">
            <span className="auth-brand__mark" aria-hidden="true">
              B
            </span>
            <span className="auth-brand__name">Bookstore</span>
          </Link>
        </div>
        <div className="auth-layout__panel animate-rise">{children}</div>
      </main>
    </div>
  );
}
