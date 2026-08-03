import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

interface AuthLayoutProps {
  children: ReactNode;
}

export function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <div className="auth-layout">
      <main className="auth-layout__main">
        <div className="mb-8 flex justify-center">
          <Link to="/login" className="brand-mark">
            <span className="brand-mark__logo" aria-hidden="true">
              B
            </span>
            Bookstore
          </Link>
        </div>
        <div className="auth-layout__panel animate-rise">{children}</div>
      </main>
    </div>
  );
}
