import { forwardRef, type TextareaHTMLAttributes } from 'react';

interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string;
  error?: string;
}

export const TextArea = forwardRef<HTMLTextAreaElement, TextAreaProps>(function TextArea(
  { label, error, id, ...props },
  ref,
) {
  const inputId = id ?? props.name;
  const describedBy = error ? `${inputId}-error` : undefined;

  return (
    <div className={`field ${error ? 'field--error' : ''}`.trim()}>
      <label className="field__label" htmlFor={inputId}>
        {label}
      </label>
      <textarea
        ref={ref}
        id={inputId}
        className="field__input field__textarea"
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        rows={3}
        {...props}
      />
      {error ? (
        <p id={`${inputId}-error`} className="field__error" role="alert">
          {error}
        </p>
      ) : null}
    </div>
  );
});
