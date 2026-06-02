# Portfolio Chatbot API Spec

포트폴리오 챗봇 서비스 API 명세입니다.

## 공통

- Base URL: `/api/v1`
- 회원가입, 로그인, 공개 포트폴리오 조회 API는 `Authorization: Bearer {accessToken}` 없이 호출할 수 있습니다.
- 인증이 선택인 조회 API는 토큰이 있으면 현재 사용자 기준 정보(`saved`, 본인 private 포트폴리오 등)를 함께 반영합니다.
- 모든 엔티티 식별자는 UUID 문자열을 사용합니다.
- `page`, `size`는 0-based pagination 기준입니다.
- `PUBLIC`은 공개 조회 가능, `PRIVATE`은 작성자만 접근 가능한 상태입니다.
- 포트폴리오 목록의 `thumbnailUrl`은 업로드된 PDF 첫 페이지를 PNG로 렌더링한 MinIO presigned 다운로드 URL입니다.

## Auth

### 회원가입

`POST /api/v1/auth/signup`

```json
{
  "email": "owner@example.com",
  "password": "password1234",
  "name": "iamnot_tyler_1999"
}
```

`201 Created`

```json
{
  "userId": "00000000-0000-4000-8000-000000000001",
  "email": "owner@example.com",
  "name": "iamnot_tyler_1999"
}
```

### 로그인

`POST /api/v1/auth/login`

```json
{
  "email": "owner@example.com",
  "password": "password1234"
}
```

`200 OK`

```json
{
  "accessToken": "jwt-access-token",
  "tokenType": "Bearer"
}
```

## User

### 내 정보 조회

`GET /api/v1/users/me`

요청 body, query parameter는 사용하지 않습니다. 서버가 JWT 토큰의 subject로 현재 사용자를 식별합니다.

`200 OK`

```json
{
  "userId": "00000000-0000-4000-8000-000000000001",
  "email": "owner@example.com",
  "name": "iamnot_tyler_1999",
  "profileImageUrl": "https://cdn.example.com/users/00000000-0000-4000-8000-000000000001.png",
  "portfolioCount": 2
}
```

### 내 정보 수정

`PATCH /api/v1/users/me`

```json
{
  "name": "new_name",
  "profileImageUrl": "https://cdn.example.com/users/00000000-0000-4000-8000-000000000001-new.png"
}
```

`200 OK`

```json
{
  "userId": "00000000-0000-4000-8000-000000000001",
  "name": "new_name",
  "profileImageUrl": "https://cdn.example.com/users/00000000-0000-4000-8000-000000000001-new.png"
}
```

## Taxonomy

### 직군 목록 조회

`GET /api/v1/roles`

`200 OK`

```json
{
  "roles": [
    {
      "roleId": "10000000-0000-4000-8000-000000000001",
      "name": "Front-End"
    },
    {
      "roleId": "10000000-0000-4000-8000-000000000002",
      "name": "UX/UI Design"
    }
  ]
}
```

### 기술 목록 조회

`GET /api/v1/skills`

`200 OK`

```json
{
  "skills": [
    {
      "skillId": "20000000-0000-4000-8000-000000000001",
      "name": "React"
    },
    {
      "skillId": "20000000-0000-4000-8000-000000000002",
      "name": "Figma"
    }
  ]
}
```

## Portfolio

### 포트폴리오 업로드

`POST /api/v1/portfolios`

`multipart/form-data`

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `file` | `file` | Y | PDF 포트폴리오 |
| `title` | `string` | Y | 포트폴리오 제목 |
| `description` | `string` | N | 간단 소개 |
| `visibility` | `string` | N | `PRIVATE`, `PUBLIC`, `LINK_ONLY` |
| `roleIds` | `uuid[]` | N | 포트폴리오 직군 |
| `skillIds` | `uuid[]` | N | 포트폴리오 기술 |

업로드 시 PDF 첫 페이지를 PNG로 렌더링해 MinIO에 저장합니다. 포트폴리오 목록 조회의 `thumbnailUrl`은 이 첫 페이지 이미지 다운로드 URL입니다.

`201 Created`

