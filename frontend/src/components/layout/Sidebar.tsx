import { Link, NavLink } from 'react-router-dom';
import { NavIcon, type NavIconName } from './NavIcons';

export interface NavItem {
  to: string;
  label: string;
  end?: boolean;
  icon?: NavIconName;
}

interface SidebarProps {
  title: string;
  homeTo: string;
  items: NavItem[];
  open: boolean;
  onNavigate?: () => void;
}

export function Sidebar({ title, homeTo, items, open, onNavigate }: SidebarProps) {
  return (
    <aside className={`portal-sidebar ${open ? 'is-open' : ''}`} aria-label={title}>
      <Link to={homeTo} className="portal-sidebar__brand" onClick={onNavigate}>
        <span className="brand-mark__logo" aria-hidden="true">
          B
        </span>
        <div>
          <p className="portal-sidebar__product">Bookstore</p>
          <p className="portal-sidebar__title">{title}</p>
        </div>
      </Link>
      <nav className="portal-sidebar__nav">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) => (isActive ? 'is-active' : '')}
            onClick={onNavigate}
          >
            {item.icon ? (
              <span className="portal-sidebar__icon">
                <NavIcon name={item.icon} />
              </span>
            ) : null}
            <span className="portal-sidebar__label">{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
