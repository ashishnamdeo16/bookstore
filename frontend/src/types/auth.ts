export interface LoginRequest {
  email: string;
  password: string;
  deviceId: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  dateOfBirth: string;
  address: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn?: number;
}

export interface RegisterResponse {
  userId: string;
  email: string;
  message: string;
}

export interface AuthUser {
  userId: string;
  email: string;
  role: string;
}

export interface JwtPayload {
  sub: string;
  email: string;
  role: string;
  exp: number;
  iat: number;
}
