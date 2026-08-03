import type { AuthUser, JwtPayload } from '../types/auth';

function decodeBase64Url(value: string): string {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=');
  return atob(padded);
}

export function parseJwt(token: string): JwtPayload | null {
  try {
    const [, payload] = token.split('.');
    if (!payload) return null;
    return JSON.parse(decodeBase64Url(payload)) as JwtPayload;
  } catch {
    return null;
  }
}

export function getUserFromToken(token: string): AuthUser | null {
  const payload = parseJwt(token);
  if (!payload?.sub || !payload.email || !payload.role) {
    return null;
  }

  return {
    userId: payload.sub,
    email: payload.email,
    role: payload.role,
  };
}

export function isTokenExpired(token: string, skewSeconds = 30): boolean {
  const payload = parseJwt(token);
  if (!payload?.exp) return true;
  const now = Math.floor(Date.now() / 1000);
  return payload.exp <= now + skewSeconds;
}
