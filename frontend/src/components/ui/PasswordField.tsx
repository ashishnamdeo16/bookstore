import { forwardRef, useState, type InputHTMLAttributes } from 'react';

type FieldSize = 'sm' | 'md' | 'lg';

interface PasswordFieldProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: string;
  error?: string;
  hint?: string;
  fieldSize?: FieldSize;
}

export const PasswordField = forwardRef<HTMLInputElement, PasswordFieldProps>(
  function PasswordField(
    { label, error, hint, id, fieldSize = 'md', disabled, required, ...props },
    ref,
  ) {
    const [visible, setVisible] = useState(false);
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
        <div className="field__password">
          <input
            ref={ref}
            id={inputId}
            type={visible ? 'text' : 'password'}
            className="field__input"
            aria-invalid={error ? true : undefined}
            aria-describedby={describedBy}
            autoComplete={props.autoComplete ?? 'current-password'}
            disabled={disabled}
            required={required}
            {...props}
          />
          <button
            type="button"
            className="field__toggle"
            onClick={() => setVisible((value) => !value)}
            aria-label={visible ? 'Hide password' : 'Show password'}
            aria-pressed={visible}
            disabled={disabled}
          >
            {visible ? 'Hide' : 'Show'}
          </button>
        </div>
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
  },
);
