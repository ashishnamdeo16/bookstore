import {
  isValidDateOfBirth,
  isValidEmail,
  isValidPhone,
  passwordErrors,
} from './authValidation';
import type { RegisterRequest } from '../../types/auth';

export type LoginFormValues = { email: string; password: string };
export type LoginFormErrors = Partial<Record<keyof LoginFormValues, string>>;

export function validateLogin(values: LoginFormValues): LoginFormErrors {
  const errors: LoginFormErrors = {};

  if (!values.email.trim()) {
    errors.email = 'Email is required.';
  } else if (!isValidEmail(values.email)) {
    errors.email = 'Enter a valid email address.';
  }

  if (!values.password) {
    errors.password = 'Password is required.';
  }

  return errors;
}

export type RegisterFormValues = RegisterRequest & { confirmPassword: string };
export type RegisterFormErrors = Partial<Record<keyof RegisterFormValues, string>>;

export function validateRegisterStep(
  step: number,
  values: RegisterFormValues,
): RegisterFormErrors {
  const errors: RegisterFormErrors = {};

  if (step === 1) {
    if (!values.firstName.trim()) errors.firstName = 'First name is required.';
    else if (values.firstName.trim().length > 50) errors.firstName = 'First name is too long.';

    if (!values.lastName.trim()) errors.lastName = 'Last name is required.';
    else if (values.lastName.trim().length > 50) errors.lastName = 'Last name is too long.';

    if (!values.email.trim()) errors.email = 'Email is required.';
    else if (!isValidEmail(values.email)) errors.email = 'Enter a valid email address.';

    const passwordError = passwordErrors(values.password);
    if (passwordError) errors.password = passwordError;

    if (!values.confirmPassword) errors.confirmPassword = 'Confirm your password.';
    else if (values.confirmPassword !== values.password) {
      errors.confirmPassword = 'Passwords do not match.';
    }
  }

  if (step === 2) {
    if (!values.phoneNumber.trim()) errors.phoneNumber = 'Phone number is required.';
    else if (!isValidPhone(values.phoneNumber)) {
      errors.phoneNumber = 'Enter a 10-digit phone number.';
    }

    if (!values.dateOfBirth) errors.dateOfBirth = 'Date of birth is required.';
    else if (!isValidDateOfBirth(values.dateOfBirth)) {
      errors.dateOfBirth = 'Enter a valid date in the past.';
    }

    if (!values.address.trim()) errors.address = 'Address is required.';
    else if (values.address.trim().length > 255) errors.address = 'Address is too long.';
  }

  return errors;
}
