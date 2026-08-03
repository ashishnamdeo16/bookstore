import { useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { Navbar } from '../components/layout/Navbar';
import { Sidebar, type NavItem } from '../components/layout/Sidebar';

const ADMIN_NAV: NavItem[] = [
  { to: '/admin/dashboard', label: 'Dashboard', end: true },
  { to: '/admin/books', label: 'Manage Books', end: true },
  { to: '/admin/books/new', label: 'Add Book' },
  { to: '/admin/authors', label: 'Authors' },
  { to: '/admin/publishers', label: 'Publishers' },
  { to: '/admin/categories', label: 'Categories' },
  { to: '/admin/users', label: 'Users' },
];

function headingFromPath(pathname: string): string {
  if (pathname.includes('/admin/books/new')) return 'Add Book';
  if (pathname.includes('/edit')) return 'Edit Book';
  if (pathname.startsWith('/admin/books')) return 'Manage Books';
  if (pathname.startsWith('/admin/authors')) return 'Authors';
  if (pathname.startsWith('/admin/publishers')) return 'Publishers';
  if (pathname.startsWith('/admin/categories')) return 'Categories';
  if (pathname.startsWith('/admin/users')) return 'User Management';
  return 'Admin Dashboard';
}

export function AdminShell() {
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
        title="Admin Portal"
        items={ADMIN_NAV}
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