```json
{
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "status": "READY",
  "fileUrl": "https://minio.example.com/hwayoung-portfolios/portfolios/30000000-0000-4000-8000-000000000010/original.pdf?X-Amz-Expires=3600",
  "processingStatusUrl": "/api/v1/portfolios/30000000-0000-4000-8000-000000000010/processing-status"
}
```

### PDF 포트폴리오 업로드

`POST /api/v1/portfolios/pdf`

`multipart/form-data`

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `file` | `file` | Y | PDF 포트폴리오 |
| `title` | `string` | Y | 포트폴리오 제목 |
| `description` | `string` | N | 간단 소개 |
| `visibility` | `string` | N | `public` 또는 `private` |

업로드 시 PDF 첫 페이지를 PNG로 렌더링해 MinIO에 저장합니다. 포트폴리오 목록 조회의 `thumbnailUrl`은 이 첫 페이지 이미지 다운로드 URL입니다.

`201 Created`

```json
{
  "id": "30000000-0000-4000-8000-000000000010",
  "title": "UX/UI Designer Portfolio",
  "description": "UX/UI 중심 포트폴리오입니다.",
  "visibility": "public",
  "pageCount": 8,
  "pdf": {
    "originalFilename": "portfolio.pdf",
    "contentType": "application/pdf",
    "size": 1048576
  },
  "likeCount": 0,
  "commentCount": 0,
  "createdAt": "2026-04-28T12:00:00",
  "updatedAt": "2026-04-28T12:00:00"
}
```

### 포트폴리오 목록 조회

`GET /api/v1/portfolios?page&size&role&skill&name&keyword`

인증은 선택입니다.

- 토큰이 있으면 요청자 본인 포트폴리오는 제외하고, 다른 사용자의 공개 포트폴리오만 반환합니다.
- 토큰이 없으면 공개 포트폴리오만 반환합니다.
- 포트폴리오는 `visibility=PUBLIC`이고 `status=READY` 또는 `status=PUBLISHED`인 경우 목록에 포함됩니다.

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `page` | `number` | N | 페이지 번호 |
| `size` | `number` | N | 페이지 크기 |
| `role` | `string` | N | 직군 필터 |
| `skill` | `string` | N | 기술 필터 |
| `name` | `string` | N | 작성자 이름 검색 |
| `keyword` | `string` | N | 제목/설명 검색 |

`200 OK`

```json
{
  "content": [
    {
      "portfolioId": "30000000-0000-4000-8000-000000000010",
      "title": "UX/UI Designer Portfolio",
      "ownerName": "iamnot_tyler_1999",
      "thumbnailUrl": "https://minio.example.com/hwayoung-portfolios/portfolios/00000000-0000-4000-8000-000000000001/30000000-0000-4000-8000-000000000010/first-page.png?X-Amz-Expires=600",
      "roles": ["UX/UI Design"],
      "skills": ["Figma", "Illustrator", "Photoshop"],
      "saved": false
    }
  ],
  "page": 0,
  "size": 12,
  "totalElements": 42,
  "totalPages": 4
}
```

### 포트폴리오 상세 조회

`GET /api/v1/portfolios/{portfolioId}`

인증은 선택입니다. `PUBLIC` 포트폴리오는 비로그인 사용자도 조회할 수 있고, `PRIVATE` 포트폴리오는 작성자만 조회할 수 있습니다.

`200 OK`

```json
{
  "id": "30000000-0000-4000-8000-000000000010",
  "ownerId": "00000000-0000-4000-8000-000000000001",
  "title": "UX/UI Designer Portfolio",
  "description": "UX/UI 중심 포트폴리오입니다.",
  "visibility": "public",
  "pageCount": 8,
  "pdf": {
    "originalFilename": "portfolio.pdf",
    "contentType": "application/pdf",
    "size": 1048576
  },
  "likeCount": 0,
  "commentCount": 0,
  "createdAt": "2026-04-28T12:00:00",
  "updatedAt": "2026-04-28T12:00:00"
}
```

### 포트폴리오 수정

`PATCH /api/v1/portfolios/{portfolioId}`

