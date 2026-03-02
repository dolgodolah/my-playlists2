# My Playlists Rebuild

Monorepo for the My Playlists rebuilding project.

## Tech spec

- Backend: Java 21, Kotlin, Spring Boot 4
- Frontend: Next.js 16, React 19, TypeScript 5
- AI-assisted development: OpenAI Codex (GPT-5 based coding agent)
- Collaboration scope: 요구사항 해석, 코드 구현/리팩토링, 보안 점검(시크릿 스캔), 문서화(README)

## Structure

- `backend(root)`: Spring Boot API (`/api/v1`)
- `frontend`: Next.js web app

## Quick start

1. Copy env files.
2. Start local infra.
3. Run backend and frontend.

```bash
./gradlew bootRun
cd frontend && npm install && npm run dev
```

## API conventions

- Base path: `/api/v1`
- Error shape: `{ "message": "..." }`

## Auth API (temporary in-memory)

- `POST /api/v1/auth/signup`
  - request: `{ "nickname": "tester_01" }`
  - response: `{ "memberCode": "AB12...", "nickname": "tester_01" }`
  - behavior: 서버 메모리(HashMap)에 회원 저장
- `POST /api/v1/auth/login`
  - request: `{ "memberCode": "AB12..." }`
  - response: `{ "memberCode": "...", "nickname": "..." }`
  - behavior: 성공 시 `JSESSIONID` 세션 쿠키 발급/갱신
- `POST /api/v1/auth/logout`
  - request body: 없음
  - response: `204 No Content`
  - behavior: 세션 무효화
- `GET /api/v1/auth/session` (frontend 호환용)
  - response: 로그인 시 member 정보, 미로그인 시 `401`

## Auth Architecture (Hexagonal)

- `domain`
  - `domain/auth/Member.kt`
- `usecase`
  - `usecase/auth/SignupUseCase.kt`
  - `usecase/auth/LoginUseCase.kt`
  - `usecase/auth/GetSessionMemberUseCase.kt`
  - `usecase/auth/port/out/MemberRepository.kt`
  - `usecase/auth/port/out/MemberCodeGenerator.kt`
- `adapter`
  - in(web): `adapter/in/web/auth/AuthController.kt`
  - in(web-common): `adapter/in/web/common/GlobalExceptionHandler.kt`, `adapter/in/web/config/WebConfig.kt`
  - out(memory): `adapter/out/persistence/memory/auth/InMemoryMemberRepository.kt`
  - out(generator): `adapter/out/generator/auth/SecureRandomMemberCodeGenerator.kt`

현재는 out adapter가 메모리 구현이며, MySQL 전환 시 `MemberRepository` 구현체만 교체해 API 스펙을 유지할 수 있습니다.
