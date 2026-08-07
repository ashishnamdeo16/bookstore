import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Alert } from '../ui/Alert';
import { Button } from '../ui/Button';
import { MultiSelect } from '../ui/MultiSelect';
import { authorName } from '../../hooks/useBooks';
import type { Author, BookCreateRequest, Category, Publisher } from '../../types/book';

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
  existingCoverUrl?: string | null;
  authors: Author[];
  categories: Category[];
  publishers: Publisher[];
  submitLabel: string;
  loading?: boolean;
  onSubmit: (payload: BookCreateRequest, coverFile: File | null) => Promise<void>;
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

const ALLOWED_COVER_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);
const MAX_COVER_BYTES = 5 * 1024 * 1024;

export function BookForm({
  initialValues,
  existingCoverUrl = null,
  authors,
  categories,
  publishers,
  submitLabel,
  loading = false,
  onSubmit,
  onCancel,
}: BookFormProps) {
  const [values, setValues] = useState<BookFormValues>({ ...EMPTY, ...initialValues });
  const [errors, setErrors] = useState<Partial<Record<keyof BookFormValues | 'cover', string>>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [coverFile, setCoverFile] = useState<File | null>(null);
  const [coverPreviewUrl, setCoverPreviewUrl] = useState<string | null>(null);

  const authorOptions = useMemo(
    () =>
      [...authors]
        .sort((a, b) => authorName(a).localeCompare(authorName(b)))
        .map((author) => ({ value: author.id, label: authorName(author) })),
    [authors],
  );

  useEffect(() => {
    if (!coverFile) {
      setCoverPreviewUrl(null);
      return;
    }
    const objectUrl = URL.createObjectURL(coverFile);
    setCoverPreviewUrl(objectUrl);
    return () => URL.revokeObjectURL(objectUrl);
  }, [coverFile]);

  function updateField<K extends keyof BookFormValues>(key: K, value: BookFormValues[K]) {
    setValues((current) => ({ ...current, [key]: value }));
  }

  function handleCoverChange(file: File | null) {
    setErrors((current) => {
      const next = { ...current };
      delete next.cover;
      return next;
    });

    if (!file) {
      setCoverFile(null);
      return;
    }

    if (!ALLOWED_COVER_TYPES.has(file.type)) {
      setCoverFile(null);
      setErrors((current) => ({
        ...current,
        cover: 'Only JPEG, PNG, and WebP images are allowed.',
      }));
      return;
    }

    if (file.size > MAX_COVER_BYTES) {
      setCoverFile(null);
      setErrors((current) => ({
        ...current,
        cover: 'Cover image must be 5MB or smaller.',
      }));
      return;
    }

    setCoverFile(file);
  }

  function validate(): boolean {
    const next: Partial<Record<keyof BookFormValues | 'cover', string>> = {};
    if (!values.isbn.trim()) next.isbn = 'ISBN is required.';
    if (!values.title.trim()) next.title = 'Title is required.';
    if (!values.price.trim() || Number(values.price) <= 0) next.price = 'Enter a positive price.';
    if (!values.language.trim()) next.language = 'Language is required.';
    if (!values.categoryId) next.categoryId = 'Category is required.';
    if (!values.publisherId) next.publisherId = 'Publisher is required.';
    if (!values.authorIds.length) next.authorIds = 'Select at least one author.';
    if (errors.cover) next.cover = errors.cover;
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setFormError(null);
    if (!validate()) return;

    try {
      await onSubmit(
        {
          isbn: values.isbn.trim(),
          title: values.title.trim(),
          description: values.description.trim() || undefined,
          price: Number(values.price),
          language: values.language.trim(),
          publishedDate: values.publishedDate || undefined,
          categoryId: values.categoryId,
          publisherId: values.publisherId,
          authorIds: values.authorIds,
        },
        coverFile,
      );
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Unable to save book.');
    }
  }

  const previewSrc = coverPreviewUrl ?? existingCoverUrl ?? null;

  return (
    <form className="admin-form" onSubmit={(event) => void handleSubmit(event)} noValidate>
      {formError ? <Alert variant="error">{formError}</Alert> : null}

      <div className="admin-form__grid">
        <div className={`field ${errors.isbn ? 'field--error' : ''}`}>
          <label className="field__label" htmlFor="book-isbn">
            ISBN <span className="field__required">*</span>
          </label>
          <input
            id="book-isbn"
            className="field__input"
            value={values.isbn}
            onChange={(event) => updateField('isbn', event.target.value)}
          />
          {errors.isbn ? <p className="field__error">{errors.isbn}</p> : null}
        </div>

        <div className={`field ${errors.title ? 'field--error' : ''}`}>
          <label className="field__label" htmlFor="book-title">
            Title <span className="field__required">*</span>
          </label>
          <input
            id="book-title"
            className="field__input"
            value={values.title}
            onChange={(event) => updateField('title', event.target.value)}
          />
          {errors.title ? <p className="field__error">{errors.title}</p> : null}
        </div>

        <div className="field admin-form__full">
          <label className="field__label" htmlFor="book-description">
            Description
          </label>
          <textarea
            id="book-description"
            className="field__input field__textarea"
            value={values.description}
            onChange={(event) => updateField('description', event.target.value)}
          />
        </div>

        <div className={`field ${errors.price ? 'field--error' : ''}`}>
          <label className="field__label" htmlFor="book-price">
            Price <span className="field__required">*</span>
          </label>
          <input
            id="book-price"
            type="number"
            min="0.01"
            step="0.01"
            className="field__input"
            value={values.price}
            onChange={(event) => updateField('price', event.target.value)}
          />
          {errors.price ? <p className="field__error">{errors.price}</p> : null}
        </div>

        <div className={`field ${errors.language ? 'field--error' : ''}`}>
          <label className="field__label" htmlFor="book-language">
            Language <span className="field__required">*</span>
          </label>
          <input
            id="book-language"
            className="field__input"
            value={values.language}
            onChange={(event) => updateField('language', event.target.value)}
          />
          {errors.language ? <p className="field__error">{errors.language}</p> : null}
        </div>

        <div className="field">
          <label className="field__label" htmlFor="book-published">
            Published date
          </label>
          <input
            id="book-published"
            type="date"
            className="field__input"
            value={values.publishedDate}
            onChange={(event) => updateField('publishedDate', event.target.value)}
          />
        </div>

        <div className={`field ${errors.categoryId ? 'field--error' : ''}`}>
          <label className="field__label" htmlFor="book-category">
            Category <span className="field__required">*</span>
          </label>
          <select
            id="book-category"
            className="field__input"
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
          {errors.categoryId ? <p className="field__error">{errors.categoryId}</p> : null}
        </div>

        <div className={`field ${errors.publisherId ? 'field--error' : ''}`}>
          <label className="field__label" htmlFor="book-publisher">
            Publisher <span className="field__required">*</span>
          </label>
          <select
            id="book-publisher"
            className="field__input"
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
          {errors.publisherId ? <p className="field__error">{errors.publisherId}</p> : null}
        </div>

        <div className="admin-form__full">
          <MultiSelect
            id="book-authors"
            label="Authors"
            required
            options={authorOptions}
            value={values.authorIds}
            onChange={(authorIds) => updateField('authorIds', authorIds)}
            placeholder="Select authors"
            error={errors.authorIds}
            disabled={loading}
          />
        </div>

        <div className={`field admin-form__full ${errors.cover ? 'field--error' : ''}`}>
          <label className="field__label" htmlFor="book-cover">
            Cover image
          </label>
          <div className="book-cover-upload">
            <div className="book-cover-upload__preview" aria-hidden={!previewSrc}>
              {previewSrc ? (
                <img src={previewSrc} alt="" className="book-cover-upload__image" />
              ) : (
                <span className="book-cover-upload__placeholder">No cover</span>
              )}
            </div>
            <div className="book-cover-upload__controls">
              <input
                id="book-cover"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                className="field__input"
                disabled={loading}
                onChange={(event) => handleCoverChange(event.target.files?.[0] ?? null)}
              />
              <p className="field__hint">JPEG, PNG, or WebP. Max 5MB. Optional.</p>
              {coverFile ? (
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  disabled={loading}
                  onClick={() => handleCoverChange(null)}
                >
                  Clear selected file
                </Button>
              ) : null}
            </div>
          </div>
          {errors.cover ? <p className="field__error">{errors.cover}</p> : null}
        </div>
      </div>

      <div className="admin-form__actions">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={loading}>
          Cancel
        </Button>
        <Button type="submit" loading={loading}>
          {submitLabel}
        </Button>
      </div>
    </form>
  );
}
