import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  Calendar,
  ExternalLink,
  Loader2,
  Trash2,
} from 'lucide-react';
import { deleteAdminNews, getNewsDetail } from '../api/news';
import { extractApiError, formatDateTime } from '../constants/promptOptions';
import { useAuth } from '../context/AuthContext';
import type { AiNewsResponse } from '../types/news';

export function NewsDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  const [news, setNews] = useState<AiNewsResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState<boolean>(false);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const newsId = Number(id);
  const isAdmin = isAuthenticated && user?.role === 'ADMIN';

  const fetchDetail = useCallback(async () => {
    if (!id || Number.isNaN(newsId)) {
      setErrorMessage('올바르지 않은 뉴스 경로입니다.');
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await getNewsDetail(newsId);
      setNews(data);
    } catch (error) {
      const { message } = extractApiError(
        error,
        '뉴스 상세 정보를 불러오지 못했습니다.',
      );
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  }, [id, newsId]);

  useEffect(() => {
    void fetchDetail();
  }, [fetchDetail]);

  const handleDelete = async () => {
    if (!news || isDeleting) return;
    setIsDeleting(true);
    setActionError(null);
    try {
      await deleteAdminNews(news.id);
      navigate('/news', { replace: true });
    } catch (error) {
      const { message } = extractApiError(error, '뉴스 삭제에 실패했습니다.');
      setActionError(message);
      setShowDeleteModal(false);
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex min-h-[45vh] flex-col items-center justify-center gap-3 text-slate-500">
        <Loader2 className="h-7 w-7 animate-spin text-indigo-600" />
        <p className="text-sm">뉴스를 불러오는 중입니다...</p>
      </div>
    );
  }

  if (errorMessage || !news) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-red-200 bg-white p-8 text-center shadow-xs">
        <p className="text-sm font-medium text-red-600">
          {errorMessage || '뉴스를 찾을 수 없습니다.'}
        </p>
        <Link
          to="/news"
          className="mt-4 inline-flex items-center gap-1.5 rounded-xl bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>뉴스 목록으로</span>
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center justify-between">
        <Link
          to="/news"
          className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-900"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>AI 뉴스 목록으로</span>
        </Link>

        {isAdmin && (
          <button
            type="button"
            onClick={() => setShowDeleteModal(true)}
            className="inline-flex cursor-pointer items-center gap-1.5 rounded-xl border border-red-200 bg-white px-3.5 py-2 text-sm font-medium text-red-600 hover:bg-red-50"
          >
            <Trash2 className="h-4 w-4" />
            <span>삭제</span>
          </button>
        )}
      </div>

      <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs sm:p-8">
        <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500">
          <span className="rounded-md bg-indigo-50 px-2.5 py-1 font-semibold text-indigo-700">
            {news.sourceName}
          </span>
          <span className="inline-flex items-center gap-1">
            <Calendar className="h-3.5 w-3.5" />
            발행일 {formatDateTime(news.publishedAt || news.createdAt)}
          </span>
        </div>

        <h1 className="mt-4 text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
          {news.title}
        </h1>

        {news.tags.length > 0 && (
          <div className="mt-4 flex flex-wrap gap-1.5">
            {news.tags.map((tag) => (
              <Link
                key={tag}
                to={`/news?tag=${encodeURIComponent(tag)}`}
                className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600 transition-colors hover:bg-indigo-50 hover:text-indigo-700"
              >
                #{tag}
              </Link>
            ))}
          </div>
        )}

        {actionError && (
          <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {actionError}
          </div>
        )}

        <div className="mt-6 rounded-xl border border-slate-200 bg-slate-50 p-5 text-base leading-relaxed whitespace-pre-wrap text-slate-800">
          {news.summary}
        </div>

        {news.originalUrl && (
          <div className="mt-6 flex justify-end border-t border-slate-100 pt-6">
            <a
              href={news.originalUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-indigo-700"
            >
              <span>원문 보러가기</span>
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        )}
      </article>

      {showDeleteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 px-4">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="text-lg font-bold text-slate-900">
              뉴스를 삭제할까요?
            </h2>
            <p className="mt-2 text-sm text-slate-600">
              삭제한 뉴스는 복구할 수 없습니다.
            </p>
            <div className="mt-6 flex justify-end gap-2">
              <button
                type="button"
                disabled={isDeleting}
                onClick={() => setShowDeleteModal(false)}
                className="cursor-pointer rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                취소
              </button>
              <button
                type="button"
                disabled={isDeleting}
                onClick={() => void handleDelete()}
                className="inline-flex cursor-pointer items-center gap-1.5 rounded-xl bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-60"
              >
                {isDeleting && <Loader2 className="h-4 w-4 animate-spin" />}
                <span>삭제하기</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
