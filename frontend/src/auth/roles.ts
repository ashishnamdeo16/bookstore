export const Roles = {
  USER: 'ROLE_USER',
  ADMIN: 'ROLE_ADMIN',
} as const;

export type AppRole = (typeof Roles)[keyof typeof Roles];

/** JWT stores USER/ADMIN; Spring uses ROLE_* — normalize both. */
export function normalizeRole(role: string | null | undefined): AppRole | null {
  if (!role) return null;
  const value = role.trim().toUpperCase();
  if (value === 'USER' || value === 'ROLE_USER') return Roles.USER;
  if (value === 'ADMIN' || value === 'ROLE_ADMIN') return Roles.ADMIN;
  return null;
}

export function isAdmin(role: string | null | undefined): boolean {
  return normalizeRole(role) === Roles.ADMIN;
}

export function isCustomer(role: string | null | undefined): boolean {
  return normalizeRole(role) === Roles.USER;
}

export function homePathForRole(role: string | null | undefined): string {
  return isAdmin(role) ? '/admin/dashboard' : '/dashboard';
}
