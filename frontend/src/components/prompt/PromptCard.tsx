import { Link } from 'react-router-dom';
import { Bookmark, Copy, Heart } from 'lucide-react';
import {
  AI_MODEL_LABEL_MAP,
  CATEGORY_LABEL_MAP,
  formatDateTime,
} from '../../constants/promptOptions';
import type { PromptResponse } from '../../types/prompt';

interface PromptCardProps {
  prompt: PromptResponse;
  onToggleLike?: (promptId: number) => void;
  onToggleBookmark?: (promptId: number) => void;
  isLikePending?: boolean;
  isBookmarkPending?: boolean;
}

export function PromptCard({
  prompt,
  onToggleLike,
  onToggleBookmark,
  isLikePending = false,
  isBookmarkPending = false,
}: PromptCardProps) {
  return (
    <article className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:border-indigo-200 hover:shadow-md">
      <Link to={`/prompts/${prompt.id}`} className="block flex-1">
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
      </Link>

      <div className="mt-5 flex items-center justify-between border-t border-slate-100 pt-3 text-xs text-slate-500">
        <div className="flex items-center gap-1.5 truncate">
          <span className="truncate font-medium text-slate-700">
            {prompt.authorNickname}
          </span>
          <span>·</span>
          <span className="shrink-0">{formatDateTime(prompt.createdAt)}</span>
        </div>

        <div className="flex shrink-0 items-center gap-2.5">
          {onToggleLike ? (
            <button
              type="button"
              onClick={() => onToggleLike(prompt.id)}
              disabled={isLikePending}
              aria-label={prompt.liked ? '좋아요 취소' : '좋아요'}
              className="inline-flex cursor-pointer items-center gap-1 rounded-md px-1 py-0.5 transition-colors hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <Heart
                className={`h-3.5 w-3.5 ${
                  prompt.liked
                    ? 'fill-rose-500 text-rose-500'
                    : 'text-slate-400'
                }`}
              />
              <span>{prompt.likeCount}</span>
            </button>
          ) : (
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
          )}

          {onToggleBookmark ? (
            <button
              type="button"
              onClick={() => onToggleBookmark(prompt.id)}
              disabled={isBookmarkPending}
              aria-label={prompt.bookmarked ? '북마크 해제' : '북마크'}
              className="inline-flex cursor-pointer items-center gap-1 rounded-md px-1 py-0.5 transition-colors hover:bg-amber-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <Bookmark
                className={`h-3.5 w-3.5 ${
                  prompt.bookmarked
                    ? 'fill-amber-500 text-amber-500'
                    : 'text-slate-400'
                }`}
              />
              <span>{prompt.bookmarkCount}</span>
            </button>
          ) : (
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
          )}

          <span className="inline-flex items-center gap-1">
            <Copy className="h-3.5 w-3.5 text-slate-400" />
            {prompt.copyCount}
          </span>
        </div>
      </div>
    </article>
  );
}
