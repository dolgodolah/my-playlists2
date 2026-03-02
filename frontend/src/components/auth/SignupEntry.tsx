"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { loginWithMemberCode, signupWithNickname, validateNickname } from "@/lib/auth";
import { useRouter } from "next/navigation";

export default function SignupEntry() {
  const router = useRouter();
  const [nickname, setNickname] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [issuedCode, setIssuedCode] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const validationMessage = validateNickname(nickname);
    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      const member = await signupWithNickname(nickname);
      setIssuedCode(member.issuedCode);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
        return;
      }
      setErrorMessage("회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCopyCode = async () => {
    if (!issuedCode) {
      return;
    }
    await navigator.clipboard.writeText(issuedCode);
    alert("회원코드를 복사했습니다.");
  };

  const handleLoginNow = async () => {
    if (!issuedCode) {
      return;
    }
    try {
      await loginWithMemberCode(issuedCode);
      router.push("/");
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
        return;
      }
      setErrorMessage("로그인 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <main className="page-shell">
      <section className="panel-card">
        <h1 className="page-title">회원가입</h1>
        <p className="page-subtitle">닉네임을 입력하면 회원코드를 발급해드립니다.</p>

        <form className="form-grid" onSubmit={handleSubmit}>
          <label htmlFor="nickname" className="field-label">
            닉네임
          </label>
          <input
            id="nickname"
            name="nickname"
            className="field-input"
            autoComplete="nickname"
            placeholder="닉네임을 입력하세요"
            value={nickname}
            maxLength={20}
            onChange={(event) => setNickname(event.target.value)}
          />

          {errorMessage && <p className="field-error">{errorMessage}</p>}

          <button className="primary-button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "처리 중..." : "회원코드 발급"}
          </button>
        </form>

        {issuedCode && (
          <div className="issued-code-box">
            <p className="issued-code-label">발급된 회원코드</p>
            <p className="issued-code-value">{issuedCode}</p>
            <p className="issued-code-warning">
              회원코드는 본인 외에는 절대 노출하거나 공유하지 마세요. 이 코드로 언제든 다시 로그인할 수 있습니다.
            </p>
            <div className="form-actions">
              <button className="primary-button secondary-button" onClick={handleCopyCode} type="button">
                회원코드 복사
              </button>
              <button className="primary-button" onClick={handleLoginNow} type="button">
                이 코드로 로그인
              </button>
            </div>
          </div>
        )}

        <p className="footer-link">
          이미 회원코드가 있나요? <Link href="/login">로그인</Link>
        </p>
      </section>
    </main>
  );
}
