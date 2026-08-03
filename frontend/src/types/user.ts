export interface UserProfile {
  userId: string;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  phoneNumber: string | null;
  dateOfBirth: string | null;
  address: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  dateOfBirth: string;
  address: string;
}
