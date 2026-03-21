import { type HTMLAttributes, forwardRef } from 'react';

interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: 'default' | 'success' | 'warning' | 'danger' | 'info' | 'purple';
  size?: 'sm' | 'md';
  dot?: boolean;
}

export const Badge = forwardRef<HTMLSpanElement, BadgeProps>(
  ({ className = '', variant = 'default', size = 'sm', dot = false, children, ...props }, ref) => {
    const variants = {
      default: 'bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-200 ring-slate-200 dark:ring-slate-600',
      success: 'bg-emerald-100 dark:bg-emerald-900/50 text-emerald-800 dark:text-emerald-300 ring-emerald-200 dark:ring-emerald-700',
      warning: 'bg-amber-100  dark:bg-amber-900/50  text-amber-800  dark:text-amber-300  ring-amber-200  dark:ring-amber-700',
      danger:  'bg-red-100    dark:bg-red-900/50    text-red-800    dark:text-red-300    ring-red-200    dark:ring-red-700',
      info:    'bg-blue-100   dark:bg-blue-900/50   text-blue-800   dark:text-blue-300   ring-blue-200   dark:ring-blue-700',
      purple:  'bg-violet-100 dark:bg-violet-900/50 text-violet-800 dark:text-violet-300 ring-violet-200 dark:ring-violet-700',
    };
    const dotColors = {
      default: 'bg-slate-400',  success: 'bg-emerald-500',
      warning: 'bg-amber-500',  danger:  'bg-red-500',
      info:    'bg-blue-500',   purple:  'bg-violet-500',
    };
    const sizes = { sm: 'px-2 py-0.5 text-xs', md: 'px-2.5 py-1 text-xs' };

    return (
      <span ref={ref} className={`inline-flex items-center gap-1.5 font-medium rounded-full ring-1 ${variants[variant]} ${sizes[size]} ${className}`} {...props}>
        {dot && <span aria-hidden="true" className={`shrink-0 w-1.5 h-1.5 rounded-full ${dotColors[variant]}`} />}
        {children}
      </span>
    );
  }
);
Badge.displayName = 'Badge';
