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
