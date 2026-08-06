import {
  useCallback,
  useEffect,
  useId,
  useMemo,
  useRef,
  useState,
  type KeyboardEvent as ReactKeyboardEvent,
} from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../auth/AuthProvider';
import { useTheme, type ThemePreference } from '../../theme/ThemeProvider';
import { NavIcon, type NavIconName } from './NavIcons';

export interface AccountMenuItem {
  to: string;
  label: string;
  icon?: NavIconName;
}

interface AccountMenuProps {
  items?: AccountMenuItem[];
  onLogout: () => void;
  loggingOut?: boolean;
}

const THEME_OPTIONS: { value: ThemePreference; label: string }[] = [
  { value: 'light', label: 'Light' },
  { value: 'dark', label: 'Dark' },
  { value: 'system', label: 'Auto' },
];

function displayNameFromEmail(email: string): string {
  const local = email.split('@')[0] ?? '';
  const words = local
    .split(/[._\-+]+/)
    .map((part) => part.replace(/\d+$/, ''))
    .filter(Boolean);

  if (words.length === 0) return 'Account';

  return words.map((word) => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
}

function IconChevron() {
  return (
    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" aria-hidden="true">
      <path
        d="m6 9 6 6 6-6"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function IconSignOut() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" aria-hidden="true">
      <path
        d="M15 17l5-5-5-5M20 12H9M12 20H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h6"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export function AccountMenu({ items = [], onLogout, loggingOut }: AccountMenuProps) {
  const { user } = useAuth();
  const { preference, setPreference } = useTheme();
  const [open, setOpen] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const panelRef = useRef<HTMLDivElement>(null);
  const menuId = useId();

  const email = user?.email ?? '';
  const initials = useMemo(() => email.charAt(0).toUpperCase() || 'U', [email]);
  const name = useMemo(() => displayNameFromEmail(email), [email]);

  const close = useCallback((focusTrigger = false) => {
    setOpen(false);
    if (focusTrigger) triggerRef.current?.focus();
  }, []);

  useEffect(() => {
    if (!open) return;

    function onPointerDown(event: MouseEvent) {
      if (!rootRef.current?.contains(event.target as Node)) setOpen(false);
    }

    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        event.stopPropagation();
        close(true);
      }
    }

    document.addEventListener('mousedown', onPointerDown);
    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('mousedown', onPointerDown);
      document.removeEventListener('keydown', onKeyDown);
    };
  }, [open, close]);

  if (!user) return null;

  function moveFocus(direction: 1 | -1) {
    const focusables = panelRef.current?.querySelectorAll<HTMLElement>('[data-menu-item]');
    if (!focusables || focusables.length === 0) return;

    const list = Array.from(focusables);
    const currentIndex = list.indexOf(document.activeElement as HTMLElement);
    const nextIndex = (currentIndex + direction + list.length) % list.length;
    list[Math.max(nextIndex, 0)]?.focus();
  }

  function onPanelKeyDown(event: ReactKeyboardEvent<HTMLDivElement>) {
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      moveFocus(1);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      moveFocus(-1);
    }
  }

  return (
    <div className="account-menu-root" ref={rootRef}>
      <button
        type="button"
        ref={triggerRef}
        className="account-trigger"
        aria-haspopup="menu"
        aria-expanded={open}
        aria-controls={open ? menuId : undefined}
        onClick={() => setOpen((value) => !value)}
        title={email}
      >
        <span className="account-trigger__avatar" aria-hidden="true">
          {initials}
        </span>
        <span className="account-trigger__meta">
          <span className="account-trigger__name">{name}</span>
          <span className="account-trigger__role">{user.role}</span>
        </span>
        <span className="account-trigger__chevron" aria-hidden="true">
          <IconChevron />
        </span>
        <span className="sr-only">Account menu</span>
      </button>

      {open ? (
        <div
          className="account-menu"
          id={menuId}
          role="menu"
          aria-label="Account"
          ref={panelRef}
          onKeyDown={onPanelKeyDown}
        >
          <div className="account-menu__identity">
            <span className="account-menu__avatar" aria-hidden="true">
              {initials}
            </span>
            <div className="account-menu__identity-text">
              <p className="account-menu__name">{name}</p>
              <p className="account-menu__email" title={email}>
                {email}
              </p>
            </div>
          </div>
          <span className="account-menu__role">{user.role}</span>

          {items.length > 0 ? (
            <>
              <div className="account-menu__divider" />
              <div className="account-menu__group">
                {items.map((item) => (
                  <Link
                    key={item.to}
                    to={item.to}
                    role="menuitem"
                    data-menu-item
                    className="account-menu__item"
                    onClick={() => close()}
                  >
                    {item.icon ? (
                      <span className="account-menu__item-icon">
                        <NavIcon name={item.icon} />
                      </span>
                    ) : null}
                    {item.label}
                  </Link>
                ))}
              </div>
            </>
          ) : null}

          <div className="account-menu__divider" />
          <p className="account-menu__label">Appearance</p>
          <div className="account-theme" role="group" aria-label="Appearance">
            {THEME_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                data-menu-item
                className={`account-theme__option ${preference === option.value ? 'is-active' : ''}`}
                aria-pressed={preference === option.value}
                onClick={() => setPreference(option.value)}
              >
                {option.label}
              </button>
            ))}
          </div>

          <div className="account-menu__divider" />
          <div className="account-menu__group">
            <button
              type="button"
              role="menuitem"
              data-menu-item
              className="account-menu__item account-menu__item--danger"
              onClick={() => {
                close();
                onLogout();
              }}
              disabled={loggingOut}
            >
              <span className="account-menu__item-icon">
                <IconSignOut />
              </span>
              {loggingOut ? 'Signing out…' : 'Sign out'}
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
