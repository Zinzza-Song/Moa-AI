import { Sparkles } from 'lucide-react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

type OAuth2Provider = 'google' | 'kakao' | 'naver';

const BACKEND_OAUTH2_BASE_URL = 'http://localhost:8080/oauth2/authorization';

export function LoginPage() {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();
  const fromPath =
    (location.state as { from?: { pathname?: string } } | null)?.from
      ?.pathname || '/';

  if (!isLoading && isAuthenticated) {
    return <Navigate to={fromPath} replace />;
  }

  const handleSocialLogin = (provider: OAuth2Provider) => {
    window.location.href = `${BACKEND_OAUTH2_BASE_URL}/${provider}`;
  };

  return (
    <div className="flex min-h-[65vh] items-center justify-center px-4">
      <section className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="flex flex-col items-center text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-600 text-white shadow-sm">
            <Sparkles className="h-6 w-6" />
          </div>
          <h1 className="mt-4 text-2xl font-bold tracking-tight text-slate-900">
            MoaAI 로그인
          </h1>
          <p className="mt-2 text-sm text-slate-600">
            소셜 계정으로 간편하게 로그인하고 AI 프롬프트를 공유·개선해 보세요.
          </p>
        </div>

        <div className="mt-8 flex flex-col gap-3">
          <button
            type="button"
            onClick={() => handleSocialLogin('google')}
            className="flex w-full cursor-pointer items-center justify-center gap-3 rounded-xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-800 transition-colors hover:bg-slate-50"
          >
            <svg className="h-5 w-5" viewBox="0 0 24 24" aria-hidden="true">
              <path
                fill="#4285F4"
                d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.665-5.17 3.665-9.17Z"
              />
              <path
                fill="#34A853"
                d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.11-6.72-4.96H1.29v3.14C3.26 21.3 7.31 24 12 24Z"
              />
              <path
                fill="#FBBC05"
                d="M5.28 14.24c-.24-.72-.38-1.49-.38-2.24s.14-1.52.38-2.24V6.62H1.29C.47 8.24 0 10.06 0 12s.47 3.76 1.29 5.38l3.99-3.14Z"
              />
              <path
                fill="#EA4335"
                d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.26 2.7 1.29 6.62l3.99 3.14c.95-2.85 3.6-4.96 6.72-4.96Z"
              />
            </svg>
            <span>Google로 계속하기</span>
          </button>

          <button
            type="button"
            onClick={() => handleSocialLogin('kakao')}
            className="flex w-full cursor-pointer items-center justify-center gap-3 rounded-xl bg-[#FEE500] px-4 py-3 text-sm font-semibold text-[#191919] transition-colors hover:bg-[#F5DC00]"
          >
            <svg className="h-5 w-5" viewBox="0 0 24 24" aria-hidden="true">
              <path
                fill="#191919"
                d="M12 3C6.477 3 2 6.463 2 10.736c0 2.747 1.816 5.158 4.545 6.535-.199.742-.723 2.688-.828 3.104-.13.516.19.509.399.37.164-.109 2.616-1.778 3.672-2.497.721.104 1.462.159 2.212.159 5.523 0 10-3.463 10-7.671C22 6.463 17.523 3 12 3Z"
              />
            </svg>
            <span>카카오로 계속하기</span>
          </button>

          <button
            type="button"
            onClick={() => handleSocialLogin('naver')}
            className="flex w-full cursor-pointer items-center justify-center gap-3 rounded-xl bg-[#03C75A] px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-[#02B351]"
          >
            <svg className="h-4 w-4" viewBox="0 0 24 24" aria-hidden="true">
              <path
                fill="currentColor"
                d="M16.273 12.845 7.376 0H0v24h7.727V11.155L16.624 24H24V0h-7.727v12.845Z"
              />
            </svg>
            <span>네이버로 계속하기</span>
          </button>
        </div>

        <p className="mt-6 text-center text-xs leading-relaxed text-slate-400">
          로그인 시 MoaAI의 서비스 이용약관 및 개인정보 처리방침에 동의하게 됩니다.
        </p>
      </section>
    </div>
  );
}
