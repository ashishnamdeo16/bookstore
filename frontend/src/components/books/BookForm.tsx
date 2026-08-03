import { useMemo, useState, type FormEvent } from 'react';
import type { Author, BookCreateRequest, Category, Publisher } from '../../types/book';
import { authorName } from '../../hooks/useBooks';

export interface BookFormValues {
  isbn: string;
  title: string;
  description: string;
  price: string;
  language: string;
  publishedDate: string;
  categoryId: string;
  publisherId: string;
  authorIds: string[];
}

interface BookFormProps {
  initialValues?: Partial<BookFormValues>;
  authors: Author[];
  categories: Category[];
  publishers: Publisher[];
  submitLabel: string;
  loading?: boolean;
  onSubmit: (payload: BookCreateRequest) => Promise<void>;
  onCancel: () => void;
}

const EMPTY: BookFormValues = {
  isbn: '',
  title: '',
  description: '',
  price: '',
  language: '',
  publishedDate: '',
  categoryId: '',
  publisherId: '',
  authorIds: [],
};

export function BookForm({
  initialValues,
  authors,
  categories,
  publishers,
  submitLabel,
  loading = false,
  onSubmit,
  onCancel,
}: BookFormProps) {
  const [values, setValues] = useState<BookFormValues>({ ...EMPTY, ...initialValues });
  const [errors, setErrors] = useState<Partial<Record<keyof BookFormValues, string>>>({});
  const [formError, setFormError] = useState<string | null>(null);

  const sortedAuthors = useMemo(
    () => [...authors].sort((a, b) => authorName(a).localeCompare(authorName(b))),
    [authors],
  );

  function updateField<K extends keyof BookFormValues>(key: K, value: BookFormValues[K]) {
    setValues((current) => ({ ...current, [key]: value }));
  }

  function toggleAuthor(id: string) {
    setValues((current) => {
      const selected = current.authorIds.includes(id)
        ? current.authorIds.filter((authorId) => authorId !== id)
        : [...current.authorIds, id];
      return { ...current, authorIds: selected };
    });
  }

  function validate(): boolean {
    const next: Partial<Record<keyof BookFormValues, string>> = {};
    if (!values.isbn.trim()) next.isbn = 'ISBN is required.';
    if (!values.title.trim()) next.title = 'Title is required.';
    if (!values.price.trim() || Number(values.price) <= 0) next.price = 'Enter a positive price.';
    if (!values.language.trim()) next.language = 'Language is required.';
    if (!values.categoryId) next.categoryId = 'Category is required.';
    if (!values.publisherId) next.publisherId = 'Publisher is required.';
    if (!values.authorIds.length) next.authorIds = 'Select at least one author.';
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setFormError(null);
    if (!validate()) return;

    try {
      await onSubmit({
        isbn: values.isbn.trim(),
        title: values.title.trim(),
        description: values.description.trim() || undefined,
        price: Number(values.price),
        language: values.language.trim(),
        publishedDate: values.publishedDate || undefined,
        categoryId: values.categoryId,
        publisherId: values.publisherId,
        authorIds: values.authorIds,
      });
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Unable to save book.');
    }
  }

  const fieldClass =
    'mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-700/20';

  return (
    <form className="space-y-5" onSubmit={(event) => void handleSubmit(event)} noValidate>
      {formError ? (
        <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
          {formError}
        </p>
      ) : null}

      <div className="grid gap-4 md:grid-cols-2">
        <label className="block text-sm font-medium text-slate-700">
          ISBN
          <input
            className={fieldClass}
            value={values.isbn}
            onChange={(event) => updateField('isbn', event.target.value)}
          />
          {errors.isbn ? <span className="mt-1 block text-xs text-red-600">{errors.isbn}</span> : null}
        </label>

        <label className="block text-sm font-medium text-slate-700">
          Title
          <input
            className={fieldClass}
            value={values.title}
            onChange={(event) => updateField('title', event.target.value)}
          />
          {errors.title ? <span className="mt-1 block text-xs text-red-600">{errors.title}</span> : null}
        </label>

        <label className="block text-sm font-medium text-slate-700 md:col-span-2">
          Description
          <textarea
            className={`${fieldClass} min-h-24`}
            value={values.description}
            onChange={(event) => updateField('description', event.target.value)}
          />
        </label>

        <label className="block text-sm font-medium text-slate-700">
          Price
          <input
            type="number"
            min="0.01"
            step="0.01"
            className={fieldClass}
            value={values.price}
            onChange={(event) => updateField('price', event.target.value)}
          />
          {errors.price ? <span className="mt-1 block text-xs text-red-600">{errors.price}</span> : null}
        </label>

        <label className="block text-sm font-medium text-slate-700">
          Language
          <input
            className={fieldClass}
            value={values.language}
            onChange={(event) => updateField('language', event.target.value)}
          />
          {errors.language ? (
            <span className="mt-1 block text-xs text-red-600">{errors.language}</span>
          ) : null}
        </label>

        <label className="block text-sm font-medium text-slate-700">
          Published date
          <input
            type="date"
            className={fieldClass}
            value={values.publishedDate}
            onChange={(event) => updateField('publishedDate', event.target.value)}
          />
        </label>

        <label className="block text-sm font-medium text-slate-700">
          Category
          <select
            className={fieldClass}
            value={values.categoryId}
            onChange={(event) => updateField('categoryId', event.target.value)}
          >
            <option value="">Select category</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
          {errors.categoryId ? (
            <span className="mt-1 block text-xs text-red-600">{errors.categoryId}</span>
          ) : null}
        </label>

        <label className="block text-sm font-medium text-slate-700">
          Publisher
          <select
            className={fieldClass}
            value={values.publisherId}
            onChange={(event) => updateField('publisherId', event.target.value)}
          >
            <option value="">Select publisher</option>
            {publishers.map((publisher) => (
              <option key={publisher.id} value={publisher.id}>
                {publisher.name}
              </option>
            ))}
          </select>
          {errors.publisherId ? (
            <span className="mt-1 block text-xs text-red-600">{errors.publisherId}</span>
          ) : null}
        </label>
      </div>

      <fieldset>
        <legend className="text-sm font-medium text-slate-700">Authors</legend>
        <div className="mt-2 max-h-48 space-y-2 overflow-y-auto rounded-lg border border-slate-200 p-3">
          {sortedAuthors.length === 0 ? (
            <p className="text-sm text-slate-500">No authors available.</p>
          ) : (
            sortedAuthors.map((author) => (
              <label key={author.id} className="flex items-center gap-2 text-sm text-slate-700">
                <input
                  type="checkbox"
                  checked={values.authorIds.includes(author.id)}
                  onChange={() => toggleAuthor(author.id)}
                />
                {authorName(author)}
              </label>
            ))
          )}
        </div>
        {errors.authorIds ? (
          <span className="mt-1 block text-xs text-red-600">{errors.authorIds}</span>
        ) : null}
      </fieldset>

      <div className="flex flex-wrap gap-3 pt-2">
        <button
          type="button"
          className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
          onClick={onCancel}
          disabled={loading}
        >
          Cancel
        </button>
        <button
          type="submit"
          className="rounded-lg bg-teal-800 px-4 py-2 text-sm font-medium text-white hover:bg-teal-900 disabled:opacity-60"
          disabled={loading}
        >
          {loading ? 'Saving…' : submitLabel}
        </button>
      </div>
    </form>
  );
}
