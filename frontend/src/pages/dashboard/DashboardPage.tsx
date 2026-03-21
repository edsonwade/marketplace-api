import { Link } from 'react-router-dom';
import {
  Package, ClipboardList, Users,
  TrendingUp, ArrowRight, Activity, AlertCircle, DollarSign
} from 'lucide-react';
import {
  AreaChart, Area, BarChart, Bar, XAxis, YAxis,
  CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Spinner } from '../../components/ui/Spinner';
import { useProducts, useCustomers, useOrders, usePayments } from '../../services';

// ── Helpers ──────────────────────────────────────────────────────────────────
const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function buildOrdersPerMonth(orders: { localDateTime?: string }[] = []) {
  const counts: Record<number, number> = {};
  orders.forEach((o) => {
    if (!o.localDateTime) return;
    const m = new Date(o.localDateTime).getMonth();
    counts[m] = (counts[m] ?? 0) + 1;
  });
  return MONTHS.map((name, i) => ({ name, orders: counts[i] ?? 0 }));
}

function buildRevenuePerMonth(payments: { createdAt?: string; amount?: number | string }[] = []) {
  const totals: Record<number, number> = {};
  payments.forEach((p) => {
    if (!p.createdAt) return;
    const m = new Date(p.createdAt).getMonth();
    totals[m] = (totals[m] ?? 0) + Number(p.amount ?? 0);
  });
  return MONTHS.map((name, i) => ({ name, revenue: Math.round((totals[i] ?? 0) * 100) / 100 }));
}


