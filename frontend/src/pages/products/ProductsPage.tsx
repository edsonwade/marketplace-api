import { useState } from 'react';
import { Plus, Search, Edit2, Trash2, ShoppingCart as CartIcon, X, Package } from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Alert } from '../../components/ui/Alert';
import { Spinner } from '../../components/ui/Spinner';
import { Table, TableHead, TableBody, TableRow, TableHeadCell, TableCell } from '../../components/ui/Table';
import { useProducts, useCreateProduct, useDeleteProduct, useUpdateProduct } from '../../services';
import { useCartStore } from '../../store';
import type { Product } from '../../api/types';

export const ProductsPage = () => {
  const [isAdding, setIsAdding] = useState(false);
  const [newProduct, setNewProduct] = useState({ name: '', quantity: 0 });
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editQuantity, setEditQuantity] = useState(0);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [search, setSearch] = useState('');

  const { data: products, isLoading, error } = useProducts();
  const createProduct = useCreateProduct();
  const deleteProduct = useDeleteProduct();
  const updateProduct = useUpdateProduct();
  const { addItem } = useCartStore();

  const filtered = products?.filter((p) =>
    p.name.toLowerCase().includes(search.toLowerCase())
  ) ?? [];

  const handleCreate = async () => {
    if (!newProduct.name.trim()) return;
    await createProduct.mutateAsync({ name: newProduct.name, quantity: newProduct.quantity });
    setNewProduct({ name: '', quantity: 0 });
    setIsAdding(false);
  };

  const handleUpdate = async (product: Product) => {
    await updateProduct.mutateAsync({ id: product.productId, data: { name: product.name, quantity: editQuantity }, version: product.version });
    setEditingId(null);
  };

  const handleDelete = async (id: number) => {
    await deleteProduct.mutateAsync(id);
    setDeleteId(null);
  };

  const handleAddToCart = (product: Product) => {
    addItem({ productId: product.productId, productName: product.name, quantity: 1, price: 10 });
  };

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;
  if (error) return <Alert variant="error" message="Failed to load products" className="max-w-lg" />;

  return (
    <div className="space-y-6 max-w-7xl">

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Products</h1>
          <p className="text-sm text-slate-500 mt-0.5">{products?.length ?? 0} total products</p>
        </div>
        <Button onClick={() => setIsAdding(true)} className="self-start sm:self-auto">
          <Plus className="h-4 w-4" aria-hidden="true" />
          Add Product
        </Button>
      </div>

      {/* Add form */}
      {isAdding && (
        <Card>
          <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-800">New Product</h2>
            <button
              type="button"
              aria-label="Close form"
              onClick={() => setIsAdding(false)}
              className="text-slate-400 hover:text-slate-600 transition-colors"
            >
              <X className="h-4 w-4" aria-hidden="true" />
            </button>
          </div>
          <div className="px-5 py-4 flex flex-col sm:flex-row gap-3 items-end">
            <Input
              label="Product Name"
              value={newProduct.name}
              onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })}
              placeholder="e.g., Wireless Headphones"
              className="sm:max-w-xs"
            />
            <Input
              label="Initial Quantity"
              type="number"
              min={0}
              value={newProduct.quantity}
              onChange={(e) => setNewProduct({ ...newProduct, quantity: parseInt(e.target.value) || 0 })}
              className="sm:w-32"
            />
            <div className="flex gap-2">
              <Button onClick={handleCreate} isLoading={createProduct.isPending}>
                Save
              </Button>
              <Button variant="outline" onClick={() => setIsAdding(false)}>
                Cancel
              </Button>
            </div>
          </div>
        </Card>
      )}

      {/* Search */}
      <div className="relative max-w-xs">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" aria-hidden="true" />
        <input
          type="search"
          placeholder="Search products…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 placeholder:text-slate-400"
          aria-label="Search products"
        />
      </div>

      {/* Table */}
      <Table>
        <TableHead>
          <TableRow>
            <TableHeadCell>ID</TableHeadCell>
            <TableHeadCell>Product</TableHeadCell>
            <TableHeadCell>Quantity</TableHeadCell>
            <TableHeadCell>Status</TableHeadCell>
            <TableHeadCell className="text-right">Actions</TableHeadCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {filtered.length === 0 ? (
            <TableRow>
              <TableCell colSpan={5} className="text-center py-12 text-slate-400">
                <Package className="h-8 w-8 mx-auto mb-2 opacity-30" aria-hidden="true" />
                {search ? 'No products match your search' : 'No products yet'}
              </TableCell>
            </TableRow>
          ) : (
            filtered.map((product) => (
              <TableRow key={product.productId}>
                <TableCell className="font-mono text-xs text-slate-400">#{product.productId}</TableCell>
                <TableCell className="font-medium text-slate-800">{product.name}</TableCell>
                <TableCell>
                  {editingId === product.productId ? (
                    <Input
                      type="number"
                      min={0}
                      value={editQuantity}
                      onChange={(e) => setEditQuantity(parseInt(e.target.value) || 0)}
                      className="w-24"
                      aria-label="Edit quantity"
                    />
                  ) : (
                    <span className="font-medium">{product.quantity}</span>
                  )}
                </TableCell>
                <TableCell>
                  {product.quantity === 0 ? (
                    <Badge variant="danger" dot>Out of Stock</Badge>
                  ) : product.quantity < 10 ? (
                    <Badge variant="warning" dot>Low Stock</Badge>
                  ) : (
                    <Badge variant="success" dot>In Stock</Badge>
                  )}
                </TableCell>
                <TableCell>
                  <div className="flex items-center justify-end gap-1.5">
                    {editingId === product.productId ? (
                      <>
                        <Button size="sm" onClick={() => handleUpdate(product)} isLoading={updateProduct.isPending}>
                          Save
                        </Button>
                        <Button size="sm" variant="outline" onClick={() => setEditingId(null)}>
                          Cancel
                        </Button>
                      </>
                    ) : deleteId === product.productId ? (
                      <>
                        <span className="text-xs text-slate-500 mr-1">Delete?</span>
                        <Button size="sm" variant="danger" onClick={() => handleDelete(product.productId)} isLoading={deleteProduct.isPending}>
                          Confirm
                        </Button>
                        <Button size="sm" variant="outline" onClick={() => setDeleteId(null)}>
                          Cancel
                        </Button>
                      </>
                    ) : (
                      <>
                        <Button
                          size="sm"
                          variant="outline"
                          aria-label={`Edit ${product.name}`}
                          onClick={() => { setEditingId(product.productId); setEditQuantity(product.quantity); }}
                        >
                          <Edit2 className="h-3.5 w-3.5" aria-hidden="true" />
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          aria-label={`Add ${product.name} to cart`}
                          onClick={() => handleAddToCart(product)}
                          className="text-blue-600 hover:bg-blue-50"
                        >
                          <CartIcon className="h-3.5 w-3.5" aria-hidden="true" />
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          aria-label={`Delete ${product.name}`}
                          onClick={() => setDeleteId(product.productId)}
                          className="text-red-500 hover:bg-red-50"
                        >
                          <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
                        </Button>
                      </>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
};
