import { AlertTriangle, CheckCircle2, Info, XCircle, X } from 'lucide-react';
import type { HTMLAttributes } from 'react';

interface AlertProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'info' | 'success' | 'warning' | 'error';
  title?: string;
  message: string;
  onClose?: () => void;
}

const CONFIG = {
  info:    { container: 'bg-blue-50   dark:bg-blue-950/60  border-blue-200  dark:border-blue-800  text-blue-800  dark:text-blue-200',  icon: 'text-blue-500',    Icon: Info         },
  success: { container: 'bg-green-50  dark:bg-green-950/60 border-green-200 dark:border-green-800 text-green-800 dark:text-green-200', icon: 'text-green-500',   Icon: CheckCircle2  },
  warning: { container: 'bg-amber-50  dark:bg-amber-950/60 border-amber-200 dark:border-amber-800 text-amber-800 dark:text-amber-200', icon: 'text-amber-500',   Icon: AlertTriangle },
  error:   { container: 'bg-red-50    dark:bg-red-950/60   border-red-200   dark:border-red-800   text-red-800   dark:text-red-200',   icon: 'text-red-500',     Icon: XCircle       },
} as const;

export const Alert = ({ variant = 'info', title, message, onClose, className = '', ...rest }: AlertProps) => {
  const { container, icon, Icon } = CONFIG[variant];
  return (
    <div role="alert" className={`${container} border rounded-lg p-3.5 flex items-start gap-3 text-sm ${className}`} {...rest}>
      <Icon aria-hidden="true" className={`shrink-0 h-4 w-4 mt-0.5 ${icon}`} />
      <div className="flex-1 min-w-0">
        {title && <p className="font-semibold mb-0.5">{title}</p>}
        <p className="leading-snug">{message}</p>
      </div>
      {onClose && (
        <button type="button" aria-label="Dismiss" onClick={onClose} className="shrink-0 rounded hover:opacity-70">
          <X aria-hidden="true" className="h-4 w-4" />
        </button>
      )}
    </div>
  );
};
