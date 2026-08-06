import { AccountMenu, type AccountMenuItem } from './AccountMenu';
import { IconMenu } from './NavIcons';

interface NavbarProps {
  heading: string;
  onMenuClick: () => void;
  onLogout: () => void;
  loggingOut?: boolean;
  accountItems?: AccountMenuItem[];
}

export function Navbar({ heading, onMenuClick, onLogout, loggingOut, accountItems }: NavbarProps) {
  return (
    <header className="portal-navbar">
      <div className="portal-navbar__left">
        <button
          type="button"
          className="portal-navbar__menu"
          onClick={onMenuClick}
          aria-label="Open menu"
        >
          <IconMenu />
        </button>
        <div className="portal-navbar__titles">
          <p className="portal-navbar__eyebrow">Bookstore</p>
          <h1 className="portal-navbar__heading">{heading}</h1>
        </div>
      </div>
      <div className="portal-navbar__right">
        <AccountMenu items={accountItems} onLogout={onLogout} loggingOut={loggingOut} />
      </div>
    </header>
  );
}
