import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/AuthProvider';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { PasswordField } from '../../components/ui/PasswordField';
import { TextArea } from '../../components/ui/TextArea';
import { ApiError } from '../../types/api';
import {
  type RegisterFormErrors,
  type RegisterFormValues,
  validateRegisterStep,
} from './validateForms';

const INITIAL_VALUES: RegisterFormValues = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  phoneNumber: '',
  dateOfBirth: '',
  address: '',
};

export function RegisterForm() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [values, setValues] = useState<RegisterFormValues>(INITIAL_VALUES);
  const [errors, setErrors] = useState<RegisterFormErrors>({});
  const [apiError, setApiError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  function updateField<K extends keyof RegisterFormValues>(key: K, value: RegisterFormValues[K]) {
    setValues((current) => ({ ...current, [key]: value }));
  }

  function handleContinue(event: FormEvent) {
    event.preventDefault();
    setApiError(null);
    const nextErrors = validateRegisterStep(1, values);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length === 0) {
      setStep(2);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setApiError(null);

    const nextErrors = validateRegisterStep(2, values);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    setLoading(true);
    try {
      await register({
        email: values.email.trim(),
        password: values.password,
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        phoneNumber: values.phoneNumber.trim(),
        dateOfBirth: values.dateOfBirth,
        address: values.address.trim(),
      });
      navigate('/login', {
        replace: true,
        state: { registered: true, email: values.email.trim() },
      });
    } catch (err) {
      if (err instanceof ApiError) {
        const details = err.validationErrors.length
          ? `${err.message} ${err.validationErrors.join(' ')}`
          : err.message;
        setApiError(details);
      } else {
        setApiError('Unable to create your account. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="auth-form" onSubmit={step === 1 ? handleContinue : handleSubmit} noValidate>
      <div className="auth-form__header">
        <p className="auth-form__step" aria-live="polite">
          Step {step} of 2
        </p>
        <h1 className="auth-form__title">Create your account</h1>
        <p className="auth-form__subtitle">
          {step === 1
            ? 'Start with your identity and sign-in credentials.'
            : 'Add contact details so your profile is ready.'}
        </p>
      </div>

      <div className="stepper" aria-hidden="true">
        <span className={`stepper__dot ${step >= 1 ? 'is-active' : ''}`} />
        <span className={`stepper__line ${step >= 2 ? 'is-active' : ''}`} />
        <span className={`stepper__dot ${step >= 2 ? 'is-active' : ''}`} />
      </div>

      {apiError ? <Alert variant="error">{apiError}</Alert> : null}

      {step === 1 ? (
        <div className="auth-form__grid" key="step-1">
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
          <div className="auth-form__full">
            <Input
              label="Email"
              name="email"
              type="email"
              autoComplete="email"
              value={values.email}
              onChange={(event) => updateField('email', event.target.value)}
              error={errors.email}
              required
            />
          </div>
          <div className="auth-form__full">
            <PasswordField
              label="Password"
              name="password"
              autoComplete="new-password"
              value={values.password}
              onChange={(event) => updateField('password', event.target.value)}
              error={errors.password}
              hint="At least 8 characters, with a letter and a number."
              required
            />
          </div>
          <div className="auth-form__full">
            <PasswordField
              label="Confirm password"
              name="confirmPassword"
              autoComplete="new-password"
              value={values.confirmPassword}
              onChange={(event) => updateField('confirmPassword', event.target.value)}
              error={errors.confirmPassword}
              required
            />
          </div>
        </div>
      ) : (
        <div className="auth-form__stack" key="step-2">
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
            hint="10 digits, no spaces or dashes."
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
      )}

      <div className="auth-form__actions">
        {step === 2 ? (
          <Button type="button" variant="secondary" onClick={() => setStep(1)} disabled={loading}>
            Back
          </Button>
        ) : null}
        <Button type="submit" loading={loading} className="auth-form__submit">
          {step === 1 ? 'Continue' : 'Create account'}
        </Button>
      </div>

      <p className="auth-form__footer">
        Already registered? <Link to="/login">Sign in</Link>
      </p>
    </form>
  );
}
