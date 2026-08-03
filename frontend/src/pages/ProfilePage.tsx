import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { Alert } from '../components/ui/Alert';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { ProfileView } from '../features/profile/ProfileView';
import { useProfile } from '../features/profile/useProfile';

export function ProfilePage() {
  const { user } = useAuth();
  const { profile, loading, error, refresh } = useProfile(user?.userId);

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
      <div className="page-toolbar">
        <p className="page-eyebrow">Account</p>
        <Link to="/profile/edit" className="btn btn--secondary">
          Edit profile
        </Link>
      </div>
      <ProfileView
        firstName={profile.firstName}
        lastName={profile.lastName}
        email={user.email}
        phoneNumber={profile.phoneNumber}
        dateOfBirth={profile.dateOfBirth}
        address={profile.address}
        role={user.role}
      />
    </div>
  );
}
