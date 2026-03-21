import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Package, LogIn } from 'lucide-react';
import { useState } from 'react';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Alert } from '../../components/ui/Alert';
import { useLogin } from '../../services';
import { useAuthStore } from '../../store';

interface LoginForm {
  email: string;
  password: string;
}

const getLoginError = (error: unknown): string => {
  if (!error) return 'Login failed. Please try again.';
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const resp = (error as { response?: { data?: { errorCode?: string; message?: string } } }).response;
    const { errorCode, message } = resp?.data ?? {};
    if (errorCode === 'EMAIL_NOT_FOUND') return 'No account found with this email.';
    if (errorCode === 'INCORRECT_PASSWORD') return 'Incorrect password. Please try again.';
    if (errorCode === 'RATE_LIMIT_EXCEEDED' || message?.toLowerCase().includes('rate'))
      return 'Too many attempts. Please wait a few minutes before trying again.';
  }
  return 'Login failed. Please try again.';
};

export const LoginPage = () => {
  const navigate = useNavigate();
  const { setAuth } = useAuthStore();
  const login = useLogin();
  const [showPassword, setShowPassword] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm<LoginForm>();

  const onSubmit = async (data: LoginForm) => {
    try {
      await login.mutateAsync(data);
      setAuth();
      navigate('/dashboard');
    } catch {
      /* handled below via login.isError */
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 p-4">
      {/* Card */}
      <div className="w-full max-w-[400px] bg-white rounded-2xl shadow-2xl overflow-hidden">

        {/* Top accent bar */}
        <div className="h-1 bg-gradient-to-r from-blue-500 via-blue-400 to-blue-600" />

        <div className="px-8 py-8">

          {/* Brand */}
          <div className="flex flex-col items-center mb-8">
            <div className="flex items-center justify-center w-12 h-12 rounded-xl bg-blue-600 mb-3 shadow-md">
              <Package className="h-6 w-6 text-white" aria-hidden="true" />
            </div>
            <h1 className="text-xl font-bold text-slate-900">Welcome back</h1>
            <p className="text-sm text-slate-500 mt-1">Sign in to your account</p>
          </div>

          {/* Error */}
          {login.isError && (
            <Alert
              variant="error"
              message={getLoginError(login.error)}
              className="mb-5"
            />
          )}

          {/* Form */}
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <Input
              label="Email address"
              type="email"
              placeholder="you@company.com"
              autoComplete="email"
              error={errors.email?.message}
              {...register('email', {
                required: 'Email is required',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Please enter a valid email',
                },
              })}
            />

            <div className="relative">
              <Input
                label="Password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Enter your password"
                autoComplete="current-password"
                error={errors.password?.message}
                {...register('password', { required: 'Password is required' })}
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                className="absolute right-3 top-[30px] text-slate-400 hover:text-slate-600 transition-colors"
              >
                {showPassword
                  ? <EyeOff className="h-4 w-4" aria-hidden="true" />
                  : <Eye className="h-4 w-4" aria-hidden="true" />}
              </button>
            </div>

            <Button
              type="submit"
              className="w-full mt-2"
              size="lg"
              isLoading={login.isPending}
              disabled={login.isPending}
            >
              <LogIn className="h-4 w-4" aria-hidden="true" />
              Sign in
            </Button>
          </form>

          {/* Footer */}
          <p className="text-center text-sm text-slate-500 mt-6">
            Don't have an account?{' '}
            <Link
              to="/register"
              className="font-semibold text-blue-600 hover:text-blue-700 hover:underline"
            >
              Create one
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};
