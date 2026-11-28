export interface AiNewsListParams {
  keyword?: string;
  tag?: string;
  page?: number;
  size?: number;
}

export interface AiNewsCreateRequest {
  title: string;
  summary: string;
  originalUrl: string;
  sourceName: string;
  tags?: string;
  publishedAt?: string;
}

export interface AiNewsResponse {
  id: number;
  title: string;
  summary: string;
  originalUrl: string;
  sourceName: string;
  tags: string[];
  publishedAt: string;
  createdAt: string;
}

export interface AiNewsCollectResponse {
  totalFetched: number;
  savedCount: number;
  skippedCount: number;
  failedCount: number;
}

export interface AiVideoResponse {
  id: number;
  videoId: string;
  title: string;
  channelTitle: string;
  thumbnailUrl: string;
  publishedAt: string;
  videoUrl: string;
}
