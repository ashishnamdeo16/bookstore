import { useEffect, useId, useMemo, useRef, useState } from 'react';

export interface MultiSelectOption {
  value: string;
  label: string;
}

interface MultiSelectProps {
  id?: string;
  label: string;
  required?: boolean;
  options: MultiSelectOption[];
  value: string[];
  onChange: (next: string[]) => void;
  placeholder?: string;
  error?: string;
  disabled?: boolean;
}

export function MultiSelect({
  id,
  label,
  required = false,
  options,
  value,
  onChange,
  placeholder = 'Select options',
  error,
  disabled = false,
}: MultiSelectProps) {
  const generatedId = useId();
  const fieldId = id ?? generatedId;
  const rootRef = useRef<HTMLDivElement>(null);
  const [open, setOpen] = useState(false);

  const selectedLabels = useMemo(() => {
    const byValue = new Map(options.map((option) => [option.value, option.label]));
    return value.map((idValue) => byValue.get(idValue) ?? idValue);
  }, [options, value]);

  const summary =
    selectedLabels.length === 0
      ? placeholder
      : selectedLabels.length <= 2
        ? selectedLabels.join(', ')
        : `${selectedLabels.length} selected`;

  useEffect(() => {
    if (!open) return;

    function handlePointerDown(event: MouseEvent) {
      if (!rootRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') setOpen(false);
    }

    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [open]);

  function toggleValue(optionValue: string) {
    if (value.includes(optionValue)) {
      onChange(value.filter((item) => item !== optionValue));
    } else {
      onChange([...value, optionValue]);
    }
  }

  return (
    <div
      className={`field multi-select ${error ? 'field--error' : ''} ${disabled ? 'field--disabled' : ''}`}
      ref={rootRef}
    >
      <label className="field__label" htmlFor={fieldId}>
        {label}
        {required ? <span className="field__required"> *</span> : null}
      </label>

      <button
        id={fieldId}
        type="button"
        className={`field__input multi-select__trigger ${open ? 'is-open' : ''} ${
          value.length === 0 ? 'is-placeholder' : ''
        }`}
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-invalid={error ? true : undefined}
        disabled={disabled}
        onClick={() => setOpen((current) => !current)}
      >
        <span className="multi-select__summary">{summary}</span>
        <span className="multi-select__caret" aria-hidden="true">
          ▾
        </span>
      </button>

      {open ? (
        <div className="multi-select__menu" role="listbox" aria-multiselectable="true">
          {options.length === 0 ? (
            <p className="multi-select__empty">No options available.</p>
          ) : (
            options.map((option) => {
              const checked = value.includes(option.value);
              return (
                <label key={option.value} className="multi-select__option">
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={() => toggleValue(option.value)}
                  />
                  <span>{option.label}</span>
                </label>
              );
            })
          )}
        </div>
      ) : null}

      {value.length > 0 ? (
        <div className="multi-select__chips">
          {selectedLabels.map((labelText, index) => (
            <button
              key={`${value[index]}-${labelText}`}
              type="button"
              className="multi-select__chip"
              onClick={() => onChange(value.filter((item) => item !== value[index]))}
              disabled={disabled}
            >
              {labelText}
              <span aria-hidden="true">×</span>
            </button>
          ))}
        </div>
      ) : null}

      {error ? <p className="field__error">{error}</p> : null}
    </div>
  );
}
