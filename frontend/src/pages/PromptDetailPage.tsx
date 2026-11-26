import { useParams } from 'react-router-dom';

export function PromptDetailPage() {
  const { id } = useParams<{ id: string }>();

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-8 shadow-xs">
      <h1 className="text-2xl font-bold text-slate-900">프롬프트 상세</h1>
      <p className="mt-2 text-sm text-slate-600">
        프롬프트 ID: <span className="font-mono font-semibold">{id}</span>
      </p>
    </section>
  );
}
