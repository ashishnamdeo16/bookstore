import { forwardRef, useState, type InputHTMLAttributes } from 'react';

interface PasswordFieldProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: string;
  error?: string;
  hint?: string;
}

export const PasswordField = forwardRef<HTMLInputElement, PasswordFieldProps>(
  function PasswordField({ label, error, hint, id, ...props }, ref) {
    const [visible, setVisible] = useState(false);
    const inputId = id ?? props.name;
    const describedBy = error ? `${inputId}-error` : hint ? `${inputId}-hint` : undefined;

    return (
      <div className={`field ${error ? 'field--error' : ''}`.trim()}>
        <label className="field__label" htmlFor={inputId}>
          {label}
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
            {...props}
          />
          <button
            type="button"
            className="field__toggle"
            onClick={() => setVisible((value) => !value)}
            aria-label={visible ? 'Hide password' : 'Show password'}
            aria-pressed={visible}
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