```json
{
  "title": "Updated Portfolio",
  "description": "수정된 소개",
  "visibility": "LINK_ONLY",
  "roleIds": ["10000000-0000-4000-8000-000000000002"],
  "skillIds": [
    "20000000-0000-4000-8000-000000000002",
    "20000000-0000-4000-8000-000000000003",
    "20000000-0000-4000-8000-000000000004"
  ]
}
```

`200 OK`

```json
{
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "title": "Updated Portfolio",
  "visibility": "LINK_ONLY",
  "status": "READY"
}
```

### 포트폴리오 삭제

`DELETE /api/v1/portfolios/{portfolioId}`

작성자만 삭제할 수 있습니다. 삭제 시 DB 포트폴리오 데이터와 MinIO 원본 PDF, 첫 페이지 썸네일 object가 함께 삭제됩니다.

`204 No Content`

### 포트폴리오 게시

`POST /api/v1/portfolios/{portfolioId}/publish`

`200 OK`

```json
{
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "status": "PUBLISHED",
  "publicSlug": "ux-ui-designer-portfolio-a1b2c3",
  "publishedAt": "2026-04-28T12:00:00"
}
```

### 처리 상태 조회

`GET /api/v1/portfolios/{portfolioId}/processing-status`

`200 OK`

```json
{
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "status": "PROCESSING",
  "steps": [
    {
      "type": "PDF_RENDER",
      "status": "DONE"
    },
    {
      "type": "TEXT_EXTRACTION",
      "status": "RUNNING"
    },
    {
      "type": "RAG_INDEX",
      "status": "PENDING"
    }
  ],
  "failureReason": null
}
```

## Portfolio Page & Owner Note

### 페이지 목록 조회

`GET /api/v1/portfolios/{portfolioId}/pages`

`200 OK`

```json
{
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "pages": [
    {
      "pageId": "40000000-0000-4000-8000-000000000100",
      "pageNumber": 0,
      "pageImageUrl": "https://cdn.example.com/portfolios/30000000-0000-4000-8000-000000000010/pages/0.png",
      "hasOwnerNote": true
    }
  ]
}
```

### 페이지 상세 조회

`GET /api/v1/portfolios/{portfolioId}/pages/{pageNumber}`

`200 OK`

```json
{
  "pageId": "40000000-0000-4000-8000-000000000100",
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "pageNumber": 0,
  "pageImageUrl": "https://cdn.example.com/portfolios/30000000-0000-4000-8000-000000000010/pages/0.png",
  "extractedText": "PDF에서 추출된 텍스트",
  "ownerNote": "이 페이지에서 가장 말하고 싶은 추가 설명입니다."
}
```

### 페이지별 작성자 추가 설명 저장

`PATCH /api/v1/portfolios/{portfolioId}/pages/{pageNumber}/owner-note`

```json
{
  "content": "이 프로젝트에서 저는 문제 정의와 사용자 인터뷰 설계를 주도했습니다."
}
```

`200 OK`

```json
{
  "pageId": "40000000-0000-4000-8000-000000000100",
  "pageNumber": 0,
  "ownerNote": "이 프로젝트에서 저는 문제 정의와 사용자 인터뷰 설계를 주도했습니다.",
  "reindexStatus": "SCHEDULED"
}
```

## RAG & Summary

### RAG 재색인 요청

`POST /api/v1/portfolios/{portfolioId}/reindex`

PDF 텍스트, 작성자 메모, 요약 chunk를 다시 생성하고 embedding을 갱신합니다.

`202 Accepted`

```json
{
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "jobId": "50000000-0000-4000-8000-000000000500",
  "status": "PENDING"
}
```

### 포트폴리오 요약 조회

`GET /api/v1/portfolios/{portfolioId}/summary`

`200 OK`

```json
{
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "summaryType": "SHORT",
  "content": "UX/UI 디자인 역량과 Figma 기반 프로젝트 경험을 중심으로 구성된 포트폴리오입니다."
}
```

### 추천 질문 조회

`GET /api/v1/portfolios/{portfolioId}/suggested-questions`

`200 OK`

