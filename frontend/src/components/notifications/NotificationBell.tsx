import { useState, useEffect } from 'react';
import { Bell, Check, Trash2, CheckCircle2, AlertTriangle, XCircle, Info } from 'lucide-react';
import { useNotificationContext } from '../../contexts/NotificationContext';

const ICONS = { success: CheckCircle2, error: XCircle, warning: AlertTriangle, info: Info } as const;
const ICON_COLORS = { success: 'text-emerald-500', error: 'text-red-500', warning: 'text-amber-500', info: 'text-blue-500' } as const;

const formatTime = (ts: number, now: number) => {
  const diff = now - ts, m = Math.floor(diff / 60000), h = Math.floor(diff / 3600000);
  if (m < 1) return 'Just now';
  if (m < 60) return `${m}m ago`;
  if (h < 24) return `${h}h ago`;
  return new Date(ts).toLocaleDateString();
};

export const NotificationBell = () => {
  const { notifications, unreadCount, markAsRead, markAllAsRead, removeNotification } = useNotificationContext();
  const [isOpen, setIsOpen] = useState(false);
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => { const id = setInterval(() => setNow(Date.now()), 60000); return () => clearInterval(id); }, []);
  useEffect(() => {
    if (!isOpen) return;
    const fn = (e: MouseEvent) => { if (!(e.target as HTMLElement).closest('[data-notif-bell]')) setIsOpen(false); };
    document.addEventListener('mousedown', fn);
    return () => document.removeEventListener('mousedown', fn);
  }, [isOpen]);
  useEffect(() => {
    if (!isOpen) return;
    const fn = (e: KeyboardEvent) => { if (e.key === 'Escape') setIsOpen(false); };
    document.addEventListener('keydown', fn);
    return () => document.removeEventListener('keydown', fn);
  }, [isOpen]);

  return (
    <div className="relative" data-notif-bell>
      <button type="button" aria-label={`Notifications${unreadCount > 0 ? `, ${unreadCount} unread` : ''}`}
        aria-haspopup="true" aria-expanded={isOpen} onClick={() => setIsOpen((v) => !v)}
        className="relative p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
        <Bell aria-hidden="true" className="h-5 w-5" />
        {unreadCount > 0 && (
          <span aria-hidden="true" className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div role="dialog" aria-label="Notifications"
          className="absolute right-0 mt-2 w-80 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-slate-200 dark:border-slate-700 z-50 overflow-hidden">
          <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100 dark:border-slate-700">
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-semibold text-slate-800 dark:text-slate-100">Notifications</h2>
              {unreadCount > 0 && <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-blue-100 dark:bg-blue-900/50 text-blue-700 dark:text-blue-300 text-[10px] font-bold">{unreadCount}</span>}
            </div>
            {unreadCount > 0 && (
              <button type="button" onClick={markAllAsRead} className="flex items-center gap-1 text-xs font-medium text-blue-600 dark:text-blue-400 hover:text-blue-700">
                <Check aria-hidden="true" className="h-3 w-3" /> Mark all read
              </button>
            )}
          </div>
          <div className="max-h-80 overflow-y-auto divide-y divide-slate-50 dark:divide-slate-700/50">
            {notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-10 gap-2 text-slate-400">
                <Bell aria-hidden="true" className="h-6 w-6 opacity-30" />
                <p className="text-sm">No notifications</p>
              </div>
            ) : notifications.map((n) => {
              const Icon = ICONS[n.type] ?? Info;
              return (
                <div key={n.id} onClick={() => markAsRead(n.id)} role="button" tabIndex={0}
                  onKeyDown={(e) => { if (e.key === 'Enter') markAsRead(n.id); }}
                  className={`flex items-start gap-3 px-4 py-3 cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors ${!n.read ? 'bg-blue-50/40 dark:bg-blue-900/10' : ''}`}>
                  <Icon aria-hidden="true" className={`h-4 w-4 mt-0.5 shrink-0 ${ICON_COLORS[n.type]}`} />
                  <div className="flex-1 min-w-0">
                    {n.title && <p className="text-xs font-semibold text-slate-800 dark:text-slate-100">{n.title}</p>}
                    <p className="text-xs text-slate-600 dark:text-slate-300 line-clamp-2">{n.message}</p>
                    <p className="text-[10px] text-slate-400 dark:text-slate-500 mt-1">{formatTime(n.timestamp, now)}</p>
                  </div>
                  {!n.read && <span aria-hidden="true" className="shrink-0 mt-1.5 w-2 h-2 rounded-full bg-blue-500" />}
                  <button type="button" aria-label="Remove" onClick={(e) => { e.stopPropagation(); removeNotification(n.id); }}
                    className="shrink-0 p-0.5 rounded text-slate-300 dark:text-slate-600 hover:text-red-400 transition-colors">
                    <Trash2 aria-hidden="true" className="h-3.5 w-3.5" />
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
