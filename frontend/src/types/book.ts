export interface Book {
  id: string;
  isbn: string;
  title: string;
  description: string | null;
  price: number;
  language: string;
  publishedDate: string | null;
  coverImageUrl: string | null;
  publisherId: string;
  categoryId: string;
  authorIds: string[];
}

export interface BookCreateRequest {
  isbn: string;
  title: string;
  description?: string;
  price: number;
  language: string;
  publishedDate?: string;
  publisherId: string;
  categoryId: string;
  authorIds: string[];
}

export interface Author {
  id: string;
  firstName: string;
  lastName: string;
  biography: string | null;
  country: string | null;
}

export interface Category {
  id: string;
  name: string;
  description: string | null;
}

export interface Publisher {
  id: string;
  name: string;
  address: string | null;
}
