# 방탈출 예약 서비스 - 인증/인가

## API 명세

### 회원가입 - 일반 사용자

```
POST /members/normal/join
```

**요청**
```json
{
  "username": "user1",
  "password": "1234"
}
```

**응답** `201 Created`
```json
{
  "id": 1
}
```

---

### 회원가입 - 관리자

```
POST /members/admin/join
```

**요청**
```json
{
  "username": "admin1",
  "password": "1234"
}
```

**응답** `201 Created`
```json
{
  "id": 2
}
```

---

### 로그인

```
POST /members/login
```

**요청**
```json
{
  "username": "user1",
  "password": "1234"
}
```

**응답** `200 OK`

응답 헤더에 `Set-Cookie: JSESSIONID=...` 발급.
이후 요청에 쿠키를 포함해야 인증된 사용자로 처리된다.

---

### 예약 생성 `🔒 로그인 필요`

```
POST /reservations
```

**요청**
```json
{
  "date": "2026-06-01",
  "time": "10:00",
  "theme": "공포방"
}
```

**응답** `201 Created`
```json
{
  "id": 1
}
```

---

### 전체 예약 조회 `🔒 관리자만`

```
GET /admin/reservations
```

**응답** `200 OK`
```json
[
  {
    "id": 1,
    "name": "user1",
    "date": "2026-06-01",
    "startAt": "10:00",
    "theme": "공포방"
  }
]
```

---

## 에러 응답 형식

```json
{
  "code": "에러코드",
  "message": "에러 메시지"
}
```

| 상태코드 | code | 설명 |
|---------|------|------|
| 400 | `VALIDATION_FAILED` | 필수값 누락 또는 유효성 검증 실패 |
| 400 | `INVALID_REQUEST_FORMAT` | JSON 파싱 실패 |
| 400 | `INVALID_USERNAME_AND_PASSWORD` | 아이디 또는 비밀번호 불일치 |
| 401 | `UNAUTHORIZED_MEMBER` | 로그인 필요 |
| 403 | `FORBIDDEN` | 접근 권한 없음 (관리자 아님) |
| 404 | `NOT_FOUND` | 존재하지 않는 리소스 |
| 409 | `DUPLICATED_USERNAME` | 이미 존재하는 아이디 |
| 500 | `INTERNAL_SERVER_ERROR` | 서버 오류 |

---

## 인증/인가 구조

```
HTTP 요청
    ↓
[AuthInterceptor]
    ├── 세션 없음 → 401
    ├── /admin + ADMIN 아님 → 403
    └── 통과 → Controller
                  ↓
         @LoginMember Member
         (LoginMemberArgumentResolver가 세션에서 주입)
```

### 공개 URL (인증 불필요)

- `POST /members/login`
- `POST /members/normal/join`
- `POST /members/admin/join`
