import { type HTMLAttributes, forwardRef } from 'react';

interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: 'default' | 'success' | 'warning' | 'danger' | 'info' | 'purple';
  size?: 'sm' | 'md';
  dot?: boolean;
}

export const Badge = forwardRef<HTMLSpanElement, BadgeProps>(
  ({ className = '', variant = 'default', size = 'sm', dot = false, children, ...props }, ref) => {
    const variants = {
      default: 'bg-slate-100 text-slate-700 ring-slate-200',
      success: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
      warning: 'bg-amber-50  text-amber-700  ring-amber-200',
      danger:  'bg-red-50    text-red-700    ring-red-200',
      info:    'bg-blue-50   text-blue-700   ring-blue-200',
      purple:  'bg-violet-50 text-violet-700 ring-violet-200',
    };

    const dotColors = {
      default: 'bg-slate-400',
      success: 'bg-emerald-500',
      warning: 'bg-amber-500',
      danger:  'bg-red-500',
      info:    'bg-blue-500',
      purple:  'bg-violet-500',
    };

    const sizes = {
      sm: 'px-2 py-0.5 text-xs',
      md: 'px-2.5 py-1 text-xs',
    };

    return (
      <span
        ref={ref}
        className={`
          inline-flex items-center gap-1.5 font-medium rounded-full ring-1
          ${variants[variant]} ${sizes[size]} ${className}
        `}
        {...props}
      >
        {dot && (
          <span
            aria-hidden="true"
            className={`shrink-0 w-1.5 h-1.5 rounded-full ${dotColors[variant]}`}
          />
        )}
        {children}
      </span>
    );
  }
);
Badge.displayName = 'Badge';
