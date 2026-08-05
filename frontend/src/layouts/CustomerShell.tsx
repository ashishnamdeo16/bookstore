import { Outlet, useLocation } from 'react-router-dom';
import { PortalShell } from '../components/layout/PortalShell';
import type { NavItem } from '../components/layout/Sidebar';
import { useCart } from '../features/cart/CartContext';

function buildNav(cartCount: number): NavItem[] {
  return [
    { to: '/dashboard', label: 'Dashboard', end: true, icon: 'dashboard' },
    { to: '/books', label: 'Browse Books', icon: 'books' },
    {
      to: '/cart',
      label: cartCount > 0 ? `Cart (${cartCount})` : 'Cart',
      icon: 'cart',
    },
    { to: '/orders', label: 'My Orders', icon: 'orders' },
    { to: '/profile', label: 'Profile', icon: 'profile' },
  ];
}

function headingFromPath(pathname: string): string {
  if (pathname.startsWith('/books')) return 'Browse Books';
  if (pathname.startsWith('/checkout')) return 'Checkout';
  if (pathname.startsWith('/cart')) return 'Cart';
  if (pathname.startsWith('/orders')) return 'My Orders';
  if (pathname.startsWith('/profile')) return 'Profile';
  return 'Customer Dashboard';
}

export function CustomerShell() {
  const { itemCount } = useCart();
  const location = useLocation();

  return (
    <PortalShell
      sidebarTitle="Customer Portal"
      sidebarHomeTo="/dashboard"
      navItems={buildNav(itemCount)}
      heading={headingFromPath(location.pathname)}
    >
      <Outlet />
    </PortalShell>
  );
}
