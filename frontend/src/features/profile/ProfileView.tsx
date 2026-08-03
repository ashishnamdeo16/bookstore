interface ProfileFieldProps {
  label: string;
  value: string | null | undefined;
}

function ProfileField({ label, value }: ProfileFieldProps) {
  const display = value?.trim() ? value : 'Not provided';
  const empty = !value?.trim();

  return (
    <div className="profile-field">
      <dt className="profile-field__label">{label}</dt>
      <dd className={`profile-field__value ${empty ? 'is-empty' : ''}`.trim()}>{display}</dd>
    </div>
  );
}

interface ProfileViewProps {
  firstName: string | null;
  lastName: string | null;
  email: string;
  phoneNumber: string | null;
  dateOfBirth: string | null;
  address: string | null;
  role: string;
}

function formatDate(value: string | null): string | null {
  if (!value) return null;
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(date);
}

export function ProfileView({
  firstName,
  lastName,
  email,
  phoneNumber,
  dateOfBirth,
  address,
  role,
}: ProfileViewProps) {
  const fullName = [firstName, lastName].filter(Boolean).join(' ') || 'Your profile';

  return (
    <section className="profile-panel">
      <header className="profile-panel__header">
        <div>
          <h1 className="profile-panel__title">{fullName}</h1>
          <p className="profile-panel__email">{email}</p>
        </div>
        <span className="role-badge" title="Assigned by the server">
          {role}
        </span>
      </header>

      <dl className="profile-grid">
        <ProfileField label="First name" value={firstName} />
        <ProfileField label="Last name" value={lastName} />
        <ProfileField label="Phone number" value={phoneNumber} />
        <ProfileField label="Date of birth" value={formatDate(dateOfBirth)} />
        <div className="profile-grid__full">
          <ProfileField label="Address" value={address} />
        </div>
      </dl>
    </section>
  );
}
