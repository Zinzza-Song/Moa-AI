import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Bookmark,
  ChevronLeft,
  ChevronRight,
  FileText,
  Heart,
  PlusCircle,
  Sparkles,
  User,
} from 'lucide-react';
import {
  getMyBookmarkedPrompts,
  getMyLikedPrompts,
  getMyPrompts,
  getMySummary,
} from '../api/auth';
import { togglePromptBookmark, togglePromptLike } from '../api/prompt';
import { PromptCard } from '../components/prompt/PromptCard';
import { extractApiError } from '../constants/promptOptions';
import { useAuth } from '../context/AuthContext';
import type { PageResponse } from '../types/api';
import type { UserSummaryResponse } from '../types/auth';
import type { PromptResponse } from '../types/prompt';

type MyPageTab = 'prompts' | 'likes' | 'bookmarks';

const TAB_ITEMS: { key: MyPageTab; label: string; emptyText: string }[] = [
  {
    key: 'prompts',
    label: '내 프롬프트',
    emptyText: '아직 작성한 프롬프트가 없습니다.',
  },
  {
    key: 'likes',
    label: '좋아요',
    emptyText: '좋아요한 프롬프트가 없습니다.',
  },
  {
    key: 'bookmarks',
    label: '북마크',
    emptyText: '북마크한 프롬프트가 없습니다.',
  },
];

