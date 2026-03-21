import { ClipboardList } from 'lucide-react';
import { Badge } from '../../components/ui/Badge';
import { Alert } from '../../components/ui/Alert';
import { Spinner } from '../../components/ui/Spinner';
import { Table, TableHead, TableBody, TableRow, TableHeadCell, TableCell } from '../../components/ui/Table';
import { useOrders } from '../../services';

export const OrdersPage = () => {
  const { data: orders, isLoading, error } = useOrders();

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;
  if (error) return <Alert variant="error" message="Failed to load orders" className="max-w-lg" />;

  return (
    <div className="space-y-6 max-w-7xl">

      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Orders</h1>
        <p className="text-sm text-slate-500 mt-0.5">{orders?.length ?? 0} total orders</p>
      </div>

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
          {!orders || orders.length === 0 ? (
            <TableRow>
              <TableCell colSpan={5} className="text-center py-12 text-slate-400">
                <ClipboardList className="h-8 w-8 mx-auto mb-2 opacity-30" aria-hidden="true" />
                No orders found
              </TableCell>
            </TableRow>
          ) : (
            orders.map((order) => (
              <TableRow key={order.orderId}>
                <TableCell>
                  <span className="font-semibold text-slate-800">#{order.orderId}</span>
                </TableCell>
                <TableCell className="text-slate-500">
                  {order.localDateTime
                    ? new Date(order.localDateTime).toLocaleDateString('en-GB', {
                        day: '2-digit', month: 'short', year: 'numeric',
                      })
                    : '—'}
                </TableCell>
                <TableCell>
                  <span className="font-medium text-slate-700">
                    {order.customer?.name || <span className="text-slate-400 italic">Unknown</span>}
                  </span>
                </TableCell>
                <TableCell>
                  <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-slate-100 text-xs font-semibold text-slate-600">
                    {order.orderItems?.length ?? 0}
                  </span>
                </TableCell>
                <TableCell>
                  <Badge variant="success" dot>Completed</Badge>
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
};
