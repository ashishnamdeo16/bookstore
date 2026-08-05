import { forwardRef, type TextareaHTMLAttributes } from 'react';

type FieldSize = 'sm' | 'md' | 'lg';

interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string;
  error?: string;
  hint?: string;
  fieldSize?: FieldSize;
}

export const TextArea = forwardRef<HTMLTextAreaElement, TextAreaProps>(function TextArea(
  { label, error, hint, id, fieldSize = 'md', disabled, required, ...props },
  ref,
) {
  const inputId = id ?? props.name;
  const describedBy = error ? `${inputId}-error` : hint ? `${inputId}-hint` : undefined;
  const fieldClasses = [
    'field',
    error ? 'field--error' : '',
    disabled ? 'field--disabled' : '',
    fieldSize !== 'md' ? `field--${fieldSize}` : '',
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={fieldClasses}>
      <label className="field__label" htmlFor={inputId}>
        {label}
        {required ? (
          <span className="field__required" aria-hidden="true">
            {' '}
            *
          </span>
        ) : null}
      </label>
      <textarea
        ref={ref}
        id={inputId}
        className="field__input field__textarea"
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        disabled={disabled}
        required={required}
        rows={3}
        {...props}
      />
      {hint && !error ? (
        <p id={`${inputId}-hint`} className="field__hint">
          {hint}
        </p>
      ) : null}
      {error ? (
        <p id={`${inputId}-error`} className="field__error" role="alert">
          {error}
        </p>
      ) : null}
    </div>
  );
});
