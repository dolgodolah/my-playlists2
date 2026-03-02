const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

interface ApiRequestOptions {
  redirectOnUnauthorized?: boolean;
}

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

export class UnauthorizedError extends ApiError {}

export function isUnauthorizedError(error: unknown): error is UnauthorizedError {
  return error instanceof UnauthorizedError;
}

export async function apiRequest<T>(
  path: string,
  init: RequestInit,
  fallbackMessage: string,
  options?: ApiRequestOptions,
): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(init.headers ?? {}),
    },
  });

  if (res.status === 204) {
    return undefined as T;
  }

  const data = await parseJsonSafely(res);
  if (!res.ok) {
    const message = extractErrorMessage(data, fallbackMessage);
    if (res.status === 401) {
      if (options?.redirectOnUnauthorized !== false && typeof window !== "undefined") {
        window.location.href = "/login";
      }
      throw new UnauthorizedError(message, res.status);
    }
    throw new ApiError(message, res.status);
  }

  return data as T;
}

async function parseJsonSafely(res: Response): Promise<unknown> {
  const text = await res.text();
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

function extractErrorMessage(data: unknown, fallback: string): string {
  if (!data || typeof data !== "object") {
    return fallback;
  }

  const candidate = data as Record<string, unknown>;
  if (typeof candidate.message === "string") {
    return candidate.message;
  }
  if (typeof candidate.detail === "string") {
    return candidate.detail;
  }
  if (typeof candidate.error === "string") {
    return candidate.error;
  }
  return fallback;
}
