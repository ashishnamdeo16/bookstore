import { useEffect, useState, type FormEvent } from 'react';
import { userService } from '../../api/userService';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { TextArea } from '../../components/ui/TextArea';
import { ApiError } from '../../types/api';
import type { UpdateProfileRequest, UserProfile } from '../../types/user';
import { validateProfile, type ProfileFormErrors } from './profileValidation';

interface EditProfileFormProps {
  userId: string;
  profile: UserProfile;
  onSaved: (profile: UserProfile) => void;
  onCancel: () => void;
}

export function EditProfileForm({ userId, profile, onSaved, onCancel }: EditProfileFormProps) {
  const [values, setValues] = useState<UpdateProfileRequest>({
    firstName: profile.firstName ?? '',
    lastName: profile.lastName ?? '',
    phoneNumber: profile.phoneNumber ?? '',
    dateOfBirth: profile.dateOfBirth ?? '',
    address: profile.address ?? '',
  });
  const [errors, setErrors] = useState<ProfileFormErrors>({});
  const [apiError, setApiError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setValues({
      firstName: profile.firstName ?? '',
      lastName: profile.lastName ?? '',
      phoneNumber: profile.phoneNumber ?? '',
      dateOfBirth: profile.dateOfBirth ?? '',
      address: profile.address ?? '',
    });
  }, [profile]);

  function updateField<K extends keyof UpdateProfileRequest>(
    key: K,
    value: UpdateProfileRequest[K],
  ) {
    setValues((current) => ({ ...current, [key]: value }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setApiError(null);
    setSuccess(null);

    const nextErrors = validateProfile(values);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    setLoading(true);
    try {
      const updated = await userService.updateProfile(userId, {
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        phoneNumber: values.phoneNumber.trim(),
        dateOfBirth: values.dateOfBirth,
        address: values.address.trim(),
      });
      setSuccess('Profile updated successfully.');
      onSaved(updated);
    } catch (err) {
      if (err instanceof ApiError) {
        const details = err.validationErrors.length
          ? `${err.message} ${err.validationErrors.join(' ')}`
          : err.message;
        setApiError(details);
      } else {
        setApiError('Unable to save your profile. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="edit-form" onSubmit={handleSubmit} noValidate>
      <header className="edit-form__header">
        <h1 className="edit-form__title">Edit profile</h1>
        <p className="edit-form__subtitle">Update the details associated with your account.</p>
      </header>

      {apiError ? <Alert variant="error">{apiError}</Alert> : null}
      {success ? <Alert variant="success">{success}</Alert> : null}

      <div className="edit-form__grid">
        <Input
          label="First name"
          name="firstName"
          autoComplete="given-name"
          value={values.firstName}
          onChange={(event) => updateField('firstName', event.target.value)}
          error={errors.firstName}
          required
        />
        <Input
          label="Last name"
          name="lastName"
          autoComplete="family-name"
          value={values.lastName}
          onChange={(event) => updateField('lastName', event.target.value)}
          error={errors.lastName}
          required
        />
        <Input
          label="Phone number"
          name="phoneNumber"
          type="tel"
          inputMode="numeric"
          autoComplete="tel"
          value={values.phoneNumber}
          onChange={(event) =>
            updateField('phoneNumber', event.target.value.replace(/\D/g, '').slice(0, 10))
          }
          error={errors.phoneNumber}
          required
        />
        <Input
          label="Date of birth"
          name="dateOfBirth"
          type="date"
          value={values.dateOfBirth}
          onChange={(event) => updateField('dateOfBirth', event.target.value)}
          error={errors.dateOfBirth}
          required
        />
        <div className="edit-form__full">
          <TextArea
            label="Address"
            name="address"
            autoComplete="street-address"
            value={values.address}
            onChange={(event) => updateField('address', event.target.value)}
            error={errors.address}
            required
          />
        </div>
      </div>

      <div className="edit-form__actions">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={loading}>
          Cancel
        </Button>
        <Button type="submit" loading={loading}>
          Save changes
        </Button>
      </div>
    </form>
  );
}
