import React, { useEffect, useState } from 'react';
import {
  LayoutDashboard, Users, CreditCard, ShieldCheck, Activity,
  LogOut, Banknote, Archive, Layers, Bot, ChevronRight,
  TrendingUp, AlertTriangle, CheckCircle2, XCircle,
  Menu, X, Bell
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { userApi, makerCheckerApi, paymentApi, transactionApi } from '../api';

// ─── Types ───────────────────────────────────────────────────────────────────
type Page = 'dashboard' | 'users' | 'transactions' | 'approvals' | 'payments' | 'collections' | 'liquidity' | 'llm';

// ─── Helpers ─────────────────────────────────────────────────────────────────
const statusBadge: Record<string, string> = {
  ACTIVE:           'bg-emerald-100 text-emerald-700',
  PENDING_APPROVAL: 'bg-amber-100 text-amber-700',
  SUSPENDED:        'bg-red-100 text-red-700',
  DEACTIVATED:      'bg-slate-100 text-slate-500',
  COMPLETED:        'bg-emerald-100 text-emerald-700',
  AML_FLAGGED:      'bg-red-100 text-red-700',
  PENDING:          'bg-amber-100 text-amber-700',
  PROCESSING:       'bg-blue-100 text-blue-700',
};

const roleBadge = (role: string) => {
  if (role.includes('SUPER_ADMIN')) return 'bg-red-100 text-red-700';
  if (role.includes('BANK_')) return 'bg-blue-100 text-blue-700';
  if (role.includes('CORP_')) return 'bg-green-100 text-green-700';
  return 'bg-slate-100 text-slate-600';
};

const formatRole = (role: string) =>
  role.replace('ROLE_', '').replace(/_/g, ' ').toLowerCase()
    .replace(/\b\w/g, c => c.toUpperCase());

// ─── Sidebar ─────────────────────────────────────────────────────────────────
const navItems = [
  { id: 'dashboard',    label: 'Dashboard',       icon: LayoutDashboard },
  { id: 'users',        label: 'User Management', icon: Users },
  { id: 'transactions', label: 'Transactions',    icon: CreditCard },
  { id: 'approvals',    label: 'Maker-Checker',   icon: ShieldCheck },
  { id: 'payments',     label: 'Payments',        icon: Banknote },
  { id: 'collections',  label: 'Collections',     icon: Archive },
  { id: 'liquidity',    label: 'Liquidity',       icon: Layers },
  { id: 'llm',          label: 'AI Analysis',     icon: Bot },
];

const Sidebar = ({ page, setPage, open, setOpen }: any) => {
  const { user, logout, isSuperAdmin, isBankStaff } = useAuth();
  const showAdmin = isSuperAdmin() || isBankStaff();

  return (
    <>
      {/* Overlay */}
      {open && <div className="fixed inset-0 bg-black/50 z-20 lg:hidden" onClick={() => setOpen(false)} />}

      <aside className={`fixed lg:relative z-30 inset-y-0 left-0 w-64 bg-slate-900 text-white flex flex-col transition-transform duration-200
        ${open ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}`}>
        {/* Logo */}
        <div className="h-16 flex items-center gap-3 px-6 border-b border-slate-800">
          <div className="bg-blue-600 p-1.5 rounded-lg"><Activity size={20} /></div>
          <span className="text-lg font-bold">KyroBank CMS</span>
          <button onClick={() => setOpen(false)} className="ml-auto lg:hidden text-slate-400"><X size={18} /></button>
        </div>

        {/* User profile */}
        <div className="px-4 py-4 border-b border-slate-800">
          <div className="flex items-center gap-3 bg-slate-800 rounded-xl px-3 py-3">
            <div className="h-9 w-9 rounded-full bg-blue-600 flex items-center justify-center text-sm font-bold shrink-0">
              {user?.fullName?.charAt(0) ?? 'U'}
            </div>
            <div className="min-w-0">
              <p className="text-sm font-semibold text-white truncate">{user?.fullName}</p>
              <p className="text-xs text-slate-400 truncate">{formatRole(user?.roles?.[0] ?? '')}</p>
            </div>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
          {navItems.filter(item => {
            if (['users', 'approvals'].includes(item.id) && !showAdmin) return false;
            return true;
          }).map(({ id, label, icon: Icon }) => {
            const active = page === id;
            return (
              <button key={id} onClick={() => { setPage(id as Page); setOpen(false); }}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors
                  ${active ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'}`}>
                <Icon size={18} />
                {label}
                {active && <ChevronRight size={16} className="ml-auto" />}
              </button>
            );
          })}
        </nav>

        <div className="p-3 border-t border-slate-800">
          <button onClick={logout}
            className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-slate-400 hover:text-white hover:bg-slate-800 transition-colors">
            <LogOut size={18} />Logout
          </button>
        </div>
      </aside>
    </>
  );
};

// ─── Topbar ──────────────────────────────────────────────────────────────────
const Topbar = ({ page, setOpen }: any) => {
  const label = navItems.find(n => n.id === page)?.label ?? 'Dashboard';
  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center px-6 gap-4 sticky top-0 z-10">
      <button onClick={() => setOpen(true)} className="lg:hidden text-slate-500 hover:text-slate-800">
        <Menu size={22} />
      </button>
      <h1 className="text-xl font-bold text-slate-800">{label}</h1>
      <div className="ml-auto flex items-center gap-3">
        <span className="hidden sm:flex items-center gap-1.5 text-xs bg-emerald-100 text-emerald-700 px-3 py-1 rounded-full font-medium">
          <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
          Backend Live :8080
        </span>
        <button className="relative text-slate-500 hover:text-slate-800">
          <Bell size={20} />
          <span className="absolute -top-1 -right-1 h-4 w-4 bg-red-500 text-white text-[10px] rounded-full flex items-center justify-center font-bold">3</span>
        </button>
      </div>
    </header>
  );
};

// ─── Stat Card ───────────────────────────────────────────────────────────────
const StatCard = ({ icon: Icon, label, value, sub, color }: any) => (
  <div className="bg-white rounded-2xl border border-slate-200 p-6 flex items-start gap-4">
    <div className={`p-3 rounded-xl ${color}`}><Icon size={22} className="text-white" /></div>
    <div>
      <p className="text-sm text-slate-500">{label}</p>
      <p className="text-2xl font-bold text-slate-900 mt-0.5">{value}</p>
      {sub && <p className="text-xs text-slate-400 mt-0.5">{sub}</p>}
    </div>
  </div>
);

// ─── Dashboard Page ───────────────────────────────────────────────────────────
const DashboardPage = () => {
  const [users, setUsers] = useState<any[]>([]);
  const { user } = useAuth();

  useEffect(() => {
    userApi.getAll().then(r => setUsers(Array.isArray(r.data) ? r.data : [])).catch(() => setUsers([]));
  }, []);

  return (
    <div className="p-6 space-y-6">
      {/* Welcome */}
      <div className="bg-gradient-to-r from-blue-600 to-blue-800 rounded-2xl p-6 text-white">
        <p className="text-blue-200 text-sm mb-1">Welcome back,</p>
        <h2 className="text-2xl font-bold">{user?.fullName} 👋</h2>
        <p className="text-blue-200 text-sm mt-1">Role: {formatRole(user?.roles?.[0] ?? '')} · {new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard icon={Users} label="Total Users" value={users.length} sub="Seeded across all roles" color="bg-blue-500" />
        <StatCard icon={ShieldCheck} label="Pending Approvals" value="0" sub="All clear" color="bg-emerald-500" />
        <StatCard icon={TrendingUp} label="Daily Volume" value="₹42.5M" sub="+5.2% vs yesterday" color="bg-violet-500" />
        <StatCard icon={AlertTriangle} label="AML Alerts" value="12" sub="3 critical" color="bg-red-500" />
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h3 className="font-bold text-slate-900">Registered Users</h3>
          <span className="text-xs text-slate-400">Fetched live from Spring Boot</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
              <tr>
                <th className="px-6 py-3 text-left font-medium">Name</th>
                <th className="px-6 py-3 text-left font-medium">Email</th>
                <th className="px-6 py-3 text-left font-medium">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {users.map(u => (
                <tr key={u.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-6 py-3 font-medium text-slate-900">{u.fullName}</td>
                  <td className="px-6 py-3 text-slate-500">{u.email}</td>
                  <td className="px-6 py-3">
                    <span className={`text-xs font-bold px-2.5 py-1 rounded-full ${statusBadge[u.status] ?? 'bg-slate-100 text-slate-600'}`}>
                      {u.status}
                    </span>
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr><td colSpan={3} className="px-6 py-8 text-center text-slate-400">Loading users from backend…</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Feature cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        {[
          { icon: '🔐', title: 'JWT Authentication', desc: 'Stateless Bearer tokens with 24h expiry and 7-day refresh cycle. All endpoints secured.' },
          { icon: '🌐', title: 'Google OAuth2', desc: 'One-click sign-in via Google. Auto-creates user account on first social login.' },
          { icon: '⚖️', title: 'Maker-Checker Workflow', desc: '4-eyes principle on all critical operations. Makers cannot approve their own requests.' },
          { icon: '🏦', title: 'Payment Rails', desc: 'NEFT, RTGS (min ₹2L), IMPS, UPI (VPA validation), SWIFT (BIC), ACH, Bulk.' },
          { icon: '📊', title: 'AML & Sanctions', desc: 'Every transaction is validated → sanctions screened → AML checked before processing.' },
          { icon: '🤖', title: 'AI Transaction Analysis', desc: 'Ollama-compatible LLM endpoint for intelligent fraud pattern analysis.' },
        ].map(f => (
          <div key={f.title} className="bg-white border border-slate-200 rounded-2xl p-5">
            <div className="text-2xl mb-3">{f.icon}</div>
            <h4 className="font-bold text-slate-900 mb-1">{f.title}</h4>
            <p className="text-sm text-slate-500">{f.desc}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

// ─── Users Page ───────────────────────────────────────────────────────────────
const UsersPage = () => {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    userApi.getAll()
      .then(r => setUsers(Array.isArray(r.data) ? r.data : []))
      .catch(() => setUsers([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="p-6">
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h3 className="font-bold text-slate-900">All Users</h3>
          <button className="text-sm bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700 transition">
            + Add User
          </button>
        </div>
        {loading ? (
          <div className="p-12 text-center text-slate-400">Loading from backend…</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
                <tr>
                  <th className="px-6 py-3 text-left">Name</th>
                  <th className="px-6 py-3 text-left">Email</th>
                  <th className="px-6 py-3 text-left">Role(s)</th>
                  <th className="px-6 py-3 text-left">Status</th>
                  <th className="px-6 py-3 text-left">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {users.map(u => (
                  <tr key={u.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4 font-medium text-slate-900">{u.fullName}</td>
                    <td className="px-6 py-4 text-slate-500">{u.email}</td>
                    <td className="px-6 py-4">
                      <div className="flex flex-wrap gap-1">
                        {(u.roles ?? []).map((r: any) => (
                          <span key={r.id ?? r} className={`text-xs px-2 py-0.5 rounded-full font-medium ${roleBadge(r.roleType ?? r)}`}>
                            {formatRole(r.roleType ?? r)}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`text-xs font-bold px-2.5 py-1 rounded-full ${statusBadge[u.status] ?? 'bg-slate-100'}`}>
                        {u.status}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <button className="text-xs text-blue-600 hover:underline mr-3">Edit</button>
                      <button className="text-xs text-red-500 hover:underline">Deactivate</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

// ─── Approvals Page ───────────────────────────────────────────────────────────
const ApprovalsPage = () => {
  const [requests, setRequests] = useState<any[]>([]);
  const [message, setMessage] = useState('');

  const refresh = () => makerCheckerApi.getPending().then(r => setRequests(Array.isArray(r.data) ? r.data : [])).catch(() => setMessage('Unable to load approval requests.'));

  const approve = async (id: string) => {
    try {
      await makerCheckerApi.approve(id);
      setMessage('Request approved successfully.');
      refresh();
    } catch (error: any) {
      setMessage(error.response?.data?.message ?? 'Approval failed.');
    }
  };

  const reject = async (id: string) => {
    const reason = window.prompt('Enter a rejection reason:');
    if (!reason?.trim()) return;
    try {
      await makerCheckerApi.reject(id, reason.trim());
      setMessage('Request rejected successfully.');
      refresh();
    } catch (error: any) {
      setMessage(error.response?.data?.message ?? 'Rejection failed.');
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  return (
    <div className="p-6 space-y-4">
      <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 text-sm text-amber-800">
        <strong>⚖️ Maker-Checker Enforcement:</strong> You can only approve requests you did NOT create. Self-approval is strictly blocked by the backend.
      </div>
      {message && <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 text-sm text-blue-800">{message}</div>}
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-200">
          <h3 className="font-bold text-slate-900">Pending Approval Requests</h3>
        </div>
        {requests.length === 0 ? (
          <div className="p-12 text-center">
            <CheckCircle2 size={40} className="text-emerald-400 mx-auto mb-3" />
            <p className="text-slate-500 font-medium">All clear — no pending requests</p>
            <p className="text-sm text-slate-400 mt-1">All operations have been reviewed</p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
              <tr>
                <th className="px-6 py-3 text-left">Reference</th>
                <th className="px-6 py-3 text-left">Type</th>
                <th className="px-6 py-3 text-left">Created By</th>
                <th className="px-6 py-3 text-left">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {requests.map((r: any) => (
                <tr key={r.id}>
                  <td className="px-6 py-4 font-mono text-xs">{r.referenceNumber}</td>
                  <td className="px-6 py-4">{r.requestType}</td>
                  <td className="px-6 py-4 text-slate-500">{r.createdBy}</td>
                  <td className="px-6 py-4 flex gap-2">
                      <button onClick={() => approve(r.id)} className="flex items-center gap-1 text-xs bg-emerald-100 text-emerald-700 px-3 py-1 rounded-lg hover:bg-emerald-200">
                      <CheckCircle2 size={13} /> Approve
                    </button>
                    <button onClick={() => reject(r.id)} className="flex items-center gap-1 text-xs bg-red-100 text-red-700 px-3 py-1 rounded-lg hover:bg-red-200">
                      <XCircle size={13} /> Reject
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

// ─── Payments Page ────────────────────────────────────────────────────────────
const PaymentsPage = () => {
  const [type, setType] = useState('NEFT');
  const [amount, setAmount] = useState('');
  const [beneficiary, setBeneficiary] = useState('');
  const [ifsc, setIfsc] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState('');

  const paymentTypes = ['NEFT', 'RTGS', 'IMPS', 'UPI', 'SWIFT', 'ACH', 'BULK'];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await paymentApi.initiate(type, {
        amount: Number(amount),
        currency: type === 'SWIFT' ? 'USD' : 'INR',
        debitAccountNo: 'DEMO-MAKER-ACCOUNT',
        creditAccountNo: type === 'UPI' ? 'UPI-BENEFICIARY' : beneficiary,
        creditAccountName: beneficiary,
        creditIfscCode: ifsc || undefined,
        upiVpa: type === 'UPI' ? beneficiary : undefined,
        swiftBic: type === 'SWIFT' ? beneficiary : undefined,
      });
      setSubmitted(true);
    } catch (requestError: any) {
      setError(requestError.response?.data?.message ?? 'Payment could not be submitted.');
    }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-2">
        {paymentTypes.map(t => (
          <button key={t} onClick={() => setType(t)}
            className={`py-2 px-3 rounded-xl text-sm font-bold border-2 transition
              ${type === t ? 'border-blue-600 bg-blue-600 text-white' : 'border-slate-200 text-slate-600 hover:border-blue-300'}`}>
            {t}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-2xl border border-slate-200 p-6">
          <h3 className="font-bold text-slate-900 mb-4">Initiate {type} Payment</h3>
          {submitted ? (
            <div className="py-8 text-center">
              <CheckCircle2 size={40} className="text-emerald-500 mx-auto mb-3" />
              <p className="font-bold text-slate-900">Payment Submitted!</p>
              <p className="text-sm text-slate-500 mt-1">Routed to Maker-Checker for approval</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700">{error}</div>}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Amount (₹)</label>
                <input type="number" required value={amount} onChange={e => setAmount(e.target.value)} placeholder="e.g. 250000"
                  className="w-full border border-slate-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                {type === 'RTGS' && <p className="text-xs text-amber-600 mt-1">⚠️ RTGS requires minimum ₹2,00,000</p>}
              </div>
              {!['UPI', 'SWIFT'].includes(type) && <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Beneficiary IFSC</label>
                <input required value={ifsc} onChange={e => setIfsc(e.target.value.toUpperCase())} placeholder="HDFC0001234"
                  className="w-full border border-slate-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  {type === 'UPI' ? 'VPA (e.g. user@bank)' : type === 'SWIFT' ? 'BIC Code' : 'Beneficiary Account'}
                </label>
                <input required value={beneficiary} onChange={e => setBeneficiary(e.target.value)}
                  placeholder={type === 'UPI' ? 'user@okicici' : type === 'SWIFT' ? 'CITIUS33' : '000100200030'}
                  className="w-full border border-slate-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 rounded-xl transition text-sm">
                Submit for Approval
              </button>
            </form>
          )}
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-6">
          <h3 className="font-bold text-slate-900 mb-4">Payment Rail Info</h3>
          <div className="space-y-3 text-sm">
            {[
              { rail: 'NEFT', info: 'Batch settlement. RBI business hours. No minimum.' },
              { rail: 'RTGS', info: 'Real-time gross settlement. Min ₹2,00,000. Best for high-value.' },
              { rail: 'IMPS', info: '24×7 instant transfer. Up to ₹5 lakh.' },
              { rail: 'UPI', info: 'VPA-based instant. Up to ₹1 lakh per transaction.' },
              { rail: 'SWIFT', info: 'International wire. BIC/IBAN validated.' },
              { rail: 'ACH', info: 'Batch clearing for salary/ECS payments.' },
              { rail: 'BULK', info: 'CSV upload for batch disbursements.' },
            ].map(({ rail, info }) => (
              <div key={rail} className={`p-3 rounded-lg border-2 ${type === rail ? 'border-blue-500 bg-blue-50' : 'border-slate-100'}`}>
                <span className="font-bold text-slate-800">{rail}</span>
                <span className="text-slate-500 ml-2">{info}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

// ─── Collections Page ─────────────────────────────────────────────────────────
const CollectionsPage = () => (
  <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
    {[
      { title: '🏦 Virtual Accounts', desc: 'Unique virtual IBANs per customer. Auto-reconciles incoming credits against open receivables.', action: 'Create Virtual Account' },
      { title: '📋 Direct Debit (NACH)', desc: 'NPCI-registered mandates for recurring debits. Supports one-time and standing instructions.', action: 'Create Mandate' },
      { title: '📱 QR Code Collection', desc: 'Generate dynamic/static QR codes for UPI collections. Configurable amount and expiry.', action: 'Generate QR' },
      { title: '📊 Receivables Tracking', desc: 'Track outstanding invoices, due dates, and auto-match incoming payments.', action: 'View Receivables' },
    ].map(m => (
      <div key={m.title} className="bg-white rounded-2xl border border-slate-200 p-6">
        <h3 className="text-xl mb-2">{m.title}</h3>
        <p className="text-sm text-slate-500 mb-4">{m.desc}</p>
        <button className="w-full py-2.5 bg-slate-900 hover:bg-slate-700 text-white text-sm font-medium rounded-xl transition">
          {m.action}
        </button>
      </div>
    ))}
  </div>
);

// ─── Liquidity Page ───────────────────────────────────────────────────────────
const LiquidityPage = () => (
  <div className="p-6 grid grid-cols-1 md:grid-cols-3 gap-6">
    {[
      { title: '🔄 Cash Sweeping', desc: 'Automatically sweeps surplus balances to a target account. Zero-balance or target-balance modes.', color: 'from-blue-500 to-blue-700' },
      { title: '🏊 Notional Pooling', desc: 'Aggregate balances across entities for interest optimization. No physical fund movement required.', color: 'from-violet-500 to-violet-700' },
      { title: '🔗 Inter-Company', desc: 'Manage and reconcile inter-company loans and transfers within corporate group hierarchy.', color: 'from-teal-500 to-teal-700' },
    ].map(m => (
      <div key={m.title} className={`bg-gradient-to-br ${m.color} text-white rounded-2xl p-6`}>
        <h3 className="text-xl font-bold mb-3">{m.title}</h3>
        <p className="text-white/80 text-sm mb-5">{m.desc}</p>
        <button className="w-full py-2.5 bg-white/20 hover:bg-white/30 text-white text-sm font-semibold rounded-xl border border-white/30 transition">
          Configure
        </button>
      </div>
    ))}
  </div>
);

// ─── Transactions Page ────────────────────────────────────────────────────────
const TransactionsPage = () => {
  const [transactions, setTransactions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    transactionApi.getAll()
      .then(response => setTransactions(Array.isArray(response.data) ? response.data : []))
      .catch(() => setTransactions([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="p-6">
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h3 className="font-bold text-slate-900">Transaction Ledger</h3>
          <div className="flex gap-2">
            <select className="text-xs border border-slate-200 rounded-lg px-3 py-1.5 focus:outline-none">
              <option>All Types</option>
              <option>RTGS</option><option>NEFT</option><option>IMPS</option><option>SWIFT</option>
            </select>
            <button className="text-xs bg-blue-600 text-white px-3 py-1.5 rounded-lg font-medium hover:bg-blue-700 transition">
              Export
            </button>
          </div>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
              <tr>
                <th className="px-6 py-3 text-left">Reference</th>
                <th className="px-6 py-3 text-left">Type</th>
                <th className="px-6 py-3 text-left">Amount</th>
                <th className="px-6 py-3 text-left">Status</th>
                <th className="px-6 py-3 text-left">Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {transactions.map(t => (
                <tr key={t.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-6 py-4 font-mono text-xs text-slate-700">{t.transactionRefNo}</td>
                  <td className="px-6 py-4 font-medium">{t.transactionType}</td>
                  <td className="px-6 py-4 font-bold text-slate-900">{t.currency} {Number(t.amount).toLocaleString('en-IN')}</td>
                  <td className="px-6 py-4">
                    <span className={`text-xs font-bold px-2.5 py-1 rounded-full ${statusBadge[t.status] ?? 'bg-slate-100'}`}>
                      {t.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-slate-500">{t.createdAt ? new Date(t.createdAt).toLocaleDateString() : '-'}</td>
                </tr>
              ))}
              {!loading && transactions.length === 0 && <tr><td colSpan={5} className="px-6 py-10 text-center text-slate-400">No transactions have been submitted.</td></tr>}
              {loading && <tr><td colSpan={5} className="px-6 py-10 text-center text-slate-400">Loading transactions from backend...</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

// ─── LLM AI Page ─────────────────────────────────────────────────────────────
const LlmPage = () => {
  const [prompt, setPrompt] = useState('');
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);

  const analyze = async () => {
    if (!prompt.trim()) return;
    setLoading(true);
    setResult('');
    try {
      const res = await llmApi.analyze(prompt);
      setResult(res.data.response ?? JSON.stringify(res.data));
    } catch {
      setResult('⚠️  LLM service not available. Make sure Ollama is running at localhost:11434 with a model loaded (e.g. ollama run llama3).');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 max-w-3xl space-y-6">
      <div className="bg-white rounded-2xl border border-slate-200 p-6">
        <div className="flex items-center gap-3 mb-4">
          <div className="bg-violet-100 p-2 rounded-xl"><Bot size={22} className="text-violet-600" /></div>
          <div>
            <h3 className="font-bold text-slate-900">AI Transaction Analyst</h3>
            <p className="text-xs text-slate-500">Powered by local Ollama LLM via backend /api/llm/analyze</p>
          </div>
        </div>
        <textarea
          rows={4}
          value={prompt}
          onChange={e => setPrompt(e.target.value)}
          placeholder="Describe a transaction or pattern to analyze... e.g. 'Analyze a ₹15L SWIFT transfer to a new beneficiary in UAE with no prior history'"
          className="w-full border border-slate-200 rounded-xl p-4 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-violet-500"
        />
        <button onClick={analyze} disabled={loading || !prompt.trim()}
          className="mt-3 w-full py-2.5 bg-violet-600 hover:bg-violet-700 text-white font-semibold rounded-xl transition text-sm disabled:opacity-50">
          {loading ? '🤖 Analyzing…' : '🤖 Analyze with AI'}
        </button>
      </div>

      {result && (
        <div className="bg-slate-900 rounded-2xl p-6 text-green-400 text-sm font-mono whitespace-pre-wrap">
          {result}
        </div>
      )}

      <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 text-sm text-blue-800">
        <strong>💡 Example prompts:</strong>
        <ul className="mt-2 space-y-1 list-disc list-inside text-blue-700">
          <li>Analyze risk on a ₹50L bulk payment to 200 new beneficiaries</li>
          <li>Identify red flags in SWIFT transfers to high-risk jurisdictions</li>
          <li>Suggest AML rules for round-number transaction detection</li>
        </ul>
      </div>
    </div>
  );
};

// ─── Import llmApi ────────────────────────────────────────────────────────────
import { llmApi } from '../api';

// ─── Main App Layout ──────────────────────────────────────────────────────────
const Dashboard = () => {
  const [page, setPage] = useState<Page>('dashboard');
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const renderPage = () => {
    switch (page) {
      case 'dashboard':    return <DashboardPage />;
      case 'users':        return <UsersPage />;
      case 'transactions': return <TransactionsPage />;
      case 'approvals':    return <ApprovalsPage />;
      case 'payments':     return <PaymentsPage />;
      case 'collections':  return <CollectionsPage />;
      case 'liquidity':    return <LiquidityPage />;
      case 'llm':          return <LlmPage />;
      default:             return <DashboardPage />;
    }
  };

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden">
      <Sidebar page={page} setPage={setPage} open={sidebarOpen} setOpen={setSidebarOpen} />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Topbar page={page} setOpen={setSidebarOpen} />
        <main className="flex-1 overflow-y-auto">{renderPage()}</main>
      </div>
    </div>
  );
};

export default Dashboard;
