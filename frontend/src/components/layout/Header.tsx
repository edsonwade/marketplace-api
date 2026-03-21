import { Link } from 'react-router-dom';
import { Menu, ShoppingCart, LogOut, Package, User, ChevronDown } from 'lucide-react';
import { useAuthStore } from '../../store';
import { useLogout } from '../../services';
import { useCartStore } from '../../store';
import { NotificationBell } from '../notifications/NotificationBell';
import { useNavigate } from 'react-router-dom';
import { useState, useRef, useEffect } from 'react';

interface HeaderProps {
  onMenuClick: () => void;
}

export const Header = ({ onMenuClick }: HeaderProps) => {
  const { isAuthenticated, user } = useAuthStore();
  const navigate = useNavigate();
  const logout = useLogout();
  const cartItems = useCartStore((s) => s.items);
  const cartCount = cartItems.reduce((sum, i) => sum + i.quantity, 0);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // Close user menu on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setUserMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleLogout = async () => {
    setUserMenuOpen(false);
    await logout.mutateAsync();
    navigate('/login');
  };

  const initials = user?.name
    ? user.name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2)
    : 'U';

  return (
    <header className="sticky top-0 z-20 flex items-center h-16 px-4 md:px-6 bg-white border-b border-slate-200 shadow-sm gap-4">

      {/* Hamburger — mobile only */}
      <button
        type="button"
        aria-label="Open navigation menu"
        onClick={onMenuClick}
        className="lg:hidden inline-flex items-center justify-center p-2 rounded-lg text-slate-500 hover:bg-slate-100 transition-colors"
      >
        <Menu className="h-5 w-5" />
      </button>

      {/* Logo — visible on mobile (sidebar hidden) */}
      <Link
        to="/"
        aria-label="Marketplace home"
        className="flex items-center gap-2 lg:hidden font-bold text-slate-800"
      >
        <div className="flex items-center justify-center w-8 h-8 rounded-lg bg-blue-600">
          <Package className="h-4 w-4 text-white" />
        </div>
        <span className="text-sm">Marketplace</span>
      </Link>

      {/* Spacer */}
      <div className="flex-1" />

      {/* Right-side actions */}
      {isAuthenticated ? (
        <div className="flex items-center gap-1 md:gap-2">
          {/* Cart */}
          <Link
            to="/cart"
            aria-label={`Shopping cart, ${cartCount} item${cartCount !== 1 ? 's' : ''}`}
            className="relative p-2 rounded-lg text-slate-500 hover:bg-slate-100 transition-colors"
          >
            <ShoppingCart className="h-5 w-5" aria-hidden="true" />
            {cartCount > 0 && (
              <span
                aria-hidden="true"
                className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-blue-600 text-[10px] font-bold text-white leading-none"
              >
                {cartCount > 9 ? '9+' : cartCount}
              </span>
            )}
          </Link>

          {/* Notifications */}
          <NotificationBell />

          {/* User menu */}
          <div className="relative" ref={menuRef}>
            <button
              type="button"
              aria-haspopup="true"
              aria-expanded={userMenuOpen}
              aria-label="User menu"
              onClick={() => setUserMenuOpen((v) => !v)}
              className="flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-lg hover:bg-slate-100 transition-colors"
            >
              <div
                aria-hidden="true"
                className="w-7 h-7 rounded-full bg-blue-600 flex items-center justify-center text-white text-xs font-bold select-none"
              >
                {initials}
              </div>
              <span className="hidden md:block text-sm font-medium text-slate-700 max-w-[120px] truncate">
                {user?.name || 'User'}
              </span>
              <ChevronDown
                aria-hidden="true"
                className={`hidden md:block h-3.5 w-3.5 text-slate-400 transition-transform duration-150 ${userMenuOpen ? 'rotate-180' : ''}`}
              />
            </button>

            {userMenuOpen && (
              <div
                role="menu"
                className="absolute right-0 mt-1.5 w-52 bg-white rounded-xl shadow-xl border border-slate-200 py-1.5 z-50"
              >
                <div className="px-4 py-2.5 border-b border-slate-100 mb-1">
                  <p className="text-sm font-semibold text-slate-800 truncate">{user?.name || 'User'}</p>
                  <p className="text-xs text-slate-500 truncate">{user?.email}</p>
                </div>
                <button
                  role="menuitem"
                  type="button"
                  onClick={() => { setUserMenuOpen(false); navigate('/settings'); }}
                  className="flex w-full items-center gap-2.5 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50 transition-colors"
                >
                  <User className="h-4 w-4 text-slate-400" aria-hidden="true" />
                  Settings
                </button>
                <button
                  role="menuitem"
                  type="button"
                  onClick={handleLogout}
                  disabled={logout.isPending}
                  className="flex w-full items-center gap-2.5 px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors disabled:opacity-50"
                >
                  <LogOut className="h-4 w-4" aria-hidden="true" />
                  {logout.isPending ? 'Signing out…' : 'Sign out'}
                </button>
              </div>
            )}
          </div>
        </div>
      ) : (
        <div className="flex items-center gap-2">
          <Link
            to="/login"
            className="px-3 py-1.5 text-sm font-medium text-slate-600 hover:text-slate-900 transition-colors"
          >
            Sign in
          </Link>
          <Link
            to="/register"
            className="px-3 py-1.5 text-sm font-medium bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors shadow-sm"
          >
            Register
          </Link>
        </div>
      )}
    </header>
  );
};
