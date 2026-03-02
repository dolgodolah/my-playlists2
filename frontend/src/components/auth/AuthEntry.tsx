"use client";

import Link from "next/link";
import { FormEvent, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { loginWithMemberCode, normalizeMemberCode, validateMemberCode } from "@/lib/auth";

export default function AuthEntry() {
  const router = useRouter();
  const [memberCode, setMemberCode] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const pageTitle = "시작하기";
  const pageSubtitle = "회원코드를 입력하고 로그인하세요.";
  const submitLabel = "로그인";

  const normalizedMemberCode = useMemo(() => normalizeMemberCode(memberCode), [memberCode]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const validationMessage = validateMemberCode(normalizedMemberCode);
    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      await loginWithMemberCode(normalizedMemberCode);
      router.push("/");
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("로그인 중 오류가 발생했습니다. 다시 시도해주세요.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="page-shell">
      <section className="panel-card">
        <h1 className="page-title">{pageTitle}</h1>
        <p className="page-subtitle">{pageSubtitle}</p>

        <form className="form-grid" onSubmit={handleSubmit}>
          <label htmlFor="memberCode" className="field-label">
            회원코드
          </label>
          <input
            id="memberCode"
            name="memberCode"
            className="field-input"
            autoComplete="one-time-code"
            placeholder="발급받은 회원코드를 입력하세요"
            value={memberCode}
            maxLength={12}
            onChange={(event) => setMemberCode(event.target.value)}
          />

          {errorMessage && <p className="field-error">{errorMessage}</p>}

          <button className="primary-button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "처리 중..." : submitLabel}
          </button>
        </form>

        <p className="footer-link">
          회원코드가 없나요? <Link href="/signup">회원가입</Link>
        </p>
      </section>
    </main>
  );
}
