import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { getPromptDetail, updatePrompt } from '../api/prompt';
import { PromptForm } from '../components/prompt/PromptForm';
import { extractApiError } from '../constants/promptOptions';
import { useAuth } from '../context/AuthContext';
import type { PromptCreateRequest, PromptResponse } from '../types/prompt';

export function PromptEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [prompt, setPrompt] = useState<PromptResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const promptId = Number(id);

  useEffect(() => {
    let mounted = true;

    async function load() {
      if (!id || Number.isNaN(promptId)) {
        if (mounted) {
          setLoadError('올바르지 않은 프롬프트 경로입니다.');
          setIsLoading(false);
        }
        return;
      }

      try {
        const data = await getPromptDetail(promptId);
        if (mounted) {
          setPrompt(data);
        }
      } catch (error) {
        if (mounted) {
          const { message } = extractApiError(
            error,
            '프롬프트를 불러오지 못했습니다.',
          );
          setLoadError(message);
        }
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    void load();

    return () => {
      mounted = false;
    };
  }, [id, promptId]);

  const handleUpdate = async (values: PromptCreateRequest) => {
    if (!prompt) return;
    setIsSubmitting(true);
    setSubmitError(null);
    try {
      const updated = await updatePrompt(prompt.id, values);
      navigate(`/prompts/${updated.id}`, { replace: true });
    } catch (error) {
      const { message } = extractApiError(error, '프롬프트 수정에 실패했습니다.');
      setSubmitError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex min-h-[45vh] flex-col items-center justify-center gap-3 text-slate-500">
        <Loader2 className="h-7 w-7 animate-spin text-indigo-600" />
        <p className="text-sm">프롬프트 정보를 불러오는 중입니다...</p>
      </div>
    );
  }

  if (loadError || !prompt) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-red-200 bg-white p-8 text-center">
        <p className="text-sm font-medium text-red-600">
          {loadError || '프롬프트를 찾을 수 없습니다.'}
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

  if (user?.id !== prompt.authorId) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-amber-200 bg-white p-8 text-center">
        <p className="text-sm font-medium text-amber-800">
          작성자 본인만 프롬프트를 수정할 수 있습니다.
        </p>
        <Link
          to={`/prompts/${prompt.id}`}
          className="mt-4 inline-flex items-center gap-1.5 rounded-xl bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>상세 페이지로 돌아가기</span>
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl">
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs sm:p-8">
        <h1 className="text-2xl font-bold text-slate-900">프롬프트 수정</h1>
        <p className="mt-1.5 text-sm text-slate-600">
          등록한 프롬프트 내용을 수정합니다.
        </p>

        <div className="mt-6">
          <PromptForm
            mode="edit"
            initialValues={{
              title: prompt.title,
              content: prompt.content,
              resultExample: prompt.resultExample ?? '',
              category: prompt.category,
              aiModel: prompt.aiModel,
            }}
            isSubmitting={isSubmitting}
            submitError={submitError}
            onSubmit={handleUpdate}
            onCancel={() => navigate(`/prompts/${prompt.id}`)}
          />
        </div>
      </section>
    </div>
  );
}
