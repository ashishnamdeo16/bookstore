import { useState, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/AuthProvider';
import { AppFooter } from './Footer';
import { Navbar } from './Navbar';
import { Sidebar, type NavItem } from './Sidebar';

interface PortalShellProps {
  sidebarTitle: string;
  sidebarHomeTo: string;
  navItems: NavItem[];
  heading: string;
  children: ReactNode;
}

export function PortalShell({
  sidebarTitle,
  sidebarHomeTo,
  navItems,
  heading,
  children,
}: PortalShellProps) {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  async function handleLogout() {
    setLoggingOut(true);
    try {
      await logout();
      navigate('/login', { replace: true });
    } finally {
      setLoggingOut(false);
      setSidebarOpen(false);
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
        title={sidebarTitle}
        homeTo={sidebarHomeTo}
        items={navItems}
        open={sidebarOpen}
        onNavigate={() => setSidebarOpen(false)}
      />
      <div className="portal-content">
        <Navbar
          heading={heading}
          onMenuClick={() => setSidebarOpen(true)}
          onLogout={() => void handleLogout()}
          loggingOut={loggingOut}
        />
        <div className="portal-body">
          <main className="portal-main">
            <div className="portal-main__inner animate-rise">{children}</div>
          </main>
          <AppFooter />
        </div>
      </div>
    </div>
  );
}
