import type { ReactNode, ReactElement, SVGProps } from 'react';

type IconProps = SVGProps<SVGSVGElement>;

function IconBase({ children, ...props }: IconProps & { children: ReactNode }) {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.75"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      {...props}
    >
      {children}
    </svg>
  );
}

export function IconDashboard(props: IconProps) {
  return (
    <IconBase {...props}>
      <rect x="3" y="3" width="7" height="9" rx="1.5" />
      <rect x="14" y="3" width="7" height="5" rx="1.5" />
      <rect x="14" y="12" width="7" height="9" rx="1.5" />
      <rect x="3" y="16" width="7" height="5" rx="1.5" />
    </IconBase>
  );
}

export function IconBooks(props: IconProps) {
  return (
    <IconBase {...props}>
      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
    </IconBase>
  );
}

export function IconCart(props: IconProps) {
  return (
    <IconBase {...props}>
      <circle cx="9" cy="20" r="1.5" />
      <circle cx="18" cy="20" r="1.5" />
      <path d="M2 3h2l2.4 12.4a1 1 0 0 0 1 .8h9.7a1 1 0 0 0 1-.8L21 7H6" />
    </IconBase>
  );
}

export function IconOrders(props: IconProps) {
  return (
    <IconBase {...props}>
      <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
      <rect x="9" y="3" width="6" height="4" rx="1" />
      <path d="M9 12h6M9 16h6" />
    </IconBase>
  );
}

export function IconProfile(props: IconProps) {
  return (
    <IconBase {...props}>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 20c1.5-4 6.5-4 8-4s6.5 0 8 4" />
    </IconBase>
  );
}

export function IconAnalytics(props: IconProps) {
  return (
    <IconBase {...props}>
      <path d="M4 19V5" />
      <path d="M4 19h16" />
      <path d="M8 17V11" />
      <path d="M12 17V7" />
      <path d="M16 17v-4" />
    </IconBase>
  );
}

export function IconAdd(props: IconProps) {
  return (
    <IconBase {...props}>
      <path d="M12 5v14M5 12h14" />
    </IconBase>
  );
}

export function IconAuthors(props: IconProps) {
  return (
    <IconBase {...props}>
      <circle cx="9" cy="8" r="3" />
      <circle cx="17" cy="9" r="2.5" />
      <path d="M3 19c0-3 3-5 6-5s6 2 6 5" />
      <path d="M15 19c0-2 2-3.5 4-3.5" />
    </IconBase>
  );
}

export function IconPublishers(props: IconProps) {
  return (
    <IconBase {...props}>
      <path d="M3 9h18v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V9z" />
      <path d="M3 9l2-5h14l2 5" />
      <path d="M12 9v12" />
    </IconBase>
  );
}

export function IconCategories(props: IconProps) {
  return (
    <IconBase {...props}>
      <path d="M4 7h7V4H4v3zM13 4v3h7V4h-7zM4 14h7v-3H4v3zM13 17h7v-3h-7v3z" />
    </IconBase>
  );
}

export function IconUsers(props: IconProps) {
  return (
    <IconBase {...props}>
      <circle cx="12" cy="8" r="3.5" />
      <path d="M4 20c0-4 3.5-6 8-6s8 2 8 6" />
    </IconBase>
  );
}

export function IconMenu(props: IconProps) {
  return (
    <IconBase {...props}>
      <path d="M4 7h16M4 12h16M4 17h16" />
    </IconBase>
  );
}

export type NavIconName =
  | 'dashboard'
  | 'books'
  | 'cart'
  | 'orders'
  | 'profile'
  | 'analytics'
  | 'add'
  | 'authors'
  | 'publishers'
  | 'categories'
  | 'users';

const NAV_ICONS: Record<NavIconName, (props: IconProps) => ReactElement> = {
  dashboard: IconDashboard,
  books: IconBooks,
  cart: IconCart,
  orders: IconOrders,
  profile: IconProfile,
  analytics: IconAnalytics,
  add: IconAdd,
  authors: IconAuthors,
  publishers: IconPublishers,
  categories: IconCategories,
  users: IconUsers,
};

export function NavIcon({ name, ...props }: IconProps & { name: NavIconName }) {
  const Component = NAV_ICONS[name];
  return <Component {...props} />;
}
