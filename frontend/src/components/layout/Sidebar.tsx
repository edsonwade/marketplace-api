import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Package,
  ShoppingCart,
  ClipboardList,
  Users,
  CreditCard,
  Tag,
  Warehouse,
  Settings,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

const NAV_ITEMS = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/products',  icon: Package,         label: 'Products'  },
  { to: '/cart',      icon: ShoppingCart,     label: 'Cart'      },
  { to: '/orders',    icon: ClipboardList,    label: 'Orders'    },
  { to: '/customers', icon: Users,            label: 'Customers' },
  { to: '/payments',  icon: CreditCard,       label: 'Payments'  },
  { to: '/discounts', icon: Tag,              label: 'Discounts' },
  { to: '/stocks',    icon: Warehouse,        label: 'Stock'     },
];

interface SidebarProps {
  isOpen: boolean;
  isCollapsed: boolean;
  onClose: () => void;
  onToggleCollapse: () => void;
}

export const Sidebar = ({ isOpen, isCollapsed, onClose, onToggleCollapse }: SidebarProps) => {
  const sidebarWidth = isCollapsed ? 'lg:w-[68px]' : 'lg:w-64';

  return (
    <aside
      aria-label="Main navigation"
      className={`
        fixed inset-y-0 left-0 z-40 flex flex-col
        bg-slate-900 text-white
        transition-transform duration-300 ease-in-out
        ${sidebarWidth}
        w-64
        ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
      `}
    >
      {/* ---- Brand ---- */}
      <div className={`flex items-center h-16 border-b border-slate-800 shrink-0 ${isCollapsed ? 'justify-center px-3' : 'px-5 gap-3'}`}>
        <div
          aria-hidden="true"
          className="flex items-center justify-center w-8 h-8 rounded-lg bg-blue-600 shrink-0"
        >
          <Package className="h-4.5 w-4.5 text-white h-[18px] w-[18px]" />
        </div>
        {!isCollapsed && (
          <div className="min-w-0">
            <p className="font-bold text-white text-sm leading-none">Marketplace</p>
            <p className="text-[10px] text-slate-500 mt-0.5 uppercase tracking-widest">Admin</p>
          </div>
        )}
      </div>

      {/* ---- Nav Items ---- */}
      <nav className="flex-1 overflow-y-auto py-4 px-2 space-y-0.5" aria-label="Sidebar navigation">
        {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            aria-current={undefined}
            title={isCollapsed ? label : undefined}
            className={({ isActive }) => `
              flex items-center rounded-lg transition-colors duration-100 group relative
              ${isCollapsed ? 'justify-center px-0 py-2.5 mx-auto w-11 h-11' : 'gap-3 px-3 py-2.5'}
              ${isActive
                ? 'bg-blue-600/15 text-blue-400'
                : 'text-slate-400 hover:bg-slate-800 hover:text-white'
              }
            `}
          >
            {({ isActive }) => (
              <>
                {/* Active indicator bar */}
                {isActive && (
                  <span
                    aria-hidden="true"
                    className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-5 bg-blue-400 rounded-r-full"
                  />
                )}
                <Icon
                  aria-hidden="true"
                  className={`shrink-0 h-[18px] w-[18px] ${isActive ? 'text-blue-400' : 'text-slate-400 group-hover:text-white'}`}
                />
                {!isCollapsed && (
                  <span className="text-sm font-medium">{label}</span>
                )}
                {/* Tooltip for collapsed state */}
                {isCollapsed && (
                  <span
                    aria-hidden="true"
                    className="pointer-events-none absolute left-full ml-2 px-2 py-1 text-xs font-medium text-white bg-slate-800 rounded-md whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity z-50"
                  >
                    {label}
                  </span>
                )}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* ---- Settings + Collapse ---- */}
      <div className="border-t border-slate-800 px-2 py-3 space-y-0.5 shrink-0">
        <NavLink
          to="/settings"
          title={isCollapsed ? 'Settings' : undefined}
          className={({ isActive }) => `
            flex items-center rounded-lg transition-colors duration-100 group relative
            ${isCollapsed ? 'justify-center px-0 py-2.5 mx-auto w-11 h-11' : 'gap-3 px-3 py-2.5'}
            ${isActive
              ? 'bg-blue-600/15 text-blue-400'
              : 'text-slate-400 hover:bg-slate-800 hover:text-white'
            }
          `}
        >
          {({ isActive }) => (
            <>
              {isActive && (
                <span aria-hidden="true" className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-5 bg-blue-400 rounded-r-full" />
              )}
              <Settings aria-hidden="true" className={`shrink-0 h-[18px] w-[18px] ${isActive ? 'text-blue-400' : 'text-slate-400 group-hover:text-white'}`} />
              {!isCollapsed && <span className="text-sm font-medium">Settings</span>}
              {isCollapsed && (
                <span aria-hidden="true" className="pointer-events-none absolute left-full ml-2 px-2 py-1 text-xs font-medium text-white bg-slate-800 rounded-md whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity z-50">
                  Settings
                </span>
              )}
            </>
          )}
        </NavLink>

        {/* Collapse toggle — desktop only */}
        <button
          type="button"
          onClick={onToggleCollapse}
          aria-label={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          title={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          className={`
            hidden lg:flex items-center rounded-lg transition-colors duration-100
            text-slate-500 hover:bg-slate-800 hover:text-white
            ${isCollapsed ? 'justify-center mx-auto w-11 h-11' : 'gap-3 w-full px-3 py-2.5'}
          `}
        >
          {isCollapsed
            ? <ChevronRight aria-hidden="true" className="h-[18px] w-[18px]" />
            : (
              <>
                <ChevronLeft aria-hidden="true" className="h-[18px] w-[18px]" />
                <span className="text-sm font-medium">Collapse</span>
              </>
            )
          }
        </button>

        {/* Close button — mobile only */}
        <button
          type="button"
          onClick={onClose}
          aria-label="Close navigation"
          className="lg:hidden flex w-full items-center gap-3 px-3 py-2.5 rounded-lg text-slate-500 hover:bg-slate-800 hover:text-white transition-colors text-sm font-medium"
        >
          <ChevronLeft aria-hidden="true" className="h-[18px] w-[18px]" />
          Close menu
        </button>
      </div>
    </aside>
  );
};