```json
{
  "questions": [
    {
      "questionId": "60000000-0000-4000-8000-000000000001",
      "pageNumber": 0,
      "question": "이 프로젝트에서 맡은 역할은 무엇인가요?"
    }
  ]
}
```

## Chatbot

페이지별 참고 텍스트(`PortfolioContext`)는 챗봇/RAG 답변 근거 데이터이므로 API 문서에서는 챗봇 기능 도메인에 포함합니다.

### 챗봇 세션 생성

`POST /api/v1/portfolios/{portfolioId}/chat-sessions`

요청 본문은 필요 없습니다. 서버는 인증 토큰의 사용자 ID를 `viewer_user_id`로 저장합니다.

`201 Created`

```json
{
  "chatSessionId": "70000000-0000-4000-8000-000000001000",
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "viewerUserId": "00000000-0000-4000-8000-000000000002",
  "createdAt": "2026-04-28T12:00:00"
}
```

### 대화 내역 조회

`GET /api/v1/chat-sessions/{chatSessionId}/messages`

`200 OK`

```json
{
  "chatSessionId": "70000000-0000-4000-8000-000000001000",
  "messages": [
    {
      "messageId": "80000000-0000-4000-8000-000000000001",
      "role": "USER",
      "content": "이 프로젝트에서 뭘 담당했나요?",
      "createdAt": "2026-04-28T12:00:00"
    },
    {
      "messageId": "80000000-0000-4000-8000-000000000002",
      "role": "ASSISTANT",
      "content": "작성자는 문제 정의와 사용자 인터뷰 설계를 주도했습니다.",
      "sources": [
        {
          "pageNumber": 0,
          "sourceType": "OWNER_NOTE",
          "snippet": "문제 정의와 사용자 인터뷰 설계를 주도했습니다.",
          "score": 0.91
        }
      ],
      "createdAt": "2026-04-28T12:00:02"
    }
  ]
}
```

### 질문 전송

`POST /api/v1/chat-sessions/{chatSessionId}/messages`

```json
{
  "message": "이 프로젝트에서 정확히 어떤 역할을 맡았나요?",
  "currentPage": 0
}
```

`200 OK`

```json
{
  "questionMessageId": "80000000-0000-4000-8000-000000000010",
  "answerMessageId": "80000000-0000-4000-8000-000000000011",
  "answer": "이 페이지 기준으로 작성자는 문제 정의와 사용자 인터뷰 설계를 주도했고, Figma를 활용해 주요 화면 설계를 진행했습니다.",
  "sources": [
    {
      "chunkId": "90000000-0000-4000-8000-000000000300",
      "pageNumber": 0,
      "sourceType": "OWNER_NOTE",
      "snippet": "문제 정의와 사용자 인터뷰 설계를 주도했습니다.",
      "score": 0.91
    },
    {
      "chunkId": "90000000-0000-4000-8000-000000000301",
      "pageNumber": 0,
      "sourceType": "PDF_TEXT",
      "snippet": "Figma 기반 주요 화면 설계",
      "score": 0.82
    }
  ],
  "questionAnalysisStatus": "SCHEDULED"
}
```

## Question Insight

### 질문 인사이트 목록 조회

`GET /api/v1/portfolios/{portfolioId}/question-insights?status&category&page&size`

`200 OK`

```json
{
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "totalQuestions": 128,
  "clusters": [
    {
      "clusterId": "a0000000-0000-4000-8000-000000000020",
      "title": "프로젝트에서 맡은 역할",
      "summary": "조회자들이 작성자가 실제로 담당한 범위와 기여도를 자주 질문했습니다.",
      "category": "PROJECT_ROLE",
      "questionCount": 34,
      "status": "OPEN",
      "sampleQuestions": [
        "이 프로젝트에서 정확히 뭘 맡았나요?",
        "디자인만 한 건가요, 개발도 했나요?"
      ],
      "firstAskedAt": "2026-04-20T12:00:00",
      "lastAskedAt": "2026-04-28T12:00:00"
    }
  ]
}
```

### 질문 인사이트 상세 조회

`GET /api/v1/portfolios/{portfolioId}/question-insights/{clusterId}`

`200 OK`

