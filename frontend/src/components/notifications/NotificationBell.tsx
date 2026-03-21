import { useState, useEffect } from 'react';
import { Bell, Check, Trash2, CheckCircle2, AlertTriangle, XCircle, Info } from 'lucide-react';
import { useNotificationContext } from '../../contexts/NotificationContext';

const ICONS = {
  success: CheckCircle2,
  error:   XCircle,
  warning: AlertTriangle,
  info:    Info,
} as const;

const ICON_COLORS = {
  success: 'text-emerald-500',
  error:   'text-red-500',
  warning: 'text-amber-500',
  info:    'text-blue-500',
} as const;

const formatTime = (ts: number, now: number) => {
  const diff    = now - ts;
  const minutes = Math.floor(diff / 60_000);
  const hours   = Math.floor(diff / 3_600_000);
  if (minutes < 1)  return 'Just now';
  if (minutes < 60) return `${minutes}m ago`;
  if (hours < 24)   return `${hours}h ago`;
  return new Date(ts).toLocaleDateString();
};

export const NotificationBell = () => {
  const { notifications, unreadCount, markAsRead, markAllAsRead, removeNotification } = useNotificationContext();
  const [isOpen, setIsOpen] = useState(false);
  const [now, setNow]       = useState(() => Date.now());

  /* Tick every minute for relative timestamps */
  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), 60_000);
    return () => clearInterval(id);
  }, []);

  /* Close on outside click */
  useEffect(() => {
    if (!isOpen) return;
    const handler = (e: MouseEvent) => {
      if (!(e.target as HTMLElement).closest('[data-notif-bell]')) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [isOpen]);

  /* Close on Escape */
  useEffect(() => {
    if (!isOpen) return;
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') setIsOpen(false); };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [isOpen]);

  return (
    <div className="relative" data-notif-bell>
      {/* Bell button */}
      <button
        type="button"
        aria-label={`Notifications${unreadCount > 0 ? `, ${unreadCount} unread` : ''}`}
        aria-haspopup="true"
        aria-expanded={isOpen}
        onClick={() => setIsOpen((v) => !v)}
        className="relative p-2 rounded-lg text-slate-500 hover:bg-slate-100 transition-colors"
      >
        <Bell aria-hidden="true" className="h-5 w-5" />
        {unreadCount > 0 && (
          <span
            aria-hidden="true"
            className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white leading-none"
          >
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown */}
      {isOpen && (
        <div
          role="dialog"
          aria-label="Notifications panel"
          className="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-xl border border-slate-200 z-50 overflow-hidden"
        >
          {/* Header */}
          <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100">
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-semibold text-slate-800">Notifications</h2>
              {unreadCount > 0 && (
                <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-blue-100 text-blue-700 text-[10px] font-bold">
                  {unreadCount}
                </span>
              )}
            </div>
            {unreadCount > 0 && (
              <button
                type="button"
                onClick={markAllAsRead}
                className="flex items-center gap-1 text-xs font-medium text-blue-600 hover:text-blue-700 transition-colors"
              >
                <Check aria-hidden="true" className="h-3 w-3" />
                Mark all read
              </button>
            )}
          </div>

          {/* List */}
          <div className="max-h-80 overflow-y-auto divide-y divide-slate-50">
            {notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-10 gap-2 text-slate-400">
                <Bell aria-hidden="true" className="h-6 w-6 opacity-30" />
                <p className="text-sm">No notifications</p>
              </div>
            ) : (
              notifications.map((n) => {
                const Icon = ICONS[n.type] ?? Info;
                const iconColor = ICON_COLORS[n.type] ?? 'text-blue-500';
                return (
                  <div
                    key={n.id}
                    className={`
                      flex items-start gap-3 px-4 py-3 cursor-pointer
                      hover:bg-slate-50 transition-colors
                      ${!n.read ? 'bg-blue-50/40' : ''}
                    `}
                    onClick={() => markAsRead(n.id)}
                    role="button"
                    tabIndex={0}
                    aria-label={`${n.read ? '' : 'Unread: '}${n.title ?? n.message}`}
                    onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') markAsRead(n.id); }}
                  >
                    {/* Unread dot / icon */}
                    <div className="shrink-0 mt-0.5">
                      <Icon aria-hidden="true" className={`h-4 w-4 ${iconColor}`} />
                    </div>

                    <div className="flex-1 min-w-0">
                      {n.title && (
                        <p className={`text-xs font-semibold text-slate-800 leading-snug ${!n.read ? '' : 'opacity-80'}`}>
                          {n.title}
                        </p>
                      )}
                      <p className="text-xs text-slate-600 line-clamp-2 mt-0.5">{n.message}</p>
                      <p className="text-[10px] text-slate-400 mt-1">{formatTime(n.timestamp, now)}</p>
                    </div>

                    {/* Unread indicator */}
                    {!n.read && (
                      <span aria-hidden="true" className="shrink-0 mt-1.5 w-2 h-2 rounded-full bg-blue-500" />
                    )}

                    {/* Remove */}
                    <button
                      type="button"
                      aria-label="Remove notification"
                      onClick={(e) => { e.stopPropagation(); removeNotification(n.id); }}
                      className="shrink-0 p-0.5 rounded text-slate-300 hover:text-red-400 transition-colors"
                    >
                      <Trash2 aria-hidden="true" className="h-3.5 w-3.5" />
                    </button>
                  </div>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
};
