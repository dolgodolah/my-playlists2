"use client";

import { FormEvent, MouseEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getCurrentSessionMember, logoutSession, updateMyNickname } from "@/lib/auth";
import { isUnauthorizedError } from "@/lib/api";

export default function MePage() {
  const router = useRouter();
  const [nickname, setNickname] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    getCurrentSessionMember()
      .then((member) => {
        setNickname(member.nickname);
      })
      .catch((error) => {
        if (isUnauthorizedError(error)) {
          return;
        }
        setErrorMessage("내 정보를 불러오지 못했습니다.");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, []);

  const handleLogout = (event: MouseEvent<HTMLAnchorElement>) => {
    event.preventDefault();
    logoutSession().finally(() => {
      router.replace("/login");
    });
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);
    setIsSaving(true);

    try {
      const updated = await updateMyNickname(nickname);
      setNickname(updated.nickname);
      setSuccessMessage("닉네임이 변경되었습니다.");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        return;
      }
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("닉네임 변경 중 오류가 발생했습니다.");
      }
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <main className="page-shell">
      <section className="panel-card playlist-card">
        <h1 className="page-title">내 정보</h1>
        <p className="page-subtitle">닉네임을 수정할 수 있습니다.</p>

        {isLoading ? (
          <div className="playlist-placeholder">사용자 정보를 확인 중입니다...</div>
        ) : (
          <form className="form-grid" onSubmit={handleSubmit}>
            <label htmlFor="nickname" className="field-label">
              닉네임
            </label>
            <input
              id="nickname"
              name="nickname"
              className="field-input"
              value={nickname}
              maxLength={20}
              onChange={(event) => setNickname(event.target.value)}
            />

            {errorMessage && <p className="field-error">{errorMessage}</p>}
            {successMessage && <p className="field-success">{successMessage}</p>}

            <button className="primary-button" type="submit" disabled={isSaving}>
              {isSaving ? "저장 중..." : "저장"}
            </button>
          </form>
        )}

        <div className="account-links">
          <a className="account-link" href="/">
            플레이리스트로 이동
          </a>
          <a className="account-link" href="/login" onClick={handleLogout}>
            로그아웃
          </a>
        </div>
      </section>
    </main>
  );
}
