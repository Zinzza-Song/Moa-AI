import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  Bookmark,
  Check,
  Copy,
  Edit3,
  Heart,
  Loader2,
  Trash2,
} from 'lucide-react';
import {
  deletePrompt,
  getPromptDetail,
  increasePromptCopyCount,
  togglePromptBookmark,
  togglePromptLike,
} from '../api/prompt';
import {
  AI_MODEL_LABEL_MAP,
  CATEGORY_LABEL_MAP,
  extractApiError,
  formatDateTime,
} from '../constants/promptOptions';
import { useAuth } from '../context/AuthContext';
import type { PromptResponse } from '../types/prompt';

export function PromptDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  const [prompt, setPrompt] = useState<PromptResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [copied, setCopied] = useState<boolean>(false);
  const [isTogglingLike, setIsTogglingLike] = useState<boolean>(false);
  const [isTogglingBookmark, setIsTogglingBookmark] = useState<boolean>(false);
  const [showDeleteModal, setShowDeleteModal] = useState<boolean>(false);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const promptId = Number(id);

  const fetchDetail = useCallback(async () => {
    if (!id || Number.isNaN(promptId)) {
      setErrorMessage('올바르지 않은 프롬프트 경로입니다.');
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await getPromptDetail(promptId);
      setPrompt(data);
    } catch (error) {
      const { message } = extractApiError(
        error,
        '프롬프트 상세 정보를 불러오지 못했습니다.',
      );
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  }, [id, promptId]);

  useEffect(() => {
    void fetchDetail();
  }, [fetchDetail]);

  const handleCopy = async () => {
    if (!prompt) return;
    setActionError(null);
    try {
      await navigator.clipboard.writeText(prompt.content);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);

      const newCopyCount = await increasePromptCopyCount(prompt.id);
      setPrompt((prev) =>
        prev ? { ...prev, copyCount: newCopyCount } : prev,
      );
    } catch {
      setActionError('클립보드 복사에 실패했습니다.');
    }
  };

  const handleToggleLike = async () => {
    if (!prompt) return;
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (isTogglingLike) return;

    setIsTogglingLike(true);
    setActionError(null);
    try {
      const result = await togglePromptLike(prompt.id);
      setPrompt((prev) =>
        prev
          ? { ...prev, liked: result.active, likeCount: result.count }
          : prev,
      );
    } catch (error) {
      const { message } = extractApiError(error, '좋아요 처리에 실패했습니다.');
      setActionError(message);
    } finally {
      setIsTogglingLike(false);
    }
  };

  const handleToggleBookmark = async () => {
    if (!prompt) return;
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (isTogglingBookmark) return;

    setIsTogglingBookmark(true);
    setActionError(null);
    try {
      const result = await togglePromptBookmark(prompt.id);
      setPrompt((prev) =>
        prev
          ? { ...prev, bookmarked: result.active, bookmarkCount: result.count }
          : prev,
      );
    } catch (error) {
      const { message } = extractApiError(error, '북마크 처리에 실패했습니다.');
      setActionError(message);
    } finally {
      setIsTogglingBookmark(false);
    }
  };

  const handleDelete = async () => {
    if (!prompt || isDeleting) return;
    setIsDeleting(true);
    setActionError(null);
    try {
      await deletePrompt(prompt.id);
      navigate('/', { replace: true });
    } catch (error) {
      const { message } = extractApiError(error, '프롬프트 삭제에 실패했습니다.');
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
        <p className="text-sm">프롬프트를 불러오는 중입니다...</p>
      </div>
    );
  }

  if (errorMessage || !prompt) {
    return (
      <div className="rounded-2xl border border-red-200 bg-white p-8 text-center shadow-xs">
        <p className="text-sm font-medium text-red-600">
          {errorMessage || '프롬프트를 찾을 수 없습니다.'}
        </p>
        <Link
          to="/"
          className="mt-4 inline-flex items-center gap-1.5 rounded-xl bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>목록으로 돌아가기</span>
        </Link>
      </div>
    );
  }

  const isOwner = isAuthenticated && user?.id === prompt.authorId;

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="flex items-center justify-between">
        <Link
          to="/"
          className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-900"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>프롬프트 목록으로</span>
        </Link>

        {isOwner && (
          <div className="flex items-center gap-2">
            <Link
              to={`/prompts/${prompt.id}/edit`}
              className="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              <Edit3 className="h-4 w-4" />
              <span>수정</span>
            </Link>
            <button
              type="button"
              onClick={() => setShowDeleteModal(true)}
              className="inline-flex cursor-pointer items-center gap-1.5 rounded-xl border border-red-200 bg-white px-3.5 py-2 text-sm font-medium text-red-600 hover:bg-red-50"
            >
              <Trash2 className="h-4 w-4" />
              <span>삭제</span>
            </button>
          </div>
        )}
      </div>

      <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs sm:p-8">
        {/* 배지 & 제목 */}
        <div className="flex flex-wrap items-center gap-2">
          <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-indigo-700">
            {CATEGORY_LABEL_MAP[prompt.category] ?? prompt.category}
          </span>
          <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700">
            {AI_MODEL_LABEL_MAP[prompt.aiModel] ?? prompt.aiModel}
          </span>
        </div>

        <h1 className="mt-4 text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
          {prompt.title}
        </h1>

        <div className="mt-3 flex flex-wrap items-center gap-3 text-sm text-slate-500">
          <span className="font-medium text-slate-800">
            {prompt.authorNickname}
          </span>
          <span>·</span>
          <span>작성일 {formatDateTime(prompt.createdAt)}</span>
          {prompt.updatedAt && prompt.updatedAt !== prompt.createdAt && (
            <>
              <span>·</span>
              <span>수정일 {formatDateTime(prompt.updatedAt)}</span>
            </>
          )}
        </div>

        {actionError && (
          <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {actionError}
          </div>
        )}

        {/* 프롬프트 본문 */}
        <div className="mt-6">
          <div className="flex items-center justify-between rounded-t-xl border border-b-0 border-slate-200 bg-slate-100 px-4 py-2.5">
            <span className="text-xs font-semibold tracking-wide text-slate-600 uppercase">
              Prompt
            </span>
            <button
              type="button"
              onClick={() => void handleCopy()}
              className="inline-flex cursor-pointer items-center gap-1.5 rounded-lg bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 shadow-2xs transition-colors hover:bg-indigo-50 hover:text-indigo-700"
            >
              {copied ? (
                <>
                  <Check className="h-3.5 w-3.5 text-emerald-600" />
                  <span className="text-emerald-600">복사됨</span>
                </>
              ) : (
                <>
                  <Copy className="h-3.5 w-3.5" />
                  <span>프롬프트 복사</span>
                </>
              )}
            </button>
          </div>
          <pre className="overflow-x-auto rounded-b-xl border border-slate-200 bg-slate-50 p-5 font-mono text-sm leading-relaxed whitespace-pre-wrap text-slate-800">
            {prompt.content}
          </pre>
        </div>

        {/* 실행 결과 예시 */}
        {prompt.resultExample && (
          <div className="mt-6">
            <h2 className="text-sm font-semibold text-slate-800">
              실행 결과 예시
            </h2>
            <div className="mt-2 rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm leading-relaxed whitespace-pre-wrap text-slate-700">
              {prompt.resultExample}
            </div>
          </div>
        )}

        {/* 상호작용 버튼 바 */}
        <div className="mt-8 flex flex-wrap items-center gap-3 border-t border-slate-100 pt-6">
          <button
            type="button"
            disabled={isTogglingLike}
            onClick={() => void handleToggleLike()}
            className={`inline-flex cursor-pointer items-center gap-2 rounded-xl border px-4 py-2.5 text-sm font-medium transition-colors disabled:opacity-60 ${
              prompt.liked
                ? 'border-rose-200 bg-rose-50 text-rose-600'
                : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
            }`}
          >
            <Heart
              className={`h-4 w-4 ${prompt.liked ? 'fill-rose-500 text-rose-500' : ''}`}
            />
            <span>좋아요</span>
            <span className="font-semibold">{prompt.likeCount}</span>
          </button>

          <button
            type="button"
            disabled={isTogglingBookmark}
            onClick={() => void handleToggleBookmark()}
            className={`inline-flex cursor-pointer items-center gap-2 rounded-xl border px-4 py-2.5 text-sm font-medium transition-colors disabled:opacity-60 ${
              prompt.bookmarked
                ? 'border-amber-200 bg-amber-50 text-amber-700'
                : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
            }`}
          >
            <Bookmark
              className={`h-4 w-4 ${
                prompt.bookmarked ? 'fill-amber-500 text-amber-500' : ''
              }`}
            />
            <span>북마크</span>
            <span className="font-semibold">{prompt.bookmarkCount}</span>
          </button>

          <div className="ml-auto inline-flex items-center gap-1.5 text-sm text-slate-500">
            <Copy className="h-4 w-4" />
            <span>누적 복사 {prompt.copyCount}회</span>
          </div>
        </div>
      </article>

      {/* 삭제 확인 모달 */}
      {showDeleteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 px-4">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="text-lg font-bold text-slate-900">
              프롬프트를 삭제할까요?
            </h2>
            <p className="mt-2 text-sm text-slate-600">
              삭제한 프롬프트는 복구할 수 없습니다.
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
