import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Activity, Mail, Lock, Eye, EyeOff, AlertCircle, CheckCircle } from 'lucide-react';
import { authApi } from '../api';
import { useAuth } from '../context/AuthContext';

const DEMO_ACCOUNTS = [
  { role: 'Super Admin',        email: 'admin@kyrobank.com',      badge: 'bg-red-100 text-red-700' },
  { role: 'Payment Ops',        email: 'ops@kyrobank.com',        badge: 'bg-blue-100 text-blue-700' },
  { role: 'Compliance Officer', email: 'compliance@kyrobank.com', badge: 'bg-purple-100 text-purple-700' },
  { role: 'AML Reviewer',       email: 'aml@kyrobank.com',        badge: 'bg-amber-100 text-amber-700' },
  { role: 'Corp Maker',         email: 'maker@kyrobank.com',      badge: 'bg-green-100 text-green-700' },
  { role: 'Corp Checker',       email: 'checker@kyrobank.com',    badge: 'bg-teal-100 text-teal-700' },
  { role: 'Corp Approver',      email: 'approver@kyrobank.com',   badge: 'bg-indigo-100 text-indigo-700' },
  { role: 'Corp Admin',         email: 'corpadmin@kyrobank.com',   badge: 'bg-pink-100 text-pink-700' },
];

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await authApi.login(email, password);
      login(res.data);
      navigate('/', { replace: true });
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
        'Cannot reach the banking server. Start the backend on http://localhost:8080 and try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  const quickLogin = (demoEmail: string) => {
    setEmail(demoEmail);
    setPassword('password123');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-950 to-slate-900 flex">
      {/* Left Panel — Branding */}
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between p-16 text-white">
        <div className="flex items-center gap-3">
          <div className="bg-blue-500 p-2.5 rounded-xl">
            <Activity size={28} />
          </div>
          <span className="text-2xl font-bold tracking-tight">KyroBank CMS</span>
        </div>

        <div>
          <h1 className="text-5xl font-extrabold leading-tight mb-6">
            Enterprise<br />Banking<br />
            <span className="text-blue-400">Platform</span>
          </h1>
          <p className="text-slate-300 text-lg mb-10">
            Production-ready banking infrastructure with full JWT authentication,
            Google OAuth2, 30+ role-based access controls, and real-time transaction processing.
          </p>
          <div className="grid grid-cols-2 gap-4">
            {[
              '🔐 JWT + OAuth2 Auth',
              '🏦 NEFT / RTGS / IMPS',
              '🛡️ Maker-Checker Workflow',
              '🤖 AI Transaction Analysis',
              '🌐 SWIFT / ACH / UPI',
              '📊 AML & Sanctions Screening',
            ].map(f => (
              <div key={f} className="flex items-center gap-2 bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-sm text-slate-300">
                <CheckCircle size={14} className="text-blue-400 shrink-0" />
                {f}
              </div>
            ))}
          </div>
        </div>

        <p className="text-slate-500 text-sm">© 2026 KyroDataTech. All rights reserved.</p>
      </div>

      {/* Right Panel — Login Form */}
      <div className="flex-1 flex items-center justify-center p-8 bg-white">
        <div className="w-full max-w-md">
          <div className="mb-8">
            <div className="flex items-center gap-2 lg:hidden mb-6">
              <div className="bg-blue-600 p-2 rounded-lg">
                <Activity size={20} className="text-white" />
              </div>
              <span className="text-xl font-bold text-slate-900">KyroBank CMS</span>
            </div>
            <h2 className="text-3xl font-bold text-slate-900">Sign in</h2>
            <p className="text-slate-500 mt-1">Access your banking dashboard</p>
          </div>

          {error && (
            <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
              <AlertCircle size={18} className="text-red-500 shrink-0 mt-0.5" />
              <p className="text-sm text-red-700">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">Email address</label>
              <div className="relative">
                <Mail size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  placeholder="admin@kyrobank.com"
                  className="w-full pl-10 pr-4 py-3 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">Password</label>
              <div className="relative">
                <Lock size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                <input
                  type={showPass ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-12 py-3 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
                />
                <button
                  type="button"
                  onClick={() => setShowPass(!showPass)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                >
                  {showPass ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl transition disabled:opacity-60 flex items-center justify-center gap-2"
            >
              {loading ? (
                <><span className="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full" /> Signing in...</>
              ) : 'Sign In'}
            </button>
          </form>

          {/* Google OAuth2 Button */}
          <div className="mt-4">
            <div className="relative flex items-center my-4">
              <div className="flex-1 border-t border-slate-200" />
              <span className="mx-3 text-xs text-slate-400 font-medium">OR</span>
              <div className="flex-1 border-t border-slate-200" />
            </div>
            <a
              href={`${window.location.protocol}//${window.location.hostname}:8080/oauth2/authorization/google`}
              className="w-full flex items-center justify-center gap-3 py-3 px-4 border border-slate-200 rounded-xl hover:bg-slate-50 transition text-sm font-medium text-slate-700"
            >
              <svg className="h-5 w-5" viewBox="0 0 24 24">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
              Continue with Google
            </a>
          </div>

          {/* Demo Accounts Panel */}
          <div className="mt-6 p-4 bg-slate-50 rounded-xl border border-slate-200">
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3">
              🎭 Demo Accounts — Click to autofill (password: password123)
            </p>
            <div className="grid grid-cols-2 gap-2">
              {DEMO_ACCOUNTS.map(acc => (
                <button
                  key={acc.email}
                  type="button"
                  onClick={() => quickLogin(acc.email)}
                  className="text-left p-2 rounded-lg hover:bg-white border border-transparent hover:border-slate-200 transition"
                >
                  <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${acc.badge}`}>
                    {acc.role}
                  </span>
                  <p className="text-xs text-slate-500 mt-1 truncate">{acc.email}</p>
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
