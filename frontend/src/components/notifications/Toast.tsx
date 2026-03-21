import { useEffect, useState } from 'react';
import { CheckCircle2, XCircle, AlertTriangle, Info, X } from 'lucide-react';
import type { AppNotification } from '../../api/types/notification';

const ICONS = {
  success: CheckCircle2,
  error:   XCircle,
  warning: AlertTriangle,
  info:    Info,
} as const;

const STYLES = {
  success: { container: 'bg-white border-l-4 border-l-emerald-500', icon: 'text-emerald-500', close: 'hover:bg-emerald-50 text-slate-400' },
  error:   { container: 'bg-white border-l-4 border-l-red-500',     icon: 'text-red-500',     close: 'hover:bg-red-50   text-slate-400' },
  warning: { container: 'bg-white border-l-4 border-l-amber-500',   icon: 'text-amber-500',   close: 'hover:bg-amber-50 text-slate-400' },
  info:    { container: 'bg-white border-l-4 border-l-blue-500',    icon: 'text-blue-500',    close: 'hover:bg-blue-50  text-slate-400' },
} as const;

interface ToastProps {
  notification: AppNotification;
  onDismiss: (id: string) => void;
  duration?: number;
}

export const Toast = ({ notification, onDismiss, duration = 5000 }: ToastProps) => {
  const [visible, setVisible] = useState(false);

  /* Animate in */
  useEffect(() => {
    const t = requestAnimationFrame(() => setVisible(true));
    return () => cancelAnimationFrame(t);
  }, []);

  /* Auto-dismiss */
  useEffect(() => {
    if (duration <= 0) return;
    const timer = setTimeout(() => {
      setVisible(false);
      setTimeout(() => onDismiss(notification.id), 300);
    }, duration);
    return () => clearTimeout(timer);
  }, [notification.id, duration, onDismiss]);

  const dismiss = () => {
    setVisible(false);
    setTimeout(() => onDismiss(notification.id), 300);
  };

  const Icon   = ICONS[notification.type];
  const styles = STYLES[notification.type];

  return (
    <div
      role="alert"
      aria-live="assertive"
      aria-atomic="true"
      className={`
        flex items-start gap-3 px-4 py-3.5 rounded-xl shadow-xl border border-slate-200 max-w-sm
        transition-all duration-300 ease-in-out
        ${styles.container}
        ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2'}
      `}
    >
      <Icon aria-hidden="true" className={`h-4 w-4 shrink-0 mt-0.5 ${styles.icon}`} />
      <div className="flex-1 min-w-0">
        {notification.title && (
          <p className="text-sm font-semibold text-slate-800 leading-snug">{notification.title}</p>
        )}
        <p className={`text-sm text-slate-600 ${notification.title ? 'mt-0.5' : ''}`}>
          {notification.message}
        </p>
      </div>
      <button
        type="button"
        aria-label="Dismiss notification"
        onClick={dismiss}
        className={`shrink-0 p-0.5 rounded transition-colors ${styles.close}`}
      >
        <X aria-hidden="true" className="h-3.5 w-3.5" />
      </button>
    </div>
  );
};

export const ToastContainer = ({
  notifications,
  onDismiss,
}: {
  notifications: AppNotification[];
  onDismiss: (id: string) => void;
}) => (
  <div
    aria-label="Notifications"
    className="fixed bottom-5 right-5 z-[100] flex flex-col gap-2 pointer-events-none"
  >
    {notifications.map((n) => (
      <div key={n.id} className="pointer-events-auto">
        <Toast notification={n} onDismiss={onDismiss} />
      </div>
    ))}
  </div>
);
