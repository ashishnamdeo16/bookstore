import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { LoadingBlock } from '../components/books/ConfirmDialog';
import { Alert } from '../components/ui/Alert';
import { Button } from '../components/ui/Button';
import { PageHeader } from '../components/ui/PageHeader';
import { toast } from '../components/ui/toast';
import { EditProfileForm } from '../features/profile/EditProfileForm';
import { useProfile } from '../features/profile/useProfile';

export function EditProfilePage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { profile, loading, error, refresh, setProfile } = useProfile(user?.userId);

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
        backTo="/profile"
        backLabel="Back to profile"
        eyebrow="Account"
        title="Edit profile"
        subtitle="Update the details associated with your account."
      />
      <EditProfileForm
        userId={user.userId}
        profile={profile}
        onSaved={(updated) => {
          setProfile(updated);
          toast.success('Profile updated');
          navigate('/profile');
        }}
        onCancel={() => navigate('/profile')}
      />
    </div>
  );
}
