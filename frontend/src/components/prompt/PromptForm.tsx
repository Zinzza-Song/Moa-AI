import { useState, type FormEvent } from 'react';
import { Loader2 } from 'lucide-react';
import { AI_MODELS, PROMPT_CATEGORIES } from '../../constants/promptOptions';
import type {
  AiModel,
  PromptCategory,
  PromptCreateRequest,
} from '../../types/prompt';

interface PromptFormProps {
  mode: 'create' | 'edit';
  initialValues?: Partial<PromptCreateRequest>;
  isSubmitting: boolean;
  submitError: string | null;
  onSubmit: (values: PromptCreateRequest) => Promise<void>;
  onCancel: () => void;
}

export function PromptForm({
  mode,
  initialValues,
  isSubmitting,
  submitError,
  onSubmit,
  onCancel,
}: PromptFormProps) {
  const [title, setTitle] = useState<string>(initialValues?.title ?? '');
  const [category, setCategory] = useState<PromptCategory>(
    initialValues?.category ?? 'TEXT',
  );
  const [aiModel, setAiModel] = useState<AiModel>(
    initialValues?.aiModel ?? 'GEMINI',
  );
  const [content, setContent] = useState<string>(initialValues?.content ?? '');
  const [resultExample, setResultExample] = useState<string>(
    initialValues?.resultExample ?? '',
  );
  const [validationError, setValidationError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setValidationError(null);

    const trimmedTitle = title.trim();
    const trimmedContent = content.trim();

    if (!trimmedTitle) {
      setValidationError('제목은 필수입니다.');
      return;
    }
    if (trimmedTitle.length > 150) {
      setValidationError('제목은 150자 이하로 입력해 주세요.');
      return;
    }
    if (!trimmedContent) {
      setValidationError('프롬프트 내용은 필수입니다.');
      return;
    }

    await onSubmit({
      title: trimmedTitle,
      content: trimmedContent,
      resultExample: resultExample.trim() || undefined,
      category,
      aiModel,
    });
  };

  const displayError = validationError || submitError;

  return (
    <form onSubmit={(e) => void handleSubmit(e)} className="space-y-6">
      {displayError && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {displayError}
        </div>
      )}

      <div>
        <div className="flex items-center justify-between">
          <label
            htmlFor="prompt-title"
            className="block text-sm font-semibold text-slate-800"
          >
            제목 <span className="text-red-500">*</span>
          </label>
          <span className="text-xs text-slate-400">{title.length} / 150</span>
        </div>
        <input
          id="prompt-title"
          type="text"
          maxLength={150}
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="예: 시니어 백엔드 개발자 코드 리뷰 프롬프트"
          className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
        />
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div>
          <label
            htmlFor="prompt-category"
            className="block text-sm font-semibold text-slate-800"
          >
            카테고리 <span className="text-red-500">*</span>
          </label>
          <select
            id="prompt-category"
            value={category}
            onChange={(e) => setCategory(e.target.value as PromptCategory)}
            className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 focus:border-indigo-500 focus:outline-none"
          >
            {PROMPT_CATEGORIES.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label
            htmlFor="prompt-model"
            className="block text-sm font-semibold text-slate-800"
          >
            대상 AI 모델 <span className="text-red-500">*</span>
          </label>
          <select
            id="prompt-model"
            value={aiModel}
            onChange={(e) => setAiModel(e.target.value as AiModel)}
            className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 focus:border-indigo-500 focus:outline-none"
          >
            {AI_MODELS.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div>
        <label
          htmlFor="prompt-content"
          className="block text-sm font-semibold text-slate-800"
        >
          프롬프트 내용 <span className="text-red-500">*</span>
        </label>
        <textarea
          id="prompt-content"
          rows={9}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="AI에게 전달할 프롬프트 본문을 입력해 주세요."
          className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white p-4 font-mono text-sm leading-relaxed text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
        />
      </div>

      <div>
        <label
          htmlFor="prompt-result"
          className="block text-sm font-semibold text-slate-800"
        >
          실행 결과 예시 <span className="text-xs font-normal text-slate-400">(선택)</span>
        </label>
        <textarea
          id="prompt-result"
          rows={4}
          value={resultExample}
          onChange={(e) => setResultExample(e.target.value)}
          placeholder="이 프롬프트를 실행했을 때 기대되는 출력 예시를 적어두면 다른 사용자에게 도움이 됩니다."
          className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white p-4 text-sm leading-relaxed text-slate-900 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
        />
      </div>

      <div className="flex items-center justify-end gap-2.5 border-t border-slate-100 pt-5">
        <button
          type="button"
          onClick={onCancel}
          disabled={isSubmitting}
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
          <span>{mode === 'create' ? '프롬프트 등록' : '수정 완료'}</span>
        </button>
      </div>
    </form>
  );
}
