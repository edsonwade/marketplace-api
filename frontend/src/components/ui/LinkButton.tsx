import { Link, type LinkProps } from 'react-router-dom';

interface LinkButtonProps extends LinkProps {
  variant?: 'primary' | 'secondary' | 'danger' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export const LinkButton = ({
  className = '',
  variant = 'primary',
  size = 'md',
  children,
  to,
  ...rest
}: LinkButtonProps) => {
  const base = 'inline-flex items-center justify-center font-medium rounded-lg transition-all duration-150 focus-visible:outline-2 focus-visible:outline-offset-2 select-none';

  const variants = {
    primary:   'bg-blue-600 text-white hover:bg-blue-700 active:bg-blue-800 focus-visible:outline-blue-500 shadow-sm',
    secondary: 'bg-slate-700 text-white hover:bg-slate-800 active:bg-slate-900 focus-visible:outline-slate-500 shadow-sm',
    danger:    'bg-red-600 text-white hover:bg-red-700 active:bg-red-800 focus-visible:outline-red-500 shadow-sm',
    outline:   'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50 active:bg-slate-100 focus-visible:outline-blue-500',
    ghost:     'text-slate-600 hover:bg-slate-100 active:bg-slate-200 focus-visible:outline-slate-400',
  };

  const sizes = {
    sm: 'px-3 py-1.5 text-xs gap-1.5 h-8',
    md: 'px-4 py-2   text-sm gap-2   h-9',
    lg: 'px-5 py-2.5 text-base gap-2 h-11',
  };

  return (
    <Link
      to={to}
      className={`${base} ${variants[variant]} ${sizes[size]} ${className}`}
      {...rest}
    >
      {children}
    </Link>
  );
};
