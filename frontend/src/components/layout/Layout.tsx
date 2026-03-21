import { useState, useEffect, useCallback } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Header } from './Header';
import { Sidebar } from './Sidebar';

export const Layout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const location = useLocation();

  // Close mobile drawer on route change
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  // Close mobile drawer on Escape
  useEffect(() => {
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setSidebarOpen(false);
    };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, []);

  const toggleSidebar = useCallback(() => setSidebarOpen((v) => !v), []);
  const toggleCollapsed = useCallback(() => setSidebarCollapsed((v) => !v), []);

  return (
    <div className="flex min-h-screen bg-slate-50">
      {/* ---- Mobile overlay backdrop ---- */}
      {sidebarOpen && (
        <div
          aria-hidden="true"
          className="fixed inset-0 z-30 bg-black/40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* ---- Sidebar (mobile: drawer | desktop: fixed) ---- */}
      <Sidebar
        isOpen={sidebarOpen}
        isCollapsed={sidebarCollapsed}
        onClose={() => setSidebarOpen(false)}
        onToggleCollapse={toggleCollapsed}
      />

      {/* ---- Main content area ---- */}
      <div
        className={`
          flex flex-col flex-1 min-w-0 transition-all duration-300
          ${sidebarCollapsed ? 'lg:ml-[68px]' : 'lg:ml-64'}
        `}
      >
        {/* Skip to main content — accessibility */}
        <a href="#main-content" className="skip-to-content">
          Skip to main content
        </a>

        <Header onMenuClick={toggleSidebar} />

        <main
          id="main-content"
          tabIndex={-1}
          className="flex-1 px-4 py-6 md:px-6 md:py-8 focus:outline-none"
        >
          <Outlet />
        </main>
      </div>
    </div>
  );
};
