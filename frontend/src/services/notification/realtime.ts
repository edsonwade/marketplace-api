import { useEffect, useRef } from 'react';
import type { NotificationPayload } from '../../api/types/notification';

type NotificationHandler = (payload: NotificationPayload) => void;

class RealtimeNotificationService {
  private eventSource: EventSource | null = null;
  private handlers: Set<NotificationHandler> = new Set();
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private isConnected = false;

  connect(customerId?: number) {
    if (this.isConnected) return;
    if (this.eventSource) {
      this.eventSource.close();
    }

    const baseUrl = import.meta.env.VITE_API_URL || '';
    const url = customerId
      ? `${baseUrl}/api/notifications/stream?customerId=${customerId}`
      : `${baseUrl}/api/notifications/stream`;

    try {
      this.eventSource = new EventSource(url);

      this.eventSource.onopen = () => {
        this.isConnected = true;
      };

      this.eventSource.onmessage = (event) => {
        try {
          const payload: NotificationPayload = JSON.parse(event.data);
          this.handlers.forEach((handler) => handler(payload));
        } catch {
          // Ignore malformed messages
        }
      };

      this.eventSource.onerror = () => {
        this.isConnected = false;
        this.eventSource?.close();
        this.eventSource = null;
        this.scheduleReconnect(customerId);
      };
    } catch {
      this.scheduleReconnect(customerId);
    }
  }

  private scheduleReconnect(customerId?: number) {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.reconnectTimer = setTimeout(() => this.connect(customerId), 5000);
  }

  disconnect() {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.eventSource?.close();
    this.eventSource = null;
    this.isConnected = false;
  }

  subscribe(handler: NotificationHandler) {
    this.handlers.add(handler);
    return () => this.handlers.delete(handler);
  }
}

export const realtimeService = new RealtimeNotificationService();

export const useRealtimeNotifications = (onNotification: NotificationHandler, customerId?: number) => {
  const handlerRef = useRef(onNotification);

  useEffect(() => {
    handlerRef.current = onNotification;
  }, [onNotification]);

  useEffect(() => {
    const wrappedHandler: NotificationHandler = (payload) => handlerRef.current(payload);
    const unsubscribe = realtimeService.subscribe(wrappedHandler);
    realtimeService.connect(customerId);
    return () => {
      unsubscribe();
      realtimeService.disconnect();
    };
  }, [customerId]);
};
