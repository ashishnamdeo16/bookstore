import { Outlet, useLocation } from 'react-router-dom';
import type { AccountMenuItem } from '../components/layout/AccountMenu';
import { PortalShell } from '../components/layout/PortalShell';
import type { NavItem } from '../components/layout/Sidebar';

const ADMIN_ACCOUNT_ITEMS: AccountMenuItem[] = [
  { to: '/admin/dashboard', label: 'Admin dashboard', icon: 'dashboard' },
  { to: '/admin/users', label: 'Manage users', icon: 'users' },
];

const ADMIN_NAV: NavItem[] = [
  { to: '/admin/dashboard', label: 'Dashboard', end: true, icon: 'dashboard' },
  { to: '/admin/analytics', label: 'Analytics', icon: 'analytics' },
  { to: '/admin/books', label: 'Manage Books', end: true, icon: 'books' },
  { to: '/admin/books/new', label: 'Add Book', icon: 'add' },
  { to: '/admin/authors', label: 'Authors', icon: 'authors' },
  { to: '/admin/publishers', label: 'Publishers', icon: 'publishers' },
  { to: '/admin/categories', label: 'Categories', icon: 'categories' },
  { to: '/admin/users', label: 'Users', icon: 'users' },
];

function headingFromPath(pathname: string): string {
  if (pathname.includes('/admin/books/new')) return 'Add Book';
  if (pathname.includes('/edit')) return 'Edit Book';
  if (pathname.startsWith('/admin/analytics')) return 'Analytics';
  if (pathname.startsWith('/admin/books')) return 'Manage Books';
  if (pathname.startsWith('/admin/authors')) return 'Authors';
  if (pathname.startsWith('/admin/publishers')) return 'Publishers';
  if (pathname.startsWith('/admin/categories')) return 'Categories';
  if (pathname.startsWith('/admin/users')) return 'User Management';
  return 'Admin Dashboard';
}

export function AdminShell() {
  const location = useLocation();

  return (
    <PortalShell
      sidebarTitle="Admin Portal"
      sidebarHomeTo="/admin/dashboard"
      navItems={ADMIN_NAV}
      heading={headingFromPath(location.pathname)}
      accountItems={ADMIN_ACCOUNT_ITEMS}
    >
      <Outlet />
    </PortalShell>
  );
}
