"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { createPlaylist } from "@/lib/playlists";
import { isUnauthorizedError } from "@/lib/api";
import { getCurrentSessionMember } from "@/lib/auth";

export default function PlaylistAddPage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [visibility, setVisibility] = useState<"true" | "false">("true");
  const [isCheckingSession, setIsCheckingSession] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    getCurrentSessionMember()
      .catch((error) => {
        if (isUnauthorizedError(error)) {
          return;
        }
        setErrorMessage("로그인 상태를 확인하지 못했습니다.");
      })
      .finally(() => {
        setIsCheckingSession(false);
      });
  }, []);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      await createPlaylist({
        title,
        description,
        visibility: visibility === "true",
      });
      router.push("/");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        return;
      }
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("플레이리스트 생성 중 오류가 발생했습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="page-shell">
      <section className="panel-card playlist-card">
        <h1 className="page-title">플레이리스트 추가</h1>
        <p className="page-subtitle">타이틀, 공개 여부를 입력해 플레이리스트를 생성하세요. 소개는 선택사항입니다.</p>

        {isCheckingSession ? (
          <div className="playlist-placeholder">로그인 상태를 확인 중입니다...</div>
        ) : (
          <form className="form-grid" onSubmit={handleSubmit}>
          <label className="field-label" htmlFor="title">
            플레이리스트 타이틀
          </label>
          <input
            id="title"
            name="title"
            className="field-input"
            placeholder="플레이리스트 타이틀을 입력해 주세요."
            minLength={2}
            maxLength={50}
            required
            value={title}
            onChange={(event) => setTitle(event.target.value)}
          />

          <label className="field-label" htmlFor="description">
            플레이리스트 소개 (선택)
          </label>
          <textarea
            id="description"
            name="description"
            className="field-input field-textarea"
            placeholder="플레이리스트 소개를 입력해 주세요."
            maxLength={100}
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />

          <div className="visibility-group">
            <label className="visibility-option">
              <input
                type="radio"
                name="visibility"
                value="true"
                checked={visibility === "true"}
                onChange={(event) => setVisibility(event.target.value as "true" | "false")}
              />
              공개
            </label>
            <label className="visibility-option">
              <input
                type="radio"
                name="visibility"
                value="false"
                checked={visibility === "false"}
                onChange={(event) => setVisibility(event.target.value as "true" | "false")}
              />
              비공개
            </label>
          </div>

          {errorMessage && <p className="field-error">{errorMessage}</p>}

          <div className="form-actions form-actions-inline">
            <button className="primary-button secondary-button" type="button" onClick={() => router.back()}>
              취소
            </button>
            <button className="primary-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? "생성 중..." : "생성"}
            </button>
          </div>
          </form>
        )}
      </section>
    </main>
  );
}
