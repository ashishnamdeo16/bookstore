import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { Alert } from '../components/ui/Alert';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { EditProfileForm } from '../features/profile/EditProfileForm';
import { useProfile } from '../features/profile/useProfile';

export function EditProfilePage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { profile, loading, error, refresh, setProfile } = useProfile(user?.userId);

  if (loading) {
    return (
      <div className="state-block" role="status" aria-live="polite">
        <Spinner label="Loading profile" />
        <p>Loading your profile…</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="state-block">
        <Alert variant="error" title="Couldn’t load profile">
          {error}
        </Alert>
        <Button variant="secondary" onClick={() => void refresh()}>
          Try again
        </Button>
      </div>
    );
  }

  if (!profile || !user) {
    return (
      <div className="state-block">
        <Alert variant="info" title="No profile yet">
          We couldn’t find profile details for this account.
        </Alert>
      </div>
    );
  }

  return (
    <div className="page-stack">
      <EditProfileForm
        userId={user.userId}
        profile={profile}
        onSaved={(updated) => {
          setProfile(updated);
          navigate('/profile');
        }}
        onCancel={() => navigate('/profile')}
      />
    </div>
  );
}
