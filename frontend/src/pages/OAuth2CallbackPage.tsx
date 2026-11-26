import { AlertCircle, Loader2 } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function OAuth2CallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const processedRef = useRef(false);

  useEffect(() => {
    if (processedRef.current) {
      return;
    }
    processedRef.current = true;

    let redirectTimer: number | undefined;

    async function handleCallback() {
      const hashParams = new URLSearchParams(window.location.hash.replace(/^#/, ''));

      const errorParam =
        searchParams.get('error') ||
        hashParams.get('error') ||
        searchParams.get('error_description');

      if (errorParam) {
        setErrorMessage(
          `소셜 로그인에 실패했습니다. (${errorParam}) 로그인 페이지로 이동합니다.`,
        );
        redirectTimer = window.setTimeout(() => {
          navigate('/login', { replace: true });
        }, 2000);
        return;
      }

      const accessToken =
        searchParams.get('accessToken') || hashParams.get('accessToken');
      const refreshToken =
        searchParams.get('refreshToken') || hashParams.get('refreshToken');

      if (!accessToken || !refreshToken) {
        setErrorMessage(
          '인증 토큰이 전달되지 않았습니다. 로그인 페이지로 이동합니다.',
        );
        redirectTimer = window.setTimeout(() => {
          navigate('/login', { replace: true });
        }, 2000);
        return;
      }

      try {
        await login({ accessToken, refreshToken });
        navigate('/', { replace: true });
      } catch {
        setErrorMessage(
          '사용자 인증 정보를 확인하지 못했습니다. 다시 로그인해 주세요.',
        );
        redirectTimer = window.setTimeout(() => {
          navigate('/login', { replace: true });
        }, 2000);
      }
    }

    void handleCallback();

    return () => {
      if (redirectTimer !== undefined) {
        window.clearTimeout(redirectTimer);
      }
    };
  }, [login, navigate, searchParams]);

  if (errorMessage) {
    return (
      <div className="flex min-h-[55vh] items-center justify-center px-4">
        <section className="w-full max-w-md rounded-2xl border border-red-200 bg-white p-8 text-center shadow-sm">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-red-50 text-red-600">
            <AlertCircle className="h-6 w-6" />
          </div>
          <h1 className="mt-4 text-lg font-bold text-slate-900">로그인 오류</h1>
          <p className="mt-2 text-sm text-slate-600">{errorMessage}</p>
          <Link
            to="/login"
            className="mt-6 inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-medium text-white hover:bg-slate-800"
          >
            로그인 페이지로 이동
          </Link>
        </section>
      </div>
    );
  }

  return (
    <div className="flex min-h-[55vh] items-center justify-center px-4">
      <section className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-sm">
        <Loader2 className="mx-auto h-8 w-8 animate-spin text-indigo-600" />
        <h1 className="mt-4 text-lg font-bold text-slate-900">
          로그인 처리 중...
        </h1>
        <p className="mt-2 text-sm text-slate-600">
          소셜 로그인 인증 정보를 확인하고 있습니다.
        </p>
      </section>
    </div>
  );
}
