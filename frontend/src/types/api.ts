export interface ApiErrorBody {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  validationErrors?: string[];
}

export class ApiError extends Error {
  status: number;
  validationErrors: string[];

  constructor(status: number, message: string, validationErrors: string[] = []) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.validationErrors = validationErrors;
  }
}