```json
{
  "clusterId": "a0000000-0000-4000-8000-000000000020",
  "title": "프로젝트에서 맡은 역할",
  "summary": "역할과 기여도에 대한 질문이 반복적으로 발생했습니다.",
  "category": "PROJECT_ROLE",
  "questionCount": 34,
  "status": "OPEN",
  "questions": [
    {
      "messageId": "80000000-0000-4000-8000-000000000010",
      "content": "이 프로젝트에서 정확히 어떤 역할을 맡았나요?",
      "askedAt": "2026-04-28T12:00:00"
    }
  ],
  "recommendedAction": "해당 프로젝트 페이지의 작성자 추가 설명에 본인 역할과 결과물을 더 명확히 추가하는 것이 좋습니다."
}
```

### 질문 인사이트 상태 변경

`PATCH /api/v1/portfolios/{portfolioId}/question-insights/{clusterId}`

```json
{
  "status": "RESOLVED"
}
```

`200 OK`

```json
{
  "clusterId": "a0000000-0000-4000-8000-000000000020",
  "status": "RESOLVED"
}
```

### 질문 인사이트 재분석 요청

`POST /api/v1/portfolios/{portfolioId}/question-insights/rebuild`

`202 Accepted`

```json
{
  "portfolioId": "30000000-0000-4000-8000-000000000010",
  "jobId": "50000000-0000-4000-8000-000000000700",
  "status": "PENDING"
}
```

## Notification

### 내 알림 조회

`GET /api/v1/users/me/notifications?unreadOnly&page&size`

`200 OK`

```json
{
  "content": [
    {
      "notificationId": "b0000000-0000-4000-8000-000000000001",
      "portfolioId": "30000000-0000-4000-8000-000000000010",
      "questionClusterId": "a0000000-0000-4000-8000-000000000020",
      "type": "FREQUENT_QUESTION",
      "title": "프로젝트 역할 질문이 자주 들어오고 있어요",
      "content": "최근 이 포트폴리오에서 역할과 기여도 관련 질문이 34번 발생했습니다.",
      "read": false,
      "createdAt": "2026-04-28T12:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1
}
```

### 알림 읽음 처리

`PATCH /api/v1/users/me/notifications/{notificationId}/read`

`204 No Content`

## Saved Portfolio

### 포트폴리오 저장

`POST /api/v1/portfolios/{portfolioId}/save`

`204 No Content`

### 포트폴리오 저장 취소

`DELETE /api/v1/portfolios/{portfolioId}/save`

`204 No Content`

### 저장한 포트폴리오 목록 조회

`GET /api/v1/users/me/saved-portfolios?page&size`

`200 OK`

```json
{
  "content": [
    {
      "portfolioId": "30000000-0000-4000-8000-000000000010",
      "title": "UX/UI Designer Portfolio",
      "ownerName": "iamnot_tyler_1999",
      "thumbnailUrl": "https://minio.example.com/hwayoung-portfolios/portfolios/00000000-0000-4000-8000-000000000001/30000000-0000-4000-8000-000000000010/first-page.png?X-Amz-Expires=600",
      "savedAt": "2026-04-28T12:00:00"
    }
  ],
  "page": 0,
  "size": 12,
  "totalElements": 1
}
```

## Error Response

```json
{
  "status": 400,
  "code": "INVALID_REQUEST",
  "message": "요청 값이 올바르지 않습니다."
}
```

| Status | Code | 설명 |
| --- | --- | --- |
| `400` | `INVALID_REQUEST` | 요청 값 오류 |
| `401` | `UNAUTHORIZED` | 인증 실패 |
| `403` | `FORBIDDEN` | 포트폴리오 소유자 또는 접근 권한 없음 |
| `404` | `USER_NOT_FOUND` | 유저 없음 |
| `404` | `PORTFOLIO_NOT_FOUND` | 포트폴리오 없음 |
| `404` | `CHAT_SESSION_NOT_FOUND` | 챗봇 세션 없음 |
| `409` | `PORTFOLIO_NOT_READY` | 처리 완료 전 접근 |
| `422` | `UNSUPPORTED_FILE_TYPE` | 지원하지 않는 파일 형식 |
