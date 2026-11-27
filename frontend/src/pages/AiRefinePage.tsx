import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  AlertTriangle,
  Check,
  CheckCircle2,
  Copy,
  Loader2,
  PlusCircle,
  Sparkles,
  Wand2,
} from 'lucide-react';
import { getRemainingRefineCount, refinePrompt } from '../api/ai';
import { AI_MODELS, extractApiError } from '../constants/promptOptions';
import type { PromptRefineResponse } from '../types/ai';
import type { AiModel } from '../types/prompt';

export function AiRefinePage() {
  const navigate = useNavigate();

  const [remainingCount, setRemainingCount] = useState<number | null>(null);
  const [originalPrompt, setOriginalPrompt] = useState<string>('');
  const [targetModel, setTargetModel] = useState<AiModel | ''>('');
  const [goal, setGoal] = useState<string>('');

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorBanner, setErrorBanner] = useState<{
    title: string;
    message: string;
  } | null>(null);
  const [result, setResult] = useState<PromptRefineResponse | null>(null);
  const [copied, setCopied] = useState<boolean>(false);

  useEffect(() => {
    let mounted = true;
    async function loadRemaining() {
      try {
        const count = await getRemainingRefineCount();
        if (mounted) {
          setRemainingCount(count);
        }
      } catch {
        // 남은 횟수 조회 실패 시에도 폼 사용은 가능하도록 조용히 처리
      }
    }
    void loadRemaining();
    return () => {
      mounted = false;
    };
  }, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (isSubmitting) return;

    const trimmedPrompt = originalPrompt.trim();
    if (!trimmedPrompt) {
      setErrorBanner({
        title: '입력 확인',
        message: '개선할 원본 프롬프트를 입력해 주세요.',
      });
      return;
    }

    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      const response = await refinePrompt({
        originalPrompt: trimmedPrompt,
        targetModel: targetModel || undefined,
        goal: goal.trim() || undefined,
      });
      setResult(response);
      setRemainingCount(response.remainingDailyCount);
    } catch (error) {
      const { status, code, message } = extractApiError(
        error,
        'AI 프롬프트 개선 요청 중 오류가 발생했습니다.',
      );

      if (status === 429 || code === 'P003') {
        setRemainingCount(0);
        setErrorBanner({
          title: '일일 무료 개선 한도 초과',
          message:
            message ||
            '오늘 사용할 수 있는 무료 프롬프트 개선 횟수를 모두 소진했습니다. 내일 다시 이용해 주세요.',
        });
      } else if (status === 502 || code === 'P004') {
        setErrorBanner({
          title: 'Gemini AI 응답 오류',
          message:
            message ||
            'AI 응답을 처리하는 중 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.',
        });
      } else {
        setErrorBanner({
          title: '요청 실패',
          message,
        });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCopyRefined = async () => {
    if (!result) return;
    try {
      await navigator.clipboard.writeText(result.refinedPrompt);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      setErrorBanner({
        title: '복사 실패',
        message: '클립보드 복사에 실패했습니다.',
      });
    }
  };

  const handleRegisterPrompt = () => {
    if (!result) return;
    navigate('/prompts/new', {
      state: {
        title: result.suggestedTitle,
        content: result.refinedPrompt,
        resultExample: result.resultExample,
        aiModel: targetModel || undefined,
      },
    });
  };

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      {/* 상단 헤더 & 남은 횟수 배지 */}
      <section className="flex flex-col justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-xs sm:flex-row sm:items-center">
        <div>
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-600">
            <Wand2 className="h-4 w-4" />
            <span>Gemini AI 프롬프트 엔지니어링</span>
          </div>
          <h1 className="mt-1.5 text-2xl font-bold text-slate-900">
            AI 프롬프트 자동 개선
          </h1>
          <p className="mt-1 text-sm text-slate-600">
            작성한 초안 프롬프트를 Gemini가 역할·맥락·출력 형식까지 구조화하여 개선해 줍니다.
          </p>
        </div>

        <div className="shrink-0 rounded-xl border border-indigo-200 bg-indigo-50 px-4 py-3 text-center">
          <p className="text-xs font-medium text-indigo-700">
            오늘 남은 무료 개선 횟수
          </p>
          <p className="mt-0.5 text-xl font-bold text-indigo-900">
            {remainingCount === null ? '-' : `${remainingCount}회`}
          </p>
        </div>
      </section>

      {/* 에러 배너 */}
      {errorBanner && (
        <div className="flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50 p-4 text-red-800">
          <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0 text-red-600" />
          <div>
            <h2 className="text-sm font-bold">{errorBanner.title}</h2>
            <p className="mt-0.5 text-sm">{errorBanner.message}</p>
          </div>
        </div>
      )}

      {/* 입력 폼 */}
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
        <form onSubmit={(e) => void handleSubmit(e)} className="space-y-5">
          <div>
            <div className="flex items-center justify-between">
              <label
                htmlFor="original-prompt"
                className="block text-sm font-semibold text-slate-800"
              >
                원본 프롬프트 <span className="text-red-500">*</span>
              </label>
              <span className="text-xs text-slate-400">
                {originalPrompt.length} / 2000
              </span>
            </div>
            <textarea
              id="original-prompt"
              rows={6}
              maxLength={2000}
              value={originalPrompt}
              onChange={(e) => setOriginalPrompt(e.target.value)}
              placeholder="예: 블로그 글 써줘. 주제는 Spring Boot JWT 로그인 구현 방법이야."
              className="mt-1.5 w-full rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm leading-relaxed text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:bg-white focus:outline-none"
            />
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label
                htmlFor="target-model"
                className="block text-sm font-semibold text-slate-800"
              >
                대상 AI 모델{' '}
                <span className="text-xs font-normal text-slate-400">
                  (선택)
                </span>
              </label>
              <select
                id="target-model"
                value={targetModel}
                onChange={(e) => setTargetModel(e.target.value as AiModel | '')}
                className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 focus:border-indigo-500 focus:outline-none"
              >
                <option value="">범용 (지정 안 함)</option>
                {AI_MODELS.map((item) => (
                  <option key={item.value} value={item.value}>
                    {item.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <div className="flex items-center justify-between">
                <label
                  htmlFor="refine-goal"
                  className="block text-sm font-semibold text-slate-800"
                >
                  개선 목표{' '}
                  <span className="text-xs font-normal text-slate-400">
                    (선택)
                  </span>
                </label>
                <span className="text-xs text-slate-400">
                  {goal.length} / 300
                </span>
              </div>
              <input
                id="refine-goal"
                type="text"
                maxLength={300}
                value={goal}
                onChange={(e) => setGoal(e.target.value)}
                placeholder="예: 마크다운 표 형식으로 단계별 답변 유도"
                className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
              />
            </div>
          </div>

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={isSubmitting || remainingCount === 0}
              className="inline-flex cursor-pointer items-center gap-2 rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  <span>Gemini가 프롬프트를 개선 중입니다...</span>
                </>
              ) : (
                <>
                  <Sparkles className="h-4 w-4" />
                  <span>프롬프트 개선하기</span>
                </>
              )}
            </button>
          </div>
        </form>
      </section>

      {/* 개선 결과 영역 */}
      {result && (
        <section className="space-y-5 rounded-2xl border border-indigo-200 bg-white p-6 shadow-xs">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-lg font-bold text-slate-900">
              개선된 프롬프트
            </h2>
            <div className="flex flex-wrap items-center gap-2">
              <button
                type="button"
                onClick={() => void handleCopyRefined()}
                className="inline-flex cursor-pointer items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50"
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
              <button
                type="button"
                onClick={handleRegisterPrompt}
                className="inline-flex cursor-pointer items-center gap-1.5 rounded-xl bg-indigo-600 px-4 py-2 text-xs font-semibold text-white hover:bg-indigo-700"
              >
                <PlusCircle className="h-3.5 w-3.5" />
                <span>이 프롬프트로 글 등록하기</span>
              </button>
            </div>
          </div>

          {result.suggestedTitle && (
            <div className="rounded-xl border border-indigo-100 bg-indigo-50/60 px-4 py-3">
              <p className="text-xs font-semibold text-indigo-700">추천 제목</p>
              <p className="mt-1 text-base font-bold text-slate-900">
                {result.suggestedTitle}
              </p>
            </div>
          )}

          <div>
            <p className="mb-1.5 text-xs font-semibold text-slate-500">
              프롬프트 본문
            </p>
            <pre className="overflow-x-auto rounded-xl border border-slate-200 bg-slate-50 p-5 font-mono text-sm leading-relaxed whitespace-pre-wrap text-slate-800">
              {result.refinedPrompt}
            </pre>
          </div>

          {result.resultExample && (
            <div className="rounded-xl border border-slate-200 bg-slate-50/70 p-4">
              <h3 className="text-sm font-semibold text-slate-800">
                실행 결과 예시
              </h3>
              <pre className="mt-2 overflow-x-auto whitespace-pre-wrap font-sans text-sm leading-relaxed text-slate-700">
                {result.resultExample}
              </pre>
            </div>
          )}

          {result.improvements.length > 0 && (
            <div className="rounded-xl border border-slate-100 bg-slate-50/70 p-4">
              <h3 className="text-sm font-semibold text-slate-800">
                주요 개선 포인트
              </h3>
              <ul className="mt-2.5 space-y-2">
                {result.improvements.map((item, idx) => (
                  <li
                    key={idx}
                    className="flex items-start gap-2 text-sm text-slate-700"
                  >
                    <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-emerald-600" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </section>
      )}
    </div>
  );
}
