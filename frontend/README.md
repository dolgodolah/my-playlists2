# Frontend (Standalone)

Next.js 프론트 서버와 Spring Boot 백엔드를 분리 운영하는 기준으로 구성했습니다.

## Scope (현재)

- 회원가입(닉네임 입력 + 회원코드 발급) 페이지: `/signup`
- 로그인(회원코드 입력) 페이지: `/login`
- 로그인 후 메인(플레이리스트 페이지 스캐폴드): `/`
- OAuth(구글/카카오) 버튼 제거

## Run

```bash
cd frontend
npm install
npm run dev
```

기본 URL: `http://localhost:3000`

## Env

`.env.example`를 참고해서 `.env.local`을 생성하세요.

```bash
cp .env.example .env.local
```

## Notes

- 멤버/닉네임/세션 관리는 백엔드 서버에서 처리합니다.
- `/`는 닉네임이 없으면 `/login`으로 리다이렉트됩니다.

## API Contract (현재 프론트 가정)

- `POST /api/v1/auth/signup`
  - request: `{ "nickname": "string" }`
  - response: `{ "memberCode": "string", "nickname": "string" }`
- `POST /api/v1/auth/login`
  - request: `{ "memberCode": "string" }`
  - response: `{ "memberCode": "string", "nickname": "string" }`
  - 서버는 세션 쿠키를 설정해야 함
- `GET /api/v1/auth/session`
  - response(로그인): `{ "memberCode": "string", "nickname": "string" }`
  - response(미로그인): `401`
- `POST /api/v1/auth/logout`
  - response: `204` 또는 `200`