export function MyPage() {
  const { user } = useAuth();

  const [summary, setSummary] = useState<UserSummaryResponse | null>(null);
  const [activeTab, setActiveTab] = useState<MyPageTab>('prompts');
  const [page, setPage] = useState<number>(0);

  const [promptsPage, setPromptsPage] =
    useState<PageResponse<PromptResponse> | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [pendingLikes, setPendingLikes] = useState<Record<number, boolean>>({});
  const [pendingBookmarks, setPendingBookmarks] = useState<
    Record<number, boolean>
  >({});

  const fetchSummary = useCallback(async () => {
    try {
      const data = await getMySummary();
      setSummary(data);
    } catch {
      // 요약 정보 조회 실패 시 기본값 유지
    }
  }, []);

  const fetchTabPrompts = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data =
        activeTab === 'prompts'
          ? await getMyPrompts(page, 12)
          : activeTab === 'likes'
            ? await getMyLikedPrompts(page, 12)
            : await getMyBookmarkedPrompts(page, 12);
      setPromptsPage(data);
    } catch (error) {
      const { message } = extractApiError(
        error,
        '목록을 불러오지 못했습니다.',
      );
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  }, [activeTab, page]);

  useEffect(() => {
    void fetchSummary();
  }, [fetchSummary]);

  useEffect(() => {
    void fetchTabPrompts();
  }, [fetchTabPrompts]);

  const handleTabChange = (tab: MyPageTab) => {
    if (tab === activeTab) return;
    setActiveTab(tab);
    setPage(0);
  };

  const handleToggleLike = async (promptId: number) => {
    if (pendingLikes[promptId]) return;
    setPendingLikes((prev) => ({ ...prev, [promptId]: true }));
    try {
      const res = await togglePromptLike(promptId);
      setSummary((prev) =>
        prev
          ? {
              ...prev,
              likedPromptCount: Math.max(
                0,
                prev.likedPromptCount + (res.active ? 1 : -1),
              ),
            }
          : prev,
      );
      setPromptsPage((prev) => {
        if (!prev) return prev;
        if (activeTab === 'likes' && !res.active) {
          return {
            ...prev,
            totalElements: Math.max(0, prev.totalElements - 1),
            content: prev.content.filter((item) => item.id !== promptId),
          };
        }
        return {
          ...prev,
          content: prev.content.map((item) =>
            item.id === promptId
              ? { ...item, liked: res.active, likeCount: res.count }
              : item,
          ),
        };
      });
    } catch {
      // 토글 실패 시 기존 상태 유지
    } finally {
      setPendingLikes((prev) => ({ ...prev, [promptId]: false }));
    }
  };

  const handleToggleBookmark = async (promptId: number) => {
    if (pendingBookmarks[promptId]) return;
    setPendingBookmarks((prev) => ({ ...prev, [promptId]: true }));
    try {
      const res = await togglePromptBookmark(promptId);
      setSummary((prev) =>
        prev
          ? {
              ...prev,
              bookmarkedPromptCount: Math.max(
                0,
                prev.bookmarkedPromptCount + (res.active ? 1 : -1),
              ),
            }
          : prev,
      );
      setPromptsPage((prev) => {
        if (!prev) return prev;
        if (activeTab === 'bookmarks' && !res.active) {
          return {
            ...prev,
            totalElements: Math.max(0, prev.totalElements - 1),
            content: prev.content.filter((item) => item.id !== promptId),
          };
        }
        return {
          ...prev,
          content: prev.content.map((item) =>
            item.id === promptId
              ? { ...item, bookmarked: res.active, bookmarkCount: res.count }
              : item,
          ),
        };
      });
    } catch {
      // 토글 실패 시 기존 상태 유지
    } finally {
      setPendingBookmarks((prev) => ({ ...prev, [promptId]: false }));
    }
  };

  const prompts = promptsPage?.content ?? [];
  const totalPages = promptsPage?.totalPages ?? 0;
  const activeTabInfo = TAB_ITEMS.find((t) => t.key === activeTab)!;

  return (
    <div className="space-y-6">
      {/* 상단 프로필 & 활동 요약 카드 */}
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs sm:p-8">
        <div className="flex flex-col justify-between gap-6 md:flex-row md:items-center">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
              <User className="h-7 w-7" />
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h1 className="text-xl font-bold text-slate-900 sm:text-2xl">
                  {user?.nickname ?? '마이페이지'}
                </h1>
                {user?.provider && (
                  <span className="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-600">
                    {user.provider}
                  </span>
                )}
                {user?.role === 'ADMIN' && (
                  <span className="rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-semibold text-indigo-700">
                    ADMIN
                  </span>
                )}
              </div>
              {user?.email && (
                <p className="mt-1 text-sm text-slate-500">{user.email}</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-3 gap-3 sm:gap-4">
            <div className="rounded-xl border border-slate-100 bg-slate-50 px-4 py-3 text-center">
              <div className="flex items-center justify-center gap-1 text-xs font-medium text-slate-500">
                <FileText className="h-3.5 w-3.5 text-indigo-500" />
                <span>작성</span>
              </div>
              <p className="mt-1 text-lg font-bold text-slate-900">
                {summary ? summary.writtenPromptCount : '-'}
              </p>
            </div>

            <div className="rounded-xl border border-slate-100 bg-slate-50 px-4 py-3 text-center">
              <div className="flex items-center justify-center gap-1 text-xs font-medium text-slate-500">
                <Heart className="h-3.5 w-3.5 text-rose-500" />
                <span>좋아요</span>
              </div>
              <p className="mt-1 text-lg font-bold text-slate-900">
                {summary ? summary.likedPromptCount : '-'}
              </p>
            </div>

            <div className="rounded-xl border border-slate-100 bg-slate-50 px-4 py-3 text-center">
              <div className="flex items-center justify-center gap-1 text-xs font-medium text-slate-500">
                <Bookmark className="h-3.5 w-3.5 text-amber-500" />
                <span>북마크</span>
              </div>
              <p className="mt-1 text-lg font-bold text-slate-900">
                {summary ? summary.bookmarkedPromptCount : '-'}
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* 탭 메뉴 */}
      <div className="flex items-center justify-between border-b border-slate-200">
        <div className="flex gap-1" role="tablist" aria-label="마이페이지 탭">
          {TAB_ITEMS.map((tab) => {
            const isActive = activeTab === tab.key;
            return (
              <button
                key={tab.key}
                type="button"
                role="tab"
                aria-selected={isActive}
                onClick={() => handleTabChange(tab.key)}
                className={`cursor-pointer border-b-2 px-4 py-3 text-sm font-semibold transition-colors ${
                  isActive
                    ? 'border-indigo-600 text-indigo-600'
                    : 'border-transparent text-slate-500 hover:text-slate-800'
                }`}
              >
                {tab.label}
              </button>
            );
          })}
        </div>

        {!isLoading && !errorMessage && promptsPage && (
          <span className="hidden text-xs text-slate-500 sm:inline">
            총{' '}
            <strong className="font-semibold text-slate-800">
              {promptsPage.totalElements}
            </strong>
            개
          </span>
        )}
      </div>

      {/* 목록 영역 */}
      {errorMessage ? (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center">
          <p className="text-sm font-medium text-red-700">{errorMessage}</p>
          <button
            type="button"
            onClick={() => void fetchTabPrompts()}
            className="mt-4 cursor-pointer rounded-xl bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
          >
            다시 시도
          </button>
        </div>
      ) : isLoading ? (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <div
              key={index}
              className="flex h-56 animate-pulse flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5"
            >
              <div>
                <div className="flex gap-2">
                  <div className="h-5 w-16 rounded-full bg-slate-200" />
                  <div className="h-5 w-16 rounded-full bg-slate-200" />
                </div>
                <div className="mt-4 h-5 w-3/4 rounded bg-slate-200" />
                <div className="mt-3 space-y-2">
                  <div className="h-3.5 w-full rounded bg-slate-100" />
                  <div className="h-3.5 w-5/6 rounded bg-slate-100" />
                  <div className="h-3.5 w-2/3 rounded bg-slate-100" />
                </div>
              </div>
              <div className="flex items-center justify-between border-t border-slate-100 pt-3">
                <div className="h-3.5 w-20 rounded bg-slate-200" />
                <div className="h-3.5 w-24 rounded bg-slate-200" />
              </div>
            </div>
          ))}
        </div>
      ) : prompts.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center">
          <Sparkles className="mx-auto h-10 w-10 text-slate-300" />
          <h2 className="mt-3 text-base font-semibold text-slate-900">
            {activeTabInfo.emptyText}
          </h2>
          <p className="mt-1 text-sm text-slate-500">
            {activeTab === 'prompts'
              ? '첫 번째 프롬프트를 등록해 커뮤니티와 공유해 보세요.'
              : '마음에 드는 프롬프트를 탐색하고 표시해 보세요.'}
          </p>
          <div className="mt-5 flex justify-center">
            {activeTab === 'prompts' ? (
              <Link
                to="/prompts/new"
                className="inline-flex items-center gap-1.5 rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
              >
                <PlusCircle className="h-4 w-4" />
                <span>프롬프트 등록하기</span>
              </Link>
            ) : (
              <Link
                to="/"
                className="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                프롬프트 둘러보기
              </Link>
            )}
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {prompts.map((prompt) => (
            <PromptCard
              key={prompt.id}
              prompt={prompt}
              onToggleLike={(id) => void handleToggleLike(id)}
              onToggleBookmark={(id) => void handleToggleBookmark(id)}
              isLikePending={Boolean(pendingLikes[prompt.id])}
              isBookmarkPending={Boolean(pendingBookmarks[prompt.id])}
            />
          ))}
        </div>
      )}

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <nav
          aria-label="페이지네이션"
          className="flex items-center justify-center gap-1.5 pt-2"
        >
          <button
            type="button"
            disabled={page === 0}
            onClick={() => setPage((prev) => Math.max(0, prev - 1))}
            className="inline-flex cursor-pointer items-center rounded-lg border border-slate-200 bg-white p-2 text-slate-600 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
            aria-label="이전 페이지"
          >
            <ChevronLeft className="h-4 w-4" />
          </button>

          {Array.from({ length: totalPages }).map((_, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => setPage(idx)}
              className={`h-9 min-w-9 cursor-pointer rounded-lg px-3 text-sm font-medium transition-colors ${
                page === idx
                  ? 'bg-indigo-600 text-white'
                  : 'border border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
              }`}
            >
              {idx + 1}
            </button>
          ))}

          <button
            type="button"
            disabled={promptsPage?.last ?? page >= totalPages - 1}
            onClick={() => setPage((prev) => prev + 1)}
            className="inline-flex cursor-pointer items-center rounded-lg border border-slate-200 bg-white p-2 text-slate-600 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
            aria-label="다음 페이지"
          >
            <ChevronRight className="h-4 w-4" />
          </button>
        </nav>
      )}
    </div>
  );
}
