import { useState } from 'react';
import { Link, NavLink } from 'react-router-dom';
import {
  LogIn,
  Menu,
  Newspaper,
  PlusCircle,
  Sparkles,
  Wand2,
  X,
} from 'lucide-react';

const navItems = [
  { to: '/', label: '프롬프트', icon: Sparkles, end: true },
  { to: '/ai/refine', label: 'AI 개선', icon: Wand2, end: false },
  { to: '/news', label: 'AI 뉴스', icon: Newspaper, end: false },
];

export function Header() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `inline-flex items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
      isActive
        ? 'bg-indigo-50 text-indigo-600'
        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
    }`;

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <div className="flex items-center gap-8">
          <Link
            to="/"
            className="flex items-center gap-2 text-lg font-bold tracking-tight text-slate-900"
          >
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-600 text-white">
              <Sparkles className="h-4 w-4" />
            </span>
            <span>MoaAI</span>
          </Link>

          <nav className="hidden items-center gap-1 md:flex">
            {navItems.map(({ to, label, icon: Icon, end }) => (
              <NavLink key={to} to={to} end={end} className={navLinkClass}>
                <Icon className="h-4 w-4" />
                <span>{label}</span>
              </NavLink>
            ))}
          </nav>
        </div>

        <div className="hidden items-center gap-3 md:flex">
          <Link
            to="/prompts/new"
            className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-600 px-3.5 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-700"
          >
            <PlusCircle className="h-4 w-4" />
            <span>프롬프트 등록</span>
          </Link>
          <Link
            to="/login"
            className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3.5 py-2 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50"
          >
            <LogIn className="h-4 w-4" />
            <span>로그인</span>
          </Link>
        </div>

        <button
          type="button"
          onClick={() => setMobileMenuOpen((prev) => !prev)}
          className="inline-flex items-center justify-center rounded-lg p-2 text-slate-600 hover:bg-slate-100 hover:text-slate-900 md:hidden"
          aria-label="메뉴 열기"
        >
          {mobileMenuOpen ? (
            <X className="h-5 w-5" />
          ) : (
            <Menu className="h-5 w-5" />
          )}
        </button>
      </div>

      {mobileMenuOpen && (
        <div className="border-t border-slate-200 bg-white px-4 py-3 md:hidden">
          <nav className="flex flex-col gap-1">
            {navItems.map(({ to, label, icon: Icon, end }) => (
              <NavLink
                key={to}
                to={to}
                end={end}
                onClick={() => setMobileMenuOpen(false)}
                className={navLinkClass}
              >
                <Icon className="h-4 w-4" />
                <span>{label}</span>
              </NavLink>
            ))}
            <div className="mt-2 flex flex-col gap-2 border-t border-slate-100 pt-3">
              <Link
                to="/prompts/new"
                onClick={() => setMobileMenuOpen(false)}
                className="inline-flex items-center justify-center gap-1.5 rounded-lg bg-indigo-600 px-3.5 py-2 text-sm font-medium text-white hover:bg-indigo-700"
              >
                <PlusCircle className="h-4 w-4" />
                <span>프롬프트 등록</span>
              </Link>
              <Link
                to="/login"
                onClick={() => setMobileMenuOpen(false)}
                className="inline-flex items-center justify-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3.5 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                <LogIn className="h-4 w-4" />
                <span>로그인</span>
              </Link>
            </div>
          </nav>
        </div>
      )}
    </header>
  );
}
