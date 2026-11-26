import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import {
  Bookmark,
  ChevronLeft,
  ChevronRight,
  Copy,
  Heart,
  PlusCircle,
  Search,
  Sparkles,
  Wand2,
} from 'lucide-react';
import { getPrompts } from '../api/prompt';
import {
  AI_MODELS,
  AI_MODEL_LABEL_MAP,
  CATEGORY_LABEL_MAP,
  PROMPT_CATEGORIES,
  PROMPT_SORT_OPTIONS,
  extractApiError,
  formatDateTime,
} from '../constants/promptOptions';
import type { PageResponse } from '../types/api';
import type {
  AiModel,
  PromptCategory,
  PromptResponse,
  PromptSortBy,
} from '../types/prompt';

export function HomePage() {
  const [promptsPage, setPromptsPage] =
    useState<PageResponse<PromptResponse> | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [searchInput, setSearchInput] = useState<string>('');
  const [keyword, setKeyword] = useState<string>('');
  const [category, setCategory] = useState<PromptCategory | ''>('');
  const [aiModel, setAiModel] = useState<AiModel | ''>('');
  const [sortBy, setSortBy] = useState<PromptSortBy>('createdAt');
  const [page, setPage] = useState<number>(0);

  const fetchPrompts = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await getPrompts({
        category: category || undefined,
        aiModel: aiModel || undefined,
        keyword: keyword || undefined,
        sortBy,
        page,
        size: 12,
      });
      setPromptsPage(data);
    } catch (error) {
      const { message } = extractApiError(
        error,
        '프롬프트 목록을 불러오지 못했습니다.',
      );
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  }, [category, aiModel, keyword, sortBy, page]);

  useEffect(() => {
    void fetchPrompts();
  }, [fetchPrompts]);

  const handleSearchSubmit = (e: FormEvent) => {
    e.preventDefault();
    setPage(0);
    setKeyword(searchInput.trim());
  };

  const handleResetFilters = () => {
    setSearchInput('');
    setKeyword('');
    setCategory('');
    setAiModel('');
    setSortBy('createdAt');
    setPage(0);
  };

  const hasActiveFilters = Boolean(keyword || category || aiModel);
  const prompts = promptsPage?.content ?? [];
  const totalPages = promptsPage?.totalPages ?? 0;

  return (
    <div className="space-y-6">
      {/* 상단 배너 */}
      <section className="rounded-2xl border border-indigo-100 bg-gradient-to-br from-indigo-50 via-white to-violet-50 p-6 sm:p-8">
        <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
          <div>
            <div className="inline-flex items-center gap-1.5 rounded-full bg-indigo-100 px-3 py-1 text-xs font-semibold text-indigo-700">
              <Sparkles className="h-3.5 w-3.5" />
              <span>AI 프롬프트 공유 허브</span>
            </div>
            <h1 className="mt-3 text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
              검증된 AI 프롬프트를 탐색하고 공유하세요
            </h1>
            <p className="mt-1.5 text-sm text-slate-600">
              모델·카테고리별 인기 프롬프트를 바로 복사하거나, Gemini AI로 나만의 프롬프트를 고도화할 수 있습니다.
            </p>
          </div>
          <div className="flex shrink-0 flex-wrap gap-2.5">
            <Link
              to="/ai/refine"
              className="inline-flex items-center gap-1.5 rounded-xl border border-indigo-200 bg-white px-4 py-2.5 text-sm font-semibold text-indigo-700 transition-colors hover:bg-indigo-50"
            >
              <Wand2 className="h-4 w-4" />
              <span>AI 자동 개선</span>
            </Link>
            <Link
              to="/prompts/new"
              className="inline-flex items-center gap-1.5 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-indigo-700"
            >
              <PlusCircle className="h-4 w-4" />
              <span>프롬프트 등록</span>
            </Link>
          </div>
        </div>
      </section>

      {/* 검색 및 필터 바 */}
      <section className="space-y-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-xs sm:p-5">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-center">
          <form onSubmit={handleSearchSubmit} className="flex flex-1 gap-2">
            <div className="relative flex-1">
              <Search className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="프롬프트 제목이나 내용을 검색해 보세요..."
                className="w-full rounded-xl border border-slate-200 bg-slate-50 py-2.5 pr-4 pl-10 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:bg-white focus:outline-none"
              />
            </div>
            <button
              type="submit"
              className="cursor-pointer rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-slate-800"
            >
              검색
            </button>
          </form>

          <div className="flex flex-wrap items-center gap-2.5">
            <select
              value={aiModel}
              onChange={(e) => {
                setAiModel(e.target.value as AiModel | '');
                setPage(0);
              }}
              aria-label="AI 모델 필터"
              className="rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 focus:border-indigo-500 focus:outline-none"
            >
              <option value="">전체 모델</option>
              {AI_MODELS.map((item) => (
                <option key={item.value} value={item.value}>
                  {item.label}
                </option>
              ))}
            </select>

            <select
              value={sortBy}
              onChange={(e) => {
                setSortBy(e.target.value as PromptSortBy);
                setPage(0);
              }}
              aria-label="정렬 기준"
              className="rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 focus:border-indigo-500 focus:outline-none"
            >
              {PROMPT_SORT_OPTIONS.map((item) => (
                <option key={item.value} value={item.value}>
                  {item.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* 카테고리 칩 */}
        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => {
              setCategory('');
              setPage(0);
            }}
            className={`cursor-pointer rounded-full px-3.5 py-1.5 text-xs font-semibold transition-colors ${
              category === ''
                ? 'bg-indigo-600 text-white'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            전체
          </button>
          {PROMPT_CATEGORIES.map((item) => (
            <button
              key={item.value}
              type="button"
              onClick={() => {
                setCategory(item.value);
                setPage(0);
              }}
              className={`cursor-pointer rounded-full px-3.5 py-1.5 text-xs font-semibold transition-colors ${
                category === item.value
                  ? 'bg-indigo-600 text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {item.label}
            </button>
          ))}

          {hasActiveFilters && (
            <button
              type="button"
              onClick={handleResetFilters}
              className="ml-auto cursor-pointer text-xs font-medium text-slate-500 underline hover:text-slate-800"
            >
              필터 초기화
            </button>
          )}
        </div>
      </section>

      {/* 결과 요약 */}
      {!isLoading && !errorMessage && promptsPage && (
        <div className="flex items-center justify-between text-xs text-slate-500">
          <span>
            총{' '}
            <strong className="font-semibold text-slate-800">
              {promptsPage.totalElements}
            </strong>
            개의 프롬프트
          </span>
        </div>
      )}

      {/* 목록 영역 */}
      {errorMessage ? (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center">
          <p className="text-sm font-medium text-red-700">{errorMessage}</p>
          <button
            type="button"
            onClick={() => void fetchPrompts()}
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
            조건에 맞는 프롬프트가 없습니다
          </h2>
          <p className="mt-1 text-sm text-slate-500">
            검색어나 필터를 변경하거나 첫 번째 프롬프트를 등록해 보세요.
          </p>
          <div className="mt-5 flex justify-center gap-2.5">
            {hasActiveFilters && (
              <button
                type="button"
                onClick={handleResetFilters}
                className="cursor-pointer rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                필터 초기화
              </button>
            )}
            <Link
              to="/prompts/new"
              className="rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
            >
              프롬프트 등록하기
            </Link>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {prompts.map((prompt) => (
            <Link
              key={prompt.id}
              to={`/prompts/${prompt.id}`}
              className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:border-indigo-200 hover:shadow-md"
            >
              <div>
                <div className="flex flex-wrap items-center gap-1.5">
                  <span className="rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-semibold text-indigo-700">
                    {CATEGORY_LABEL_MAP[prompt.category] ?? prompt.category}
                  </span>
                  <span className="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-700">
                    {AI_MODEL_LABEL_MAP[prompt.aiModel] ?? prompt.aiModel}
                  </span>
                </div>

                <h2 className="mt-3 line-clamp-1 text-base font-bold text-slate-900 group-hover:text-indigo-600">
                  {prompt.title}
                </h2>
                <p className="mt-2 line-clamp-3 text-sm leading-relaxed text-slate-600">
                  {prompt.content}
                </p>
              </div>

              <div className="mt-5 flex items-center justify-between border-t border-slate-100 pt-3 text-xs text-slate-500">
                <div className="flex items-center gap-1.5 truncate">
                  <span className="truncate font-medium text-slate-700">
                    {prompt.authorNickname}
                  </span>
                  <span>·</span>
                  <span className="shrink-0">
                    {formatDateTime(prompt.createdAt)}
                  </span>
                </div>

                <div className="flex shrink-0 items-center gap-2.5">
                  <span className="inline-flex items-center gap-1">
                    <Heart
                      className={`h-3.5 w-3.5 ${
                        prompt.liked
                          ? 'fill-rose-500 text-rose-500'
                          : 'text-slate-400'
                      }`}
                    />
                    {prompt.likeCount}
                  </span>
                  <span className="inline-flex items-center gap-1">
                    <Bookmark
                      className={`h-3.5 w-3.5 ${
                        prompt.bookmarked
                          ? 'fill-amber-500 text-amber-500'
                          : 'text-slate-400'
                      }`}
                    />
                    {prompt.bookmarkCount}
                  </span>
                  <span className="inline-flex items-center gap-1">
                    <Copy className="h-3.5 w-3.5 text-slate-400" />
                    {prompt.copyCount}
                  </span>
                </div>
              </div>
            </Link>
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
