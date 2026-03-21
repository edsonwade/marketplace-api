import { useState, useEffect, useCallback } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Header } from './Header';
import { Sidebar } from './Sidebar';

export const Layout = () => {
  const [sidebarOpen,      setSidebarOpen]      = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const location = useLocation();

  useEffect(() => { setSidebarOpen(false); }, [location.pathname]);
  useEffect(() => {
    const fn = (e: KeyboardEvent) => { if (e.key === 'Escape') setSidebarOpen(false); };
    document.addEventListener('keydown', fn);
    return () => document.removeEventListener('keydown', fn);
  }, []);

  const toggleSidebar   = useCallback(() => setSidebarOpen((v) => !v), []);
  const toggleCollapsed = useCallback(() => setSidebarCollapsed((v) => !v), []);

  return (
    <div className="flex min-h-screen bg-slate-100 dark:bg-slate-950">
      {sidebarOpen && (
        <div aria-hidden="true" className="fixed inset-0 z-30 bg-black/50 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      <Sidebar isOpen={sidebarOpen} isCollapsed={sidebarCollapsed} onClose={() => setSidebarOpen(false)} onToggleCollapse={toggleCollapsed} />

      <div className={`flex flex-col flex-1 min-w-0 transition-all duration-300 ${sidebarCollapsed ? 'lg:ml-[68px]' : 'lg:ml-64'}`}>
        <a href="#main-content" className="skip-to-content">Skip to main content</a>
        <Header onMenuClick={toggleSidebar} />
        <main id="main-content" tabIndex={-1} className="flex-1 px-4 py-6 md:px-6 md:py-8 focus:outline-none">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
