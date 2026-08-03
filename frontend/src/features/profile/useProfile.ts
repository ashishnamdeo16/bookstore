import { useEffect, useState } from 'react';
import { userService } from '../../api/userService';
import { ApiError } from '../../types/api';
import type { UserProfile } from '../../types/user';

interface UseProfileResult {
  profile: UserProfile | null;
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
  setProfile: (profile: UserProfile) => void;
}

export function useProfile(userId: string | undefined): UseProfileResult {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(Boolean(userId));
  const [error, setError] = useState<string | null>(null);

  const refresh = async () => {
    if (!userId) {
      setProfile(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const data = await userService.getProfile(userId);
      setProfile(data);
    } catch (err) {
      const message =
        err instanceof ApiError ? err.message : 'Unable to load your profile right now.';
      setError(message);
      setProfile(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  return { profile, loading, error, refresh, setProfile };
}
