import { useState } from 'react';
import { ClipboardList, Search, Filter } from 'lucide-react';
import { Badge } from '../../components/ui/Badge';
import { Alert } from '../../components/ui/Alert';
import { Spinner } from '../../components/ui/Spinner';
import { Table, TableHead, TableBody, TableRow, TableHeadCell, TableCell } from '../../components/ui/Table';
import { usePaginatedData } from '../../hooks/usePaginatedData';
import { Paginator } from '../../hooks/Paginator';
import { useOrders } from '../../services';
import type { Order } from '../../api/types';

type DateFilter = 'all' | 'today' | 'week' | 'month';

const isInRange = (dateStr: string | undefined, filter: DateFilter) => {
  if (!dateStr || filter === 'all') return true;
  const d   = new Date(dateStr).getTime();
  const now = Date.now();
  if (filter === 'today') return now - d < 86_400_000;
  if (filter === 'week')  return now - d < 7 * 86_400_000;
  if (filter === 'month') return now - d < 30 * 86_400_000;
  return true;
};

export const OrdersPage = () => {
  const [dateFilter, setDateFilter] = useState<DateFilter>('all');
  const { data: orders, isLoading, error } = useOrders();

  const filteredByDate = (orders ?? []).filter((o) => isInRange(o.localDateTime, dateFilter));

  const { items, query, setQuery, page, setPage, totalPages, totalItems, pageSize } =
    usePaginatedData<Order>({
      data: filteredByDate,
      pageSize: 10,
      filterFn: (o, q) =>
        String(o.orderId).includes(q) ||
        (o.customer?.name ?? '').toLowerCase().includes(q),
    });

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;
  if (error)     return <Alert variant="error" message="Failed to load orders" className="max-w-lg" />;

  const DATE_FILTERS: { key: DateFilter; label: string }[] = [
    { key: 'all',   label: 'All time' },
    { key: 'today', label: 'Today' },
    { key: 'week',  label: 'This week' },
    { key: 'month', label: 'This month' },
  ];

  return (
    <div className="space-y-5 max-w-7xl">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Orders</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">{orders?.length ?? 0} total orders</p>
      </div>

      {/* Filters row */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1 max-w-xs">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" aria-hidden="true" />
          <input type="search" placeholder="Search by ID or customer…" value={query} onChange={(e) => setQuery(e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-sm border border-slate-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            aria-label="Search orders" />
        </div>
        <div className="flex items-center gap-1.5 flex-wrap" role="group" aria-label="Filter by date">
          <Filter className="h-4 w-4 text-slate-400 shrink-0" aria-hidden="true" />
          {DATE_FILTERS.map(({ key, label }) => (
            <button key={key} type="button" onClick={() => setDateFilter(key)}
              className={`px-3 py-1 text-xs font-medium rounded-full border transition-colors
                ${dateFilter === key
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-300 border-slate-300 dark:border-slate-600 hover:border-blue-400'}`}>
              {label}
            </button>
          ))}
        </div>
      </div>

      <div>
        <Table>
          <TableHead>
            <TableRow>
              <TableHeadCell>Order</TableHeadCell>
              <TableHeadCell>Date</TableHeadCell>
              <TableHeadCell>Customer</TableHeadCell>
              <TableHeadCell>Items</TableHeadCell>
              <TableHeadCell>Status</TableHeadCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {items.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-10 text-slate-400 dark:text-slate-500">
                  <ClipboardList className="h-8 w-8 mx-auto mb-2 opacity-30" aria-hidden="true" />
                  No orders found
                </TableCell>
              </TableRow>
            ) : items.map((order) => (
              <TableRow key={order.orderId}>
                <TableCell className="font-semibold text-slate-800 dark:text-slate-100">#{order.orderId}</TableCell>
                <TableCell className="text-slate-500 dark:text-slate-400">
                  {order.localDateTime
                    ? new Date(order.localDateTime).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
                    : '—'}
                </TableCell>
                <TableCell className="font-medium text-slate-700 dark:text-slate-200">
                  {order.customer?.name ?? <span className="italic text-slate-400">Unknown</span>}
                </TableCell>
                <TableCell>
                  <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-slate-100 dark:bg-slate-800 text-xs font-semibold text-slate-600 dark:text-slate-300">
                    {order.orderItems?.length ?? 0}
                  </span>
                </TableCell>
                <TableCell>
                  <Badge variant="success" dot>Completed</Badge>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <Paginator page={page} totalPages={totalPages} totalItems={totalItems} pageSize={pageSize} onPage={setPage} />
      </div>
    </div>
  );
};