// ── KPI Card ─────────────────────────────────────────────────────────────────
interface KpiCardProps {
  label: string; value: string | number; icon: React.ElementType;
  iconBg: string; iconColor: string; trend?: string; to: string;
}
const KpiCard = ({ label, value, icon: Icon, iconBg, iconColor, trend, to }: KpiCardProps) => (
  <Link to={to} className="group block rounded-xl focus-visible:outline-2 focus-visible:outline-blue-500">
    <Card className="transition-shadow group-hover:shadow-md h-full">
      <CardBody className="flex items-start justify-between gap-4 p-5">
        <div>
          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">{label}</p>
          <p className="text-3xl font-bold text-slate-900 dark:text-white leading-none">{value}</p>
          {trend && (
            <p className="flex items-center gap-1 text-xs text-emerald-600 dark:text-emerald-400 font-medium mt-2">
              <TrendingUp className="h-3 w-3" aria-hidden="true" />{trend}
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

// ── Custom Tooltip ────────────────────────────────────────────────────────────
const ChartTooltip = ({ active, payload, label, prefix = '' }: any) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg px-3 py-2 shadow-lg text-xs">
      <p className="font-semibold text-slate-700 dark:text-slate-200 mb-1">{label}</p>
      {payload.map((p: any) => (
        <p key={p.name} style={{ color: p.color }} className="font-medium">
          {p.name}: {prefix}{p.value}
        </p>
      ))}
    </div>
  );
};

// ── Page ─────────────────────────────────────────────────────────────────────
export const DashboardPage = () => {
  const { data: products,  isLoading: lP }   = useProducts();
  const { data: customers, isLoading: lC }   = useCustomers();
  const { data: orders,    isLoading: lO }   = useOrders();
  const { data: payments,  isLoading: lPay } = usePayments();
  const isLoading = lP || lC || lO || lPay;

  // Stock alerts — products that need attention
  const lowStockAlerts = (products ?? []).filter((p) => p.quantity > 0 && p.quantity < 10);
  const outOfStock     = (products ?? []).filter((p) => p.quantity === 0);
  const criticalItems  = [...outOfStock, ...lowStockAlerts].slice(0, 5);

  const totalRevenue = (payments ?? []).reduce((s, p) => s + Number(p.amount ?? 0), 0);

  const kpis: KpiCardProps[] = [
    { label: 'Products',  value: products?.length  ?? 0, icon: Package,       iconBg: 'bg-blue-100 dark:bg-blue-900/40',    iconColor: 'text-blue-600 dark:text-blue-400',    to: '/products'  },
    { label: 'Customers', value: customers?.length ?? 0, icon: Users,         iconBg: 'bg-emerald-100 dark:bg-emerald-900/40', iconColor: 'text-emerald-600 dark:text-emerald-400', to: '/customers' },
    { label: 'Orders',    value: orders?.length    ?? 0, icon: ClipboardList, iconBg: 'bg-violet-100 dark:bg-violet-900/40',  iconColor: 'text-violet-600 dark:text-violet-400',  to: '/orders'    },
    { label: 'Revenue',   value: `$${totalRevenue.toFixed(2)}`, icon: DollarSign, iconBg: 'bg-amber-100 dark:bg-amber-900/40', iconColor: 'text-amber-600 dark:text-amber-400', to: '/payments'  },
  ];

  const ordersData  = buildOrdersPerMonth(orders ?? []);
  const revenueData = buildRevenuePerMonth(payments ?? []);

  const axisStyle  = { fontSize: 11, fill: '#94a3b8' };
  const gridStyle  = { stroke: '#e2e8f0', strokeOpacity: 0.5 };

  return (
    <div className="space-y-6 max-w-7xl">
      {/* Title */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Dashboard</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Overview of your marketplace</p>
      </div>

      {/* KPI row */}
      {isLoading
        ? <div className="flex justify-center py-12"><Spinner size="lg" /></div>
        : <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
            {kpis.map((k) => <KpiCard key={k.label} {...k} />)}
          </div>
      }

      {/* Charts row 1 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Orders per month */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Activity className="h-4 w-4 text-slate-400" aria-hidden="true" />
              <h2 className="text-sm font-semibold text-slate-800 dark:text-slate-100">Orders per Month</h2>
            </div>
          </CardHeader>
          <CardBody className="pt-2 pb-4">
            <ResponsiveContainer width="100%" height={200}>
              <AreaChart data={ordersData} margin={{ top: 4, right: 8, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="ordersGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%"  stopColor="#3b82f6" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" {...gridStyle} />
                <XAxis dataKey="name" tick={axisStyle} />
                <YAxis tick={axisStyle} allowDecimals={false} />
                <Tooltip content={<ChartTooltip />} />
                <Area type="monotone" dataKey="orders" name="Orders" stroke="#3b82f6" strokeWidth={2} fill="url(#ordersGrad)" dot={false} activeDot={{ r: 4 }} />
              </AreaChart>
            </ResponsiveContainer>
          </CardBody>
        </Card>

        {/* Revenue per month */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <DollarSign className="h-4 w-4 text-slate-400" aria-hidden="true" />
              <h2 className="text-sm font-semibold text-slate-800 dark:text-slate-100">Revenue per Month</h2>
            </div>
          </CardHeader>
          <CardBody className="pt-2 pb-4">
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={revenueData} margin={{ top: 4, right: 8, left: -16, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" {...gridStyle} />
                <XAxis dataKey="name" tick={axisStyle} />
                <YAxis tick={axisStyle} />
                <Tooltip content={<ChartTooltip prefix="$" />} />
                <Bar dataKey="revenue" name="Revenue" fill="#8b5cf6" radius={[4, 4, 0, 0]} maxBarSize={32} />
              </BarChart>
            </ResponsiveContainer>
          </CardBody>
        </Card>
      </div>

      {/* Charts row 2 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      {/* Stock alerts panel */}
      <Card>
      <CardHeader>
      <div className="flex items-center gap-2">
          <Package className="h-4 w-4 text-slate-400" aria-hidden="true" />
          <h2 className="text-sm font-semibold text-slate-800 dark:text-slate-100">Stock Alerts</h2>
      </div>
      {(outOfStock.length > 0 || lowStockAlerts.length > 0) && (
      <Link to="/stocks" className="text-xs font-medium text-blue-600 dark:text-blue-400 hover:underline flex items-center gap-1">
      Manage <ArrowRight className="h-3 w-3" aria-hidden="true" />
      </Link>
      )}
      </CardHeader>
      <CardBody className="p-0">
      {criticalItems.length === 0 ? (
      <div className="flex flex-col items-center justify-center py-8 text-slate-400 gap-2">
      <div className="w-10 h-10 rounded-full bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center">
      <TrendingUp className="h-5 w-5 text-emerald-500" aria-hidden="true" />
      </div>
          <p className="text-sm font-medium text-emerald-600 dark:text-emerald-400">All stock healthy</p>
          </div>
          ) : (
              <div className="divide-y divide-slate-100 dark:divide-slate-800">
                {criticalItems.map((p) => (
                  <div key={p.productId} className="flex items-center justify-between px-4 py-2.5 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                    <div className="flex items-center gap-2.5 min-w-0">
                      <div className={`w-2 h-2 rounded-full shrink-0 ${p.quantity === 0 ? 'bg-red-500' : 'bg-amber-500'}`} aria-hidden="true" />
                      <span className="text-sm text-slate-700 dark:text-slate-200 truncate">{p.name}</span>
                    </div>
                    <div className="flex items-center gap-2 shrink-0 ml-2">
                      <span className="text-xs font-mono text-slate-500 dark:text-slate-400">×{p.quantity}</span>
                      {p.quantity === 0
                        ? <Badge variant="danger" size="sm">Out</Badge>
                        : <Badge variant="warning" size="sm">Low</Badge>}
                    </div>
                  </div>
                ))}
                {(outOfStock.length + lowStockAlerts.length) > 5 && (
                  <div className="px-4 py-2 text-center">
                    <Link to="/stocks" className="text-xs text-blue-600 dark:text-blue-400 hover:underline">
                      +{(outOfStock.length + lowStockAlerts.length) - 5} more items need attention
                    </Link>
                  </div>
                )}
              </div>
            )}
          </CardBody>
        </Card>

        {/* Recent Orders */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center gap-2">
              <ClipboardList className="h-4 w-4 text-slate-400" aria-hidden="true" />
              <h2 className="text-sm font-semibold text-slate-800 dark:text-slate-100">Recent Orders</h2>
            </div>
            <Link to="/orders" className="text-xs font-medium text-blue-600 dark:text-blue-400 hover:underline flex items-center gap-1">
              View all <ArrowRight className="h-3 w-3" aria-hidden="true" />
            </Link>
          </CardHeader>
          <div className="divide-y divide-slate-100 dark:divide-slate-800">
            {isLoading
              ? <div className="py-8 flex justify-center"><Spinner /></div>
              : !orders?.length
                ? <div className="flex flex-col items-center justify-center py-10 text-slate-400 gap-2">
                    <AlertCircle className="h-6 w-6" aria-hidden="true" />
                    <p className="text-sm">No orders yet</p>
                  </div>
                : orders.slice(0, 5).map((order) => (
                    <div key={order.orderId} className="flex items-center justify-between px-5 py-3 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                      <div className="flex items-center gap-3 min-w-0">
                        <div className="w-8 h-8 rounded-full bg-violet-100 dark:bg-violet-900/40 flex items-center justify-center shrink-0">
                          <ClipboardList className="h-3.5 w-3.5 text-violet-600 dark:text-violet-400" aria-hidden="true" />
                        </div>
                        <div className="min-w-0">
                          <p className="text-sm font-semibold text-slate-800 dark:text-slate-100">Order #{order.orderId}</p>
                          <p className="text-xs text-slate-500 dark:text-slate-400 truncate">{order.customer?.name || 'Unknown'}</p>
                        </div>
                      </div>
                      <div className="text-right shrink-0 ml-4">
                        <Badge variant="success" dot>Completed</Badge>
                        <p className="text-[11px] text-slate-400 dark:text-slate-500 mt-1">
                          {order.localDateTime ? new Date(order.localDateTime).toLocaleDateString() : '—'}
                        </p>
                      </div>
                    </div>
                  ))
            }
          </div>
        </Card>
      </div>
    </div>
  );
};
