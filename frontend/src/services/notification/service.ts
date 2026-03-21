import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client/apiClient';
import type { NotificationPayload } from '../../api/types/notification';

const NOTIFICATION_ENDPOINTS = {
  LIST: (customerId: number) => `/api/notifications/${customerId}`,
  MARK_READ: (id: string) => `/api/notifications/${id}/read`,
  MARK_ALL_READ: (customerId: number) => `/api/notifications/${customerId}/read-all`,
  DELETE: (id: string) => `/api/notifications/${id}`,
};

export interface NotificationItem {
  id: string;
  recipient: string;
  subject: string;
  message: string;
  type: string;
  status: string;
  createdAt: string;
  sentAt?: string;
}

export const notificationService = {
  getByCustomer: async (customerId: number): Promise<NotificationItem[]> => {
    const response = await apiClient.get<NotificationItem[]>(NOTIFICATION_ENDPOINTS.LIST(customerId));
    return response.data;
  },

  markAsRead: async (id: string): Promise<void> => {
    await apiClient.patch(NOTIFICATION_ENDPOINTS.MARK_READ(id));
  },

  markAllAsRead: async (customerId: number): Promise<void> => {
    await apiClient.patch(NOTIFICATION_ENDPOINTS.MARK_ALL_READ(customerId));
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(NOTIFICATION_ENDPOINTS.DELETE(id));
  },
};

export const useNotifications = (customerId: number) => {
  return useQuery({
    queryKey: ['notifications', customerId],
    queryFn: () => notificationService.getByCustomer(customerId),
    enabled: !!customerId,
    refetchInterval: 30000,
    staleTime: 15000,
  });
};

export const useMarkNotificationRead = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => notificationService.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
};

export const useMarkAllNotificationsRead = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (customerId: number) => notificationService.markAllAsRead(customerId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
};

export const useDeleteNotification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => notificationService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
};

export const mapBackendToNotification = (item: NotificationItem): NotificationPayload => {
  const typeMap: Record<string, 'info' | 'success' | 'warning' | 'error'> = {
    ORDER_CREATED: 'info',
    PAYMENT_PROCESSED: 'success',
    ORDER_SHIPPED: 'info',
    PAYMENT_FAILED: 'error',
    LOW_STOCK: 'warning',
  };

  return {
    type: typeMap[item.type] || 'info',
    title: item.subject || item.type,
    message: item.message,
    link: item.type === 'ORDER_CREATED' ? '/orders' : item.type === 'PAYMENT_PROCESSED' ? '/payments' : undefined,
  };
};
