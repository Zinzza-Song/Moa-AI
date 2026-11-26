import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import { createAdminNews } from '../api/news';
import { extractApiError } from '../constants/promptOptions';

export function NewsCreatePage() {
  const navigate = useNavigate();

  const [title, setTitle] = useState<string>('');
  const [sourceName, setSourceName] = useState<string>('');
  const [originalUrl, setOriginalUrl] = useState<string>('');
  const [summary, setSummary] = useState<string>('');
  const [tags, setTags] = useState<string>('');
  const [publishedAt, setPublishedAt] = useState<string>('');

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (isSubmitting) return;

    setErrorMessage(null);

    const trimmedTitle = title.trim();
    const trimmedSource = sourceName.trim();
    const trimmedUrl = originalUrl.trim();
    const trimmedSummary = summary.trim();
    const trimmedTags = tags.trim();

    if (!trimmedTitle) {
      setErrorMessage('제목은 필수입니다.');
      return;
    }
    if (trimmedTitle.length > 255) {
      setErrorMessage('제목은 255자 이하로 입력해 주세요.');
      return;
    }
    if (!trimmedSource) {
      setErrorMessage('출처명은 필수입니다.');
      return;
    }
    if (trimmedSource.length > 100) {
      setErrorMessage('출처명은 100자 이하로 입력해 주세요.');
      return;
    }
    if (!trimmedUrl) {
      setErrorMessage('원문 URL은 필수입니다.');
      return;
    }
    if (trimmedUrl.length > 500) {
      setErrorMessage('원문 URL은 500자 이하로 입력해 주세요.');
      return;
    }
    if (!/^https?:\/\/.+/i.test(trimmedUrl)) {
      setErrorMessage('원문 URL은 http:// 또는 https://로 시작해야 합니다.');
      return;
    }
    if (!trimmedSummary) {
      setErrorMessage('요약 내용은 필수입니다.');
      return;
    }
    if (trimmedTags.length > 255) {
      setErrorMessage('태그는 255자 이하로 입력해 주세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      const created = await createAdminNews({
        title: trimmedTitle,
        summary: trimmedSummary,
        originalUrl: trimmedUrl,
        sourceName: trimmedSource,
        tags: trimmedTags || undefined,
        publishedAt: publishedAt ? `${publishedAt}:00` : undefined,
      });
      navigate(`/news/${created.id}`, { replace: true });
    } catch (error) {
      const { message } = extractApiError(error, '뉴스 등록에 실패했습니다.');
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl">
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs sm:p-8">
        <h1 className="text-2xl font-bold text-slate-900">
          [관리자] AI 트렌드 뉴스 등록
        </h1>
        <p className="mt-1.5 text-sm text-slate-600">
          최신 AI 뉴스 요약과 원문 링크를 등록합니다.
        </p>

        <form onSubmit={(e) => void handleSubmit(e)} className="mt-6 space-y-5">
          {errorMessage && (
            <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              {errorMessage}
            </div>
          )}

          <div>
            <div className="flex items-center justify-between">
              <label
                htmlFor="news-title"
                className="block text-sm font-semibold text-slate-800"
              >
                제목 <span className="text-red-500">*</span>
              </label>
              <span className="text-xs text-slate-400">
                {title.length} / 255
              </span>
            </div>
            <input
              id="news-title"
              type="text"
              maxLength={255}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="예: Google DeepMind, 차세대 Gemini 모델 공개"
              className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
            />
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <div className="flex items-center justify-between">
                <label
                  htmlFor="news-source"
                  className="block text-sm font-semibold text-slate-800"
                >
                  출처명 <span className="text-red-500">*</span>
                </label>
                <span className="text-xs text-slate-400">
                  {sourceName.length} / 100
                </span>
              </div>
              <input
                id="news-source"
                type="text"
                maxLength={100}
                value={sourceName}
                onChange={(e) => setSourceName(e.target.value)}
                placeholder="예: Google Blog"
                className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
              />
            </div>

            <div>
              <label
                htmlFor="news-published-at"
                className="block text-sm font-semibold text-slate-800"
              >
                발행일시{' '}
                <span className="text-xs font-normal text-slate-400">
                  (선택)
                </span>
              </label>
              <input
                id="news-published-at"
                type="datetime-local"
                value={publishedAt}
                onChange={(e) => setPublishedAt(e.target.value)}
                className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 focus:border-indigo-500 focus:outline-none"
              />
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between">
              <label
                htmlFor="news-url"
                className="block text-sm font-semibold text-slate-800"
              >
                원문 URL <span className="text-red-500">*</span>
              </label>
              <span className="text-xs text-slate-400">
                {originalUrl.length} / 500
              </span>
            </div>
            <input
              id="news-url"
              type="url"
              maxLength={500}
              value={originalUrl}
              onChange={(e) => setOriginalUrl(e.target.value)}
              placeholder="https://example.com/article"
              className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
            />
          </div>

          <div>
            <label
              htmlFor="news-summary"
              className="block text-sm font-semibold text-slate-800"
            >
              요약 내용 <span className="text-red-500">*</span>
            </label>
            <textarea
              id="news-summary"
              rows={6}
              value={summary}
              onChange={(e) => setSummary(e.target.value)}
              placeholder="핵심 내용을 요약해 주세요."
              className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white p-4 text-sm leading-relaxed text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
            />
          </div>

          <div>
            <div className="flex items-center justify-between">
              <label
                htmlFor="news-tags"
                className="block text-sm font-semibold text-slate-800"
              >
                태그{' '}
                <span className="text-xs font-normal text-slate-400">
                  (선택, 쉼표로 구분)
                </span>
              </label>
              <span className="text-xs text-slate-400">
                {tags.length} / 255
              </span>
            </div>
            <input
              id="news-tags"
              type="text"
              maxLength={255}
              value={tags}
              onChange={(e) => setTags(e.target.value)}
              placeholder="예: Gemini, LLM, Agent"
              className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
            />
          </div>

          <div className="flex items-center justify-end gap-2.5 border-t border-slate-100 pt-5">
            <button
              type="button"
              disabled={isSubmitting}
              onClick={() => navigate('/news')}
              className="cursor-pointer rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-60"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="inline-flex cursor-pointer items-center gap-2 rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" />}
              <span>뉴스 등록</span>
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
