import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { LoadingBlock } from '../components/books/ConfirmDialog';
import { Alert } from '../components/ui/Alert';
import { Button } from '../components/ui/Button';
import { PageHeader } from '../components/ui/PageHeader';
import { ProfileView } from '../features/profile/ProfileView';
import { useProfile } from '../features/profile/useProfile';

export function ProfilePage() {
  const { user } = useAuth();
  const { profile, loading, error, refresh } = useProfile(user?.userId);

  if (loading) {
    return <LoadingBlock label="Loading profile" />;
  }

  if (error) {
    return (
      <div className="page-stack">
        <Alert variant="error" title="Couldn’t load profile">
          {error}
        </Alert>
        <Button variant="secondary" className="w-fit" onClick={() => void refresh()}>
          Try again
        </Button>
      </div>
    );
  }

  if (!profile || !user) {
    return (
      <div className="page-stack">
        <Alert variant="info" title="No profile yet">
          We couldn’t find profile details for this account.
        </Alert>
      </div>
    );
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Account"
        title="Profile"
        subtitle="Your personal details and contact information."
        actions={
          <Link to="/profile/edit" className="btn btn--secondary">
            Edit profile
          </Link>
        }
      />
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
