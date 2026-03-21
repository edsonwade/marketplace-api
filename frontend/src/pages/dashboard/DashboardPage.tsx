import { Link } from 'react-router-dom';
import {
  Package, ShoppingCart, ClipboardList, Users,
  TrendingUp, ArrowRight, Activity, AlertCircle
} from 'lucide-react';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Spinner } from '../../components/ui/Spinner';
import { useProducts, useCustomers, useOrders } from '../../services';

/* ---- KPI Card ---- */
interface KpiCardProps {
  label: string;
  value: string | number;
  icon: React.ElementType;
  iconBg: string;
  iconColor: string;
  trend?: string;
  to: string;
}

const KpiCard = ({ label, value, icon: Icon, iconBg, iconColor, trend, to }: KpiCardProps) => (
  <Link to={to} className="group block focus-visible:outline-2 focus-visible:outline-blue-500 rounded-xl">
    <Card className="transition-shadow group-hover:shadow-md h-full">
      <CardBody className="flex items-start justify-between gap-4 p-5">
        <div>
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">{label}</p>
          <p className="text-3xl font-bold text-slate-900 leading-none">{value}</p>
          {trend && (
            <p className="flex items-center gap-1 text-xs text-emerald-600 font-medium mt-2">
              <TrendingUp className="h-3 w-3" aria-hidden="true" />
              {trend}
            </p>
          )}
        </div>
        <div className={`p-3 rounded-xl ${iconBg} shrink-0`}>
          <Icon className={`h-5 w-5 ${iconColor}`} aria-hidden="true" />
        </div>
      </CardBody>
    </Card>
  </Link>
);

/* ---- Quick action item ---- */
const QuickAction = ({
  to, icon: Icon, label, description, badge
}: { to: string; icon: React.ElementType; label: string; description: string; badge?: string }) => (
  <Link
    to={to}
    className="flex items-center gap-3 p-3 rounded-xl border border-slate-200 hover:border-blue-300 hover:bg-blue-50/50 transition-all group focus-visible:outline-2 focus-visible:outline-blue-500"
  >
    <div className="p-2 rounded-lg bg-slate-100 group-hover:bg-blue-100 transition-colors shrink-0">
      <Icon className="h-4 w-4 text-slate-600 group-hover:text-blue-600 transition-colors" aria-hidden="true" />
    </div>
    <div className="flex-1 min-w-0">
      <div className="flex items-center gap-2">
        <p className="text-sm font-semibold text-slate-800">{label}</p>
        {badge && <Badge variant="info">{badge}</Badge>}
      </div>
      <p className="text-xs text-slate-500">{description}</p>
    </div>
    <ArrowRight className="h-4 w-4 text-slate-300 group-hover:text-blue-400 transition-colors shrink-0" aria-hidden="true" />
  </Link>
);

/* ---- Page ---- */
export const DashboardPage = () => {
  const { data: products, isLoading: loadingP } = useProducts();
  const { data: customers, isLoading: loadingC } = useCustomers();
  const { data: orders, isLoading: loadingO } = useOrders();

  const isLoading = loadingP || loadingC || loadingO;

  const kpis: KpiCardProps[] = [
    {
      label: 'Products',
      value: isLoading ? '—' : (products?.length ?? 0),
      icon: Package,
      iconBg: 'bg-blue-100',
      iconColor: 'text-blue-600',
      to: '/products',
    },
    {
      label: 'Customers',
      value: isLoading ? '—' : (customers?.length ?? 0),
      icon: Users,
      iconBg: 'bg-emerald-100',
      iconColor: 'text-emerald-600',
      to: '/customers',
    },
    {
      label: 'Orders',
      value: isLoading ? '—' : (orders?.length ?? 0),
      icon: ClipboardList,
      iconBg: 'bg-violet-100',
      iconColor: 'text-violet-600',
      to: '/orders',
    },
    {
      label: 'Active Cart',
      value: '—',
      icon: ShoppingCart,
      iconBg: 'bg-amber-100',
      iconColor: 'text-amber-600',
      to: '/cart',
    },
  ];

  return (
    <div className="space-y-8 max-w-7xl">

      {/* Page title */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Dashboard</h1>
        <p className="text-sm text-slate-500 mt-1">Overview of your marketplace</p>
      </div>

      {/* KPI Grid */}
      <section aria-label="Key metrics">
        {isLoading ? (
          <div className="flex justify-center py-12"><Spinner size="lg" /></div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
            {kpis.map((kpi) => (
              <KpiCard key={kpi.label} {...kpi} />
            ))}
          </div>
        )}
      </section>

      {/* Main content: Recent orders + Quick actions */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* Recent Orders */}
        <section aria-label="Recent orders" className="lg:col-span-2">
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Activity className="h-4 w-4 text-slate-500" aria-hidden="true" />
                <h2 className="text-sm font-semibold text-slate-800">Recent Orders</h2>
              </div>
              <Link
                to="/orders"
                className="text-xs font-medium text-blue-600 hover:text-blue-700 flex items-center gap-1"
              >
                View all <ArrowRight className="h-3 w-3" aria-hidden="true" />
              </Link>
            </CardHeader>
            <div className="divide-y divide-slate-100">
              {isLoading ? (
                <div className="py-10 flex justify-center"><Spinner /></div>
              ) : orders && orders.length > 0 ? (
                orders.slice(0, 6).map((order) => (
                  <div
                    key={order.orderId}
                    className="flex items-center justify-between px-5 py-3.5 hover:bg-slate-50/60 transition-colors"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-8 h-8 rounded-full bg-violet-100 flex items-center justify-center shrink-0">
                        <ClipboardList className="h-3.5 w-3.5 text-violet-600" aria-hidden="true" />
                      </div>
                      <div className="min-w-0">
                        <p className="text-sm font-semibold text-slate-800">Order #{order.orderId}</p>
                        <p className="text-xs text-slate-500 truncate">
                          {order.customer?.name || 'Unknown customer'}
                        </p>
                      </div>
                    </div>
                    <div className="text-right shrink-0 ml-4">
                      <Badge variant="success" dot>Completed</Badge>
                      <p className="text-[11px] text-slate-400 mt-1">
                        {order.localDateTime
                          ? new Date(order.localDateTime).toLocaleDateString()
                          : '—'}
                      </p>
                    </div>
                  </div>
                ))
              ) : (
                <div className="flex flex-col items-center justify-center py-12 gap-2 text-slate-400">
                  <AlertCircle className="h-6 w-6" aria-hidden="true" />
                  <p className="text-sm">No orders yet</p>
                </div>
              )}
            </div>
          </Card>
        </section>

        {/* Quick Actions */}
        <section aria-label="Quick actions">
          <Card className="h-full">
            <CardHeader>
              <h2 className="text-sm font-semibold text-slate-800">Quick Actions</h2>
            </CardHeader>
            <CardBody className="space-y-2">
              <QuickAction
                to="/products"
                icon={Package}
                label="Products"
                description="Manage inventory"
              />
              <QuickAction
                to="/orders"
                icon={ClipboardList}
                label="Orders"
                description="Review recent orders"
              />
              <QuickAction
                to="/customers"
                icon={Users}
                label="Customers"
                description="View & manage customers"
              />
              <QuickAction
                to="/cart"
                icon={ShoppingCart}
                label="Cart"
                description="Current shopping cart"
              />
              <QuickAction
                to="/discounts"
                icon={Package}
                label="Discounts"
                description="Coupons & promotions"
              />
            </CardBody>
          </Card>
        </section>
      </div>
    </div>
  );
};
