import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  AlertCircle,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  ExternalLink,
  Loader2,
  Newspaper,
  Play,
  PlayCircle,
  PlusCircle,
  Search,
  Sparkles,
  Tag,
  Video,
  X,
} from 'lucide-react';
import {
  collectAiNews,
  collectAiVideos,
  getNewsList,
  getNewsVideos,
} from '../api/news';
import { extractApiError, formatDateTime } from '../constants/promptOptions';
import { useAuth } from '../context/AuthContext';
import type { PageResponse } from '../types/api';
import type { AiNewsResponse, AiVideoResponse } from '../types/news';

type NewsTab = 'articles' | 'videos';

function decodeHtmlEntities(text: string): string {
  if (!text || !text.includes('&')) return text;
  const doc = new DOMParser().parseFromString(text, 'text/html');
  return doc.documentElement.textContent ?? text;
}

export function NewsPage() {
  const { user, isAuthenticated } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();

  const initialTag = searchParams.get('tag') ?? '';
  const initialKeyword = searchParams.get('keyword') ?? '';
  const initialTab: NewsTab =
    searchParams.get('tab') === 'videos' ? 'videos' : 'articles';

  const [activeTab, setActiveTab] = useState<NewsTab>(initialTab);

  const [newsPage, setNewsPage] =
    useState<PageResponse<AiNewsResponse> | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [videos, setVideos] = useState<AiVideoResponse[]>([]);
  const [isVideosLoading, setIsVideosLoading] = useState<boolean>(false);
  const [videosError, setVideosError] = useState<string | null>(null);
  const [hasLoadedVideos, setHasLoadedVideos] = useState<boolean>(false);

  const [searchInput, setSearchInput] = useState<string>(initialKeyword);
  const [keyword, setKeyword] = useState<string>(initialKeyword);
  const [selectedTag, setSelectedTag] = useState<string>(initialTag);
  const [page, setPage] = useState<number>(0);
  const [isCollecting, setIsCollecting] = useState<boolean>(false);
  const [isCollectingVideos, setIsCollectingVideos] = useState<boolean>(false);
  const [collectNotice, setCollectNotice] = useState<{
    type: 'success' | 'error';
    message: string;
  } | null>(null);

  const isAdmin = isAuthenticated && user?.role === 'ADMIN';

  const fetchNews = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await getNewsList({
        keyword: keyword || undefined,
        tag: selectedTag || undefined,
        page,
        size: 10,
      });
      setNewsPage(data);
    } catch (error) {
      const { message } = extractApiError(
        error,
        'AI 트렌드 뉴스를 불러오지 못했습니다.',
      );
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  }, [keyword, selectedTag, page]);

  useEffect(() => {
    void fetchNews();
  }, [fetchNews]);

  const fetchVideos = useCallback(async () => {
    setIsVideosLoading(true);
    setVideosError(null);
    try {
      const data = await getNewsVideos(12);
      setVideos(data);
      setHasLoadedVideos(true);
    } catch (error) {
      const { message } = extractApiError(
        error,
        'AI 유튜브 영상을 불러오지 못했습니다.',
      );
      setVideosError(message);
    } finally {
      setIsVideosLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'videos' && !hasLoadedVideos) {
      void fetchVideos();
    }
  }, [activeTab, hasLoadedVideos, fetchVideos]);

  const syncQueryParams = (nextKeyword: string, nextTag: string) => {
    const next = new URLSearchParams();
    if (nextKeyword) next.set('keyword', nextKeyword);
    if (nextTag) next.set('tag', nextTag);
    setSearchParams(next, { replace: true });
  };

  const handleSearchSubmit = (e: FormEvent) => {
    e.preventDefault();
    const trimmed = searchInput.trim();
    setKeyword(trimmed);
    setPage(0);
    syncQueryParams(trimmed, selectedTag);
  };

  const handleTagClick = (tag: string) => {
    const nextTag = selectedTag === tag ? '' : tag;
    setSelectedTag(nextTag);
    setPage(0);
    syncQueryParams(keyword, nextTag);
  };

  const handleResetFilters = () => {
    setSearchInput('');
    setKeyword('');
    setSelectedTag('');
    setPage(0);
    syncQueryParams('', '');
  };

  const handleCollectNews = async () => {
    if (isCollecting) return;
    setIsCollecting(true);
    setCollectNotice(null);
    try {
      const result = await collectAiNews();
      const failedSuffix =
        result.failedCount > 0 ? `, 실패 ${result.failedCount}건` : '';
      setCollectNotice({
        type: 'success',
        message: `수집 완료: 신규 ${result.savedCount}건 저장, 중복 ${result.skippedCount}건 건너뜀${failedSuffix} (총 ${result.totalFetched}건 조회)`,
      });
      if (page !== 0) {
        setPage(0);
      } else {
        await fetchNews();
      }
    } catch (error) {
      const { message } = extractApiError(
        error,
        'AI 뉴스 자동 수집에 실패했습니다.',
      );
      setCollectNotice({
        type: 'error',
        message,
      });
    } finally {
      setIsCollecting(false);
    }
  };

  const handleCollectVideos = async () => {
    if (isCollectingVideos) return;
    setIsCollectingVideos(true);
    setCollectNotice(null);
    try {
      const message = await collectAiVideos();
      setCollectNotice({
        type: 'success',
        message: message || '유튜브 AI 영상 수집이 완료되었습니다.',
      });
      await fetchVideos();
    } catch (error) {
      const { message } = extractApiError(
        error,
        'AI 유튜브 영상 수집에 실패했습니다.',
      );
      setCollectNotice({
        type: 'error',
        message,
      });
    } finally {
      setIsCollectingVideos(false);
    }
  };

  const hasActiveFilters = Boolean(keyword || selectedTag);
  const newsList = newsPage?.content ?? [];
  const totalPages = newsPage?.totalPages ?? 0;

  return (
    <div className="space-y-6">
      {/* 상단 헤더 */}
      <section className="flex flex-col justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-xs sm:flex-row sm:items-center">
        <div>
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-600">
            <Newspaper className="h-4 w-4" />
            <span>AI Trend Briefing</span>
          </div>
          <h1 className="mt-1.5 text-2xl font-bold tracking-tight text-slate-900">
            AI 트렌드 뉴스
          </h1>
          <p className="mt-1 text-sm text-slate-600">
            최신 생성형 AI 소식과 핵심 요약을 빠르게 확인하세요.
          </p>
        </div>

        {isAdmin && (
          <div className="flex shrink-0 flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={() => void handleCollectNews()}
              disabled={isCollecting}
              className="inline-flex shrink-0 cursor-pointer items-center gap-1.5 rounded-xl border border-indigo-200 bg-indigo-50 px-4 py-2.5 text-sm font-semibold text-indigo-700 transition-colors hover:bg-indigo-100 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isCollecting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  <span>수집 및 요약 중...</span>
                </>
              ) : (
                <>
                  <Sparkles className="h-4 w-4" />
                  <span>AI 뉴스 자동 수집</span>
                </>
              )}
            </button>
            <button
              type="button"
              onClick={() => void handleCollectVideos()}
              disabled={isCollectingVideos}
              className="inline-flex shrink-0 cursor-pointer items-center gap-1.5 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm font-semibold text-rose-700 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isCollectingVideos ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  <span>영상 수집 중...</span>
                </>
              ) : (
                <>
                  <Video className="h-4 w-4" />
                  <span>영상 수집</span>
                </>
              )}
            </button>
            <Link
              to="/news/new"
              className="inline-flex shrink-0 items-center gap-1.5 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-indigo-700"
            >
              <PlusCircle className="h-4 w-4" />
              <span>뉴스 등록</span>
            </Link>
          </div>
        )}
      </section>

      {/* AI 뉴스 자동 수집 결과 알림 */}
      {collectNotice && (
        <div
          role="status"
          className={`flex items-start justify-between gap-3 rounded-2xl border px-4 py-3 text-sm ${
            collectNotice.type === 'success'
              ? 'border-emerald-200 bg-emerald-50 text-emerald-800'
              : 'border-red-200 bg-red-50 text-red-700'
          }`}
        >
          <div className="flex items-center gap-2">
            {collectNotice.type === 'success' ? (
              <CheckCircle2 className="h-4 w-4 shrink-0 text-emerald-600" />
            ) : (
              <AlertCircle className="h-4 w-4 shrink-0 text-red-600" />
            )}
            <span className="font-medium">{collectNotice.message}</span>
          </div>
          <button
            type="button"
            onClick={() => setCollectNotice(null)}
            aria-label="알림 닫기"
            className="cursor-pointer rounded-lg p-1 opacity-70 transition-opacity hover:opacity-100"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* 탭 전환: AI 뉴스 기사 | AI 유튜브 영상 */}
      <div className="flex items-center justify-between border-b border-slate-200">
        <div className="flex gap-1" role="tablist" aria-label="AI 뉴스 콘텐츠 탭">
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'articles'}
            onClick={() => setActiveTab('articles')}
            className={`inline-flex cursor-pointer items-center gap-1.5 border-b-2 px-4 py-3 text-sm font-semibold transition-colors ${
              activeTab === 'articles'
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            <Newspaper className="h-4 w-4" />
            <span>AI 뉴스 기사</span>
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'videos'}
            onClick={() => setActiveTab('videos')}
            className={`inline-flex cursor-pointer items-center gap-1.5 border-b-2 px-4 py-3 text-sm font-semibold transition-colors ${
              activeTab === 'videos'
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            <PlayCircle className="h-4 w-4" />
            <span>AI 유튜브 영상</span>
          </button>
        </div>

        {activeTab === 'videos' && !isVideosLoading && !videosError && (
          <span className="hidden text-xs text-slate-500 sm:inline">
            최신{' '}
            <strong className="font-semibold text-slate-800">
              {videos.length}
            </strong>
            개의 영상
          </span>
        )}
      </div>

      {activeTab === 'articles' ? (
        <>
          {/* 검색 & 활성 태그 필터 */}
      <section className="space-y-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-xs sm:p-5">
        <form onSubmit={handleSearchSubmit} className="flex gap-2">
          <div className="relative flex-1">
            <Search className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="뉴스 제목 또는 요약 키워드로 검색..."
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

        {hasActiveFilters && (
          <div className="flex flex-wrap items-center gap-2 pt-1">
            {selectedTag && (
              <button
                type="button"
                onClick={() => handleTagClick(selectedTag)}
                className="inline-flex cursor-pointer items-center gap-1.5 rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-indigo-700 hover:bg-indigo-100"
              >
                <Tag className="h-3 w-3" />
                <span>#{selectedTag}</span>
                <X className="h-3.5 w-3.5" />
              </button>
            )}
            <button
              type="button"
              onClick={handleResetFilters}
              className="ml-auto cursor-pointer text-xs font-medium text-slate-500 underline hover:text-slate-800"
            >
              필터 초기화
            </button>
          </div>
        )}
      </section>

      {/* 결과 개수 */}
      {!isLoading && !errorMessage && newsPage && (
        <div className="text-xs text-slate-500">
          총{' '}
          <strong className="font-semibold text-slate-800">
            {newsPage.totalElements}
          </strong>
          건의 뉴스
        </div>
      )}

      {/* 목록 */}
      {errorMessage ? (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center">
          <p className="text-sm font-medium text-red-700">{errorMessage}</p>
          <button
            type="button"
            onClick={() => void fetchNews()}
            className="mt-4 cursor-pointer rounded-xl bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
          >
            다시 시도
          </button>
        </div>
      ) : isLoading ? (
        <div className="space-y-4">
          {Array.from({ length: 4 }).map((_, index) => (
            <div
              key={index}
              className="animate-pulse rounded-2xl border border-slate-200 bg-white p-6"
            >
              <div className="flex items-center gap-2">
                <div className="h-4 w-20 rounded bg-slate-200" />
                <div className="h-4 w-24 rounded bg-slate-100" />
              </div>
              <div className="mt-3 h-5 w-2/3 rounded bg-slate-200" />
              <div className="mt-2.5 space-y-2">
                <div className="h-3.5 w-full rounded bg-slate-100" />
                <div className="h-3.5 w-4/5 rounded bg-slate-100" />
              </div>
            </div>
          ))}
        </div>
      ) : newsList.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center">
          <Newspaper className="mx-auto h-10 w-10 text-slate-300" />
          <h2 className="mt-3 text-base font-semibold text-slate-900">
            등록된 AI 트렌드 뉴스가 없습니다
          </h2>
          <p className="mt-1 text-sm text-slate-500">
            {hasActiveFilters
              ? '검색어나 태그 필터를 변경해 보세요.'
              : '아직 등록된 뉴스가 없습니다.'}
          </p>
          {hasActiveFilters && (
            <button
              type="button"
              onClick={handleResetFilters}
              className="mt-5 cursor-pointer rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              필터 초기화
            </button>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {newsList.map((news) => (
            <article
              key={news.id}
              className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs transition-colors hover:border-indigo-200"
            >
              <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-slate-500">
                <div className="flex items-center gap-2">
                  <span className="rounded-md bg-slate-100 px-2.5 py-1 font-semibold text-slate-700">
                    {news.sourceName}
                  </span>
                  <span>
                    {formatDateTime(news.publishedAt || news.createdAt)}
                  </span>
                </div>

                <a
                  href={news.originalUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 font-medium text-slate-500 hover:text-indigo-600"
                >
                  <span>원문</span>
                  <ExternalLink className="h-3.5 w-3.5" />
                </a>
              </div>

              <Link to={`/news/${news.id}`} className="group block">
                <h2 className="mt-3 text-lg font-bold text-slate-900 group-hover:text-indigo-600">
                  {news.title}
                </h2>
                <p className="mt-2 line-clamp-2 text-sm leading-relaxed text-slate-600">
                  {news.summary}
                </p>
              </Link>

              {news.tags.length > 0 && (
                <div className="mt-4 flex flex-wrap gap-1.5">
                  {news.tags.map((tag) => {
                    const isSelected = selectedTag === tag;
                    return (
                      <button
                        key={tag}
                        type="button"
                        onClick={() => handleTagClick(tag)}
                        className={`cursor-pointer rounded-full px-2.5 py-1 text-xs font-medium transition-colors ${
                          isSelected
                            ? 'bg-indigo-600 text-white'
                            : 'bg-slate-100 text-slate-600 hover:bg-indigo-50 hover:text-indigo-700'
                        }`}
                      >
                        #{tag}
                      </button>
                    );
                  })}
                </div>
              )}
            </article>
          ))}
        </div>
      )}

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <nav
          aria-label="뉴스 페이지네이션"
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
            disabled={newsPage?.last ?? page >= totalPages - 1}
            onClick={() => setPage((prev) => prev + 1)}
            className="inline-flex cursor-pointer items-center rounded-lg border border-slate-200 bg-white p-2 text-slate-600 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
            aria-label="다음 페이지"
          >
            <ChevronRight className="h-4 w-4" />
          </button>
        </nav>
      )}
        </>
      ) : (
        /* AI 유튜브 영상 탭 */
        <>
          {videosError ? (
            <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center">
              <p className="text-sm font-medium text-red-700">{videosError}</p>
              <button
                type="button"
                onClick={() => void fetchVideos()}
                className="mt-4 cursor-pointer rounded-xl bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
              >
                다시 시도
              </button>
            </div>
          ) : isVideosLoading ? (
            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {Array.from({ length: 6 }).map((_, index) => (
                <div
                  key={index}
                  className="animate-pulse overflow-hidden rounded-2xl border border-slate-200 bg-white"
                >
                  <div className="aspect-video w-full bg-slate-200" />
                  <div className="space-y-3 p-4">
                    <div className="space-y-2">
                      <div className="h-4 w-full rounded bg-slate-200" />
                      <div className="h-4 w-3/4 rounded bg-slate-200" />
                    </div>
                    <div className="flex items-center justify-between border-t border-slate-100 pt-2.5">
                      <div className="h-3.5 w-24 rounded bg-slate-100" />
                      <div className="h-3.5 w-20 rounded bg-slate-100" />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : videos.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center">
              <PlayCircle className="mx-auto h-10 w-10 text-slate-300" />
              <h2 className="mt-3 text-base font-semibold text-slate-900">
                등록된 AI 유튜브 영상이 없습니다
              </h2>
              <p className="mt-1 text-sm text-slate-500">
                {isAdmin
                  ? '상단의 영상 수집 버튼을 눌러 최신 AI 유튜브 영상을 불러올 수 있습니다.'
                  : '아직 수집된 AI 유튜브 영상이 없습니다. 잠시 후 다시 확인해 주세요.'}
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {videos.map((video) => {
                const decodedTitle = decodeHtmlEntities(video.title);
                return (
                  <a
                    key={video.id}
                    href={video.videoUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="group flex flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs transition-all hover:-translate-y-0.5 hover:border-indigo-200 hover:shadow-md"
                  >
                    <div className="relative aspect-video w-full overflow-hidden bg-slate-100">
                      <img
                        src={video.thumbnailUrl}
                        alt={decodedTitle}
                        loading="lazy"
                        className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                      />
                      <div className="pointer-events-none absolute inset-0 flex items-center justify-center bg-slate-900/0 transition-colors group-hover:bg-slate-900/30">
                        <span className="flex h-12 w-12 items-center justify-center rounded-full bg-white/90 text-indigo-600 opacity-0 shadow-md transition-opacity group-hover:opacity-100">
                          <Play className="ml-0.5 h-5 w-5 fill-current" />
                        </span>
                      </div>
                    </div>

                    <div className="flex flex-1 flex-col justify-between p-4">
                      <h2 className="line-clamp-2 text-sm leading-snug font-bold text-slate-900 group-hover:text-indigo-600 sm:text-base">
                        {decodedTitle}
                      </h2>

                      <div className="mt-3 flex items-center justify-between gap-2 border-t border-slate-100 pt-2.5 text-xs text-slate-500">
                        <span className="truncate font-medium text-slate-700">
                          {video.channelTitle}
                        </span>
                        <span className="shrink-0">
                          {formatDateTime(video.publishedAt)}
                        </span>
                      </div>
                    </div>
                  </a>
                );
              })}
            </div>
          )}
        </>
      )}
    </div>
  );
}
