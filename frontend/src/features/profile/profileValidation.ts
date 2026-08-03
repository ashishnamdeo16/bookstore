import { isValidDateOfBirth, isValidPhone } from '../auth/authValidation';
import type { UpdateProfileRequest } from '../../types/user';

export type ProfileFormErrors = Partial<Record<keyof UpdateProfileRequest, string>>;

export function validateProfile(values: UpdateProfileRequest): ProfileFormErrors {
  const errors: ProfileFormErrors = {};

  if (!values.firstName.trim()) errors.firstName = 'First name is required.';
  else if (values.firstName.trim().length > 50) errors.firstName = 'First name is too long.';

  if (!values.lastName.trim()) errors.lastName = 'Last name is required.';
  else if (values.lastName.trim().length > 50) errors.lastName = 'Last name is too long.';

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

  return errors;
}
