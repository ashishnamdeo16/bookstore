const DEVICE_ID_KEY = 'bookstore.deviceId';

/**
 * UUID that works on HTTP (non-secure) contexts too.
 * `crypto.randomUUID()` is secure-context-only and throws on plain http:// ELB URLs.
 */
export function createClientId(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }

  if (typeof crypto !== 'undefined' && typeof crypto.getRandomValues === 'function') {
    const bytes = new Uint8Array(16);
    crypto.getRandomValues(bytes);
    bytes[6] = (bytes[6] & 0x0f) | 0x40;
    bytes[8] = (bytes[8] & 0x3f) | 0x80;
    const hex = Array.from(bytes, (b) => b.toString(16).padStart(2, '0')).join('');
    return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
  }

  return `dev-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`;
}

function readStoredId(): string | null {
  try {
    return localStorage.getItem(DEVICE_ID_KEY);
  } catch {
    return null;
  }
}

function writeStoredId(deviceId: string): void {
  try {
    localStorage.setItem(DEVICE_ID_KEY, deviceId);
  } catch {
    // Private mode / blocked storage — still return a usable id for this session.
  }
}

/**
 * Stable per-browser device identifier. Reused across logins from the same storage.
 * Never derived from User-Agent.
 */
export function getDeviceId(): string {
  const existing = readStoredId();
  if (existing) {
    return existing;
  }

  const deviceId = createClientId();
  writeStoredId(deviceId);
  return deviceId;
}
