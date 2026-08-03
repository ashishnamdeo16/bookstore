import { NavLink } from 'react-router-dom';

export interface NavItem {
  to: string;
  label: string;
  end?: boolean;
}

interface SidebarProps {
  title: string;
  items: NavItem[];
  open: boolean;
  onNavigate?: () => void;
}

export function Sidebar({ title, items, open, onNavigate }: SidebarProps) {
  return (
    <aside className={`portal-sidebar ${open ? 'is-open' : ''}`} aria-label={title}>
      <div className="portal-sidebar__brand">
        <span className="brand-mark__logo" aria-hidden="true">
          B
        </span>
        <div>
          <p className="portal-sidebar__product">Bookstore</p>
          <p className="portal-sidebar__title">{title}</p>
        </div>
      </div>
      <nav className="portal-sidebar__nav">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) => (isActive ? 'is-active' : '')}
            onClick={onNavigate}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
