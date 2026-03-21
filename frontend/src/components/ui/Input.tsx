import { type InputHTMLAttributes, forwardRef } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  hint?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className = '', label, error, hint, id, ...props }, ref) => {
    const inputId  = id || label?.toLowerCase().replace(/\s+/g, '-');
    const hintId   = hint  ? `${inputId}-hint`  : undefined;
    const errorId  = error ? `${inputId}-error` : undefined;
    const describedBy = [hintId, errorId].filter(Boolean).join(' ') || undefined;

    return (
      <div className="w-full">
        {label && (
          <label htmlFor={inputId} className="block text-xs font-semibold text-slate-600 dark:text-slate-300 mb-1.5 uppercase tracking-wide">
            {label}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          aria-describedby={describedBy}
          aria-invalid={error ? 'true' : undefined}
          className={`
            w-full px-3 py-2 text-sm rounded-lg border transition-colors duration-150
            bg-white dark:bg-slate-900
            text-slate-900 dark:text-slate-100
            placeholder:text-slate-400 dark:placeholder:text-slate-500
            focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500
            disabled:bg-slate-100 dark:disabled:bg-slate-800 disabled:text-slate-400 disabled:cursor-not-allowed
            ${error
              ? 'border-red-400 dark:border-red-500 focus:ring-red-400'
              : 'border-slate-300 dark:border-slate-600 hover:border-slate-400 dark:hover:border-slate-500'}
            ${className}
          `}
          {...props}
        />
        {hint && !error && (
          <p id={hintId} className="mt-1.5 text-xs text-slate-500 dark:text-slate-400">{hint}</p>
        )}
        {error && (
          <p id={errorId} role="alert" className="mt-1.5 text-xs text-red-600 dark:text-red-400 font-medium">{error}</p>
        )}
      </div>
    );
  }
);
Input.displayName = 'Input';
