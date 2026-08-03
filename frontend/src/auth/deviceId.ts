const DEVICE_ID_KEY = 'bookstore.deviceId';

/**
 * Stable per-browser device identifier. Reused across logins from the same storage.
 * Never derived from User-Agent.
 */
export function getDeviceId(): string {
  const existing = localStorage.getItem(DEVICE_ID_KEY);
  if (existing) {
    return existing;
  }

  const deviceId = crypto.randomUUID();
  localStorage.setItem(DEVICE_ID_KEY, deviceId);
  return deviceId;
}
