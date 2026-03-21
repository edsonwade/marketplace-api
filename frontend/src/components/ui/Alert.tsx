import { AlertTriangle, CheckCircle2, Info, XCircle, X } from 'lucide-react';
import type { HTMLAttributes } from 'react';

interface AlertProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'info' | 'success' | 'warning' | 'error';
  title?: string;
  message: string;
  onClose?: () => void;
}

const CONFIG = {
  info: {
    container: 'bg-blue-50  border-blue-200  text-blue-800',
    icon:      'text-blue-500',
    Icon:      Info,
  },
  success: {
    container: 'bg-emerald-50 border-emerald-200 text-emerald-800',
    icon:      'text-emerald-500',
    Icon:      CheckCircle2,
  },
  warning: {
    container: 'bg-amber-50 border-amber-200 text-amber-800',
    icon:      'text-amber-500',
    Icon:      AlertTriangle,
  },
  error: {
    container: 'bg-red-50 border-red-200 text-red-800',
    icon:      'text-red-500',
    Icon:      XCircle,
  },
} as const;

export const Alert = ({
  variant = 'info',
  title,
  message,
  onClose,
  className = '',
  ...rest
}: AlertProps) => {
  const { container, icon, Icon } = CONFIG[variant];

  return (
    <div
      role="alert"
      className={`${container} border rounded-lg p-3.5 flex items-start gap-3 text-sm ${className}`}
      {...rest}
    >
      <Icon aria-hidden="true" className={`shrink-0 h-4 w-4 mt-0.5 ${icon}`} />
      <div className="flex-1 min-w-0">
        {title && <p className="font-semibold mb-0.5">{title}</p>}
        <p className="leading-snug">{message}</p>
      </div>
      {onClose && (
        <button
          type="button"
          aria-label="Dismiss alert"
          onClick={onClose}
          className="shrink-0 rounded hover:opacity-70 focus-visible:outline-2 focus-visible:outline-current"
        >
          <X aria-hidden="true" className="h-4 w-4" />
        </button>
      )}
    </div>
  );
};
