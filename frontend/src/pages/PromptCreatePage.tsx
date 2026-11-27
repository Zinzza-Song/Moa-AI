import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { createPrompt } from '../api/prompt';
import { PromptForm } from '../components/prompt/PromptForm';
import { extractApiError } from '../constants/promptOptions';
import type { AiModel, PromptCreateRequest } from '../types/prompt';

interface CreateLocationState {
  title?: string;
  content?: string;
  resultExample?: string;
  aiModel?: AiModel;
}

export function PromptCreatePage() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state as CreateLocationState | null) ?? null;

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const handleCreate = async (values: PromptCreateRequest) => {
    setIsSubmitting(true);
    setSubmitError(null);
    try {
      const created = await createPrompt(values);
      navigate(`/prompts/${created.id}`, { replace: true });
    } catch (error) {
      const { message } = extractApiError(error, '프롬프트 등록에 실패했습니다.');
      setSubmitError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl">
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs sm:p-8">
        <h1 className="text-2xl font-bold text-slate-900">새 프롬프트 등록</h1>
        <p className="mt-1.5 text-sm text-slate-600">
          나만의 프롬프트 노하우를 커뮤니티에 공유해 보세요.
        </p>

        <div className="mt-6">
          <PromptForm
            mode="create"
            initialValues={{
              title: state?.title ?? '',
              content: state?.content ?? '',
              resultExample: state?.resultExample ?? '',
              aiModel: state?.aiModel ?? 'GEMINI',
            }}
            isSubmitting={isSubmitting}
            submitError={submitError}
            onSubmit={handleCreate}
            onCancel={() => navigate(-1)}
          />
        </div>
      </section>
    </div>
  );
}
