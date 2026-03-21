import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryProvider } from './hooks';
import { ProtectedRoute, GuestRoute } from './hooks/ProtectedRoute';
import { Layout } from './components/layout';
import { FullPageSpinner } from './components/ui/Spinner';
import { NotificationProvider } from './contexts/NotificationContext';
import { ThemeProvider } from './contexts/ThemeContext';
import { ToastContainer } from './components/notifications/Toast';
import { useNotificationContext } from './contexts/NotificationContext';
import { AuthInitializer } from './components/AuthInitializer';

const LoginPage     = lazy(() => import('./pages/auth/LoginPage').then(m => ({ default: m.LoginPage })));
const RegisterPage  = lazy(() => import('./pages/auth/RegisterPage').then(m => ({ default: m.RegisterPage })));
const DashboardPage = lazy(() => import('./pages/dashboard/DashboardPage').then(m => ({ default: m.DashboardPage })));
const ProductsPage  = lazy(() => import('./pages/products/ProductsPage').then(m => ({ default: m.ProductsPage })));
const CartPage      = lazy(() => import('./pages/cart/CartPage').then(m => ({ default: m.CartPage })));
const OrdersPage    = lazy(() => import('./pages/orders/OrdersPage').then(m => ({ default: m.OrdersPage })));
const CustomersPage = lazy(() => import('./pages/customers/CustomersPage').then(m => ({ default: m.CustomersPage })));
const PaymentsPage  = lazy(() => import('./pages/payments/PaymentsPage').then(m => ({ default: m.PaymentsPage })));
const DiscountsPage = lazy(() => import('./pages/discounts/DiscountsPage').then(m => ({ default: m.DiscountsPage })));
const StocksPage    = lazy(() => import('./pages/stocks/StocksPage').then(m => ({ default: m.StocksPage })));
const SettingsPage  = lazy(() => import('./pages/settings/SettingsPage').then(m => ({ default: m.SettingsPage })));

const NotificationToaster = () => {
  const { notifications, removeNotification } = useNotificationContext();
  return <ToastContainer notifications={notifications} onDismiss={removeNotification} />;
};

function App() {
  return (
    <ThemeProvider>
      <QueryProvider>
        <NotificationProvider>
          {/* AuthInitializer silently restores the access token before rendering */}
          <AuthInitializer>
            <BrowserRouter>
              <Suspense fallback={<FullPageSpinner />}>
                <Routes>
                  <Route path="/login"    element={<GuestRoute><LoginPage /></GuestRoute>} />
                  <Route path="/register" element={<GuestRoute><RegisterPage /></GuestRoute>} />

                  <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
                    <Route index element={<Navigate to="/dashboard" replace />} />
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="/products"  element={<ProductsPage />} />
                    <Route path="/cart"      element={<CartPage />} />
                    <Route path="/orders"    element={<OrdersPage />} />
                    <Route path="/customers" element={<CustomersPage />} />
                    <Route path="/payments"  element={<PaymentsPage />} />
                    <Route path="/discounts" element={<DiscountsPage />} />
                    <Route path="/stocks"    element={<StocksPage />} />
                    <Route path="/settings"  element={<SettingsPage />} />
                  </Route>

                  <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
              </Suspense>
            </BrowserRouter>
            <NotificationToaster />
          </AuthInitializer>
        </NotificationProvider>
      </QueryProvider>
    </ThemeProvider>
  );
}

export default App;
