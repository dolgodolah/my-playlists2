import { ApiError, apiRequest } from "@/lib/api";

export interface SignupMember {
  issuedCode: string;
  nickname: string;
}

export interface SessionMember {
  nickname: string;
}

const NICKNAME_MIN_LENGTH = 2;
const NICKNAME_MAX_LENGTH = 20;
const MEMBER_CODE_MAX_LENGTH = 64;

export function validateNickname(value: string): string | null {
  const trimmed = value.trim();

  if (trimmed.length < NICKNAME_MIN_LENGTH || trimmed.length > NICKNAME_MAX_LENGTH) {
    return `닉네임은 ${NICKNAME_MIN_LENGTH}~${NICKNAME_MAX_LENGTH}자로 입력해주세요.`;
  }

  const nicknamePattern = /^[a-zA-Z0-9가-힣_]+$/;
  if (!nicknamePattern.test(trimmed)) {
    return "닉네임은 한글/영문/숫자/_ 만 사용할 수 있습니다.";
  }

  return null;
}

export function validateMemberCode(value: string): string | null {
  const normalized = normalizeMemberCode(value);
  if (!normalized) {
    return "회원코드를 입력해주세요.";
  }

  if (normalized.length > MEMBER_CODE_MAX_LENGTH) {
    return `회원코드는 최대 ${MEMBER_CODE_MAX_LENGTH}자까지 입력할 수 있습니다.`;
  }

  return null;
}

export async function signupWithNickname(nickname: string): Promise<SignupMember> {
  const trimmedNickname = nickname.trim();
  const validationMessage = validateNickname(trimmedNickname);
  if (validationMessage) {
    throw new Error(validationMessage);
  }

  return request<SignupMember>("/api/v1/auth/signup", {
    method: "POST",
    body: JSON.stringify({ nickname: trimmedNickname }),
  });
}

export async function loginWithMemberCode(memberCode: string): Promise<SessionMember> {
  const normalized = normalizeMemberCode(memberCode);
  if (!normalized) {
    throw new Error("회원코드를 입력해주세요.");
  }

  return request<SessionMember>("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify({ memberCode: normalized }),
  }, { redirectOnUnauthorized: false });
}

export async function updateMyNickname(nickname: string): Promise<SessionMember> {
  const trimmedNickname = nickname.trim();
  const validationMessage = validateNickname(trimmedNickname);
  if (validationMessage) {
    throw new Error(validationMessage);
  }

  return request<SessionMember>("/api/v1/auth/me", {
    method: "PUT",
    body: JSON.stringify({ nickname: trimmedNickname }),
  });
}

export async function getCurrentSessionMember(): Promise<SessionMember> {
  return request<SessionMember>("/api/v1/auth/session", { method: "GET" });
}

export async function logoutSession() {
  try {
    await request<void>("/api/v1/auth/logout", { method: "POST" });
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      return;
    }
    throw error;
  }
}

export function normalizeMemberCode(value: string) {
  return value.trim().toUpperCase();
}

async function request<T>(
  path: string,
  init: RequestInit,
  options?: { redirectOnUnauthorized?: boolean },
): Promise<T> {
  return apiRequest<T>(path, init, "요청 처리에 실패했습니다.", options);
}
