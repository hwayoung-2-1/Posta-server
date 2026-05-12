# Portfolio Chatbot ERD

포트폴리오 챗봇 서비스의 백엔드 ERD입니다. 모든 엔티티 식별자는 UUID를 사용합니다.

```mermaid
erDiagram
    USERS ||--o{ PORTFOLIOS : owns
    USERS ||--o{ CHAT_SESSIONS : starts
    USERS ||--o{ SAVED_PORTFOLIOS : saves
    USERS ||--o{ OWNER_NOTIFICATIONS : receives

    PORTFOLIOS ||--|| PORTFOLIO_FILES : has
    PORTFOLIOS ||--o{ PORTFOLIO_PAGES : contains
    PORTFOLIOS ||--o{ PORTFOLIO_CHUNKS : indexes
    PORTFOLIOS ||--o{ PORTFOLIO_SUMMARIES : has
    PORTFOLIOS ||--o{ SUGGESTED_QUESTIONS : has
    PORTFOLIOS ||--o{ PORTFOLIO_ROLES : tagged
    PORTFOLIOS ||--o{ PORTFOLIO_SKILLS : tagged
    PORTFOLIOS ||--o{ CHAT_SESSIONS : receives
    PORTFOLIOS ||--o{ QUESTION_CLUSTERS : analyzed_as
    PORTFOLIOS ||--o{ PORTFOLIO_INDEX_JOBS : processes
    PORTFOLIOS ||--o{ SAVED_PORTFOLIOS : saved_by

    PORTFOLIO_FILES ||--o{ PORTFOLIO_PAGES : renders
    PORTFOLIO_PAGES ||--o{ PAGE_OWNER_NOTES : has
    PORTFOLIO_PAGES ||--o{ PORTFOLIO_CHUNKS : source_of
    PORTFOLIO_PAGES ||--o{ SUGGESTED_QUESTIONS : suggests

    ROLES ||--o{ PORTFOLIO_ROLES : assigned
    SKILLS ||--o{ PORTFOLIO_SKILLS : assigned

    CHAT_SESSIONS ||--o{ CHAT_MESSAGES : contains
    CHAT_MESSAGES ||--o{ CHAT_MESSAGE_SOURCES : cites
    PORTFOLIO_CHUNKS ||--o{ CHAT_MESSAGE_SOURCES : used_by

    QUESTION_CLUSTERS ||--o{ QUESTION_CLUSTER_ITEMS : contains
    CHAT_MESSAGES ||--o{ QUESTION_CLUSTER_ITEMS : grouped_as
    QUESTION_CLUSTERS ||--o{ OWNER_NOTIFICATIONS : triggers

    USERS {
        uuid id PK
        string email UK
        string password
        string name
        string profile_image_url
        datetime created_at
        datetime updated_at
    }

    PORTFOLIOS {
        uuid id PK
        uuid user_id FK
        string title
        text description
        string visibility
        string status
        string public_slug UK
        string thumbnail_url
        datetime published_at
        datetime created_at
        datetime updated_at
    }

    PORTFOLIO_FILES {
        uuid id PK
        uuid portfolio_id FK
        string original_file_name
        string original_file_url
        string content_type
        bigint size_bytes
        int page_count
        string processing_status
        text failure_reason
        datetime created_at
        datetime updated_at
    }

    PORTFOLIO_PAGES {
        uuid id PK
        uuid portfolio_id FK
        uuid portfolio_file_id FK
        int page_number
        string page_image_url
        text extracted_text
        string extraction_status
        datetime created_at
        datetime updated_at
    }

    PAGE_OWNER_NOTES {
        uuid id PK
        uuid portfolio_page_id FK
        text content
        datetime created_at
        datetime updated_at
    }

    PORTFOLIO_CHUNKS {
        uuid id PK
        uuid portfolio_id FK
        uuid portfolio_page_id FK
        string source_type
        uuid source_id
        int chunk_index
        text content
        vector embedding
        int token_count
        datetime created_at
    }

    PORTFOLIO_SUMMARIES {
        uuid id PK
        uuid portfolio_id FK
        string summary_type
        text content
        datetime created_at
        datetime updated_at
    }

    SUGGESTED_QUESTIONS {
        uuid id PK
        uuid portfolio_id FK
        uuid portfolio_page_id FK
        string question
        string source
        int display_order
        datetime created_at
    }

    ROLES {
        uuid id PK
        string name UK
    }

    SKILLS {
        uuid id PK
        string name UK
    }

    PORTFOLIO_ROLES {
        uuid portfolio_id FK
        uuid role_id FK
    }

    PORTFOLIO_SKILLS {
        uuid portfolio_id FK
        uuid skill_id FK
    }

    CHAT_SESSIONS {
        uuid id PK
        uuid portfolio_id FK
        uuid viewer_user_id FK
        datetime created_at
        datetime last_message_at
    }

    CHAT_MESSAGES {
        uuid id PK
        uuid chat_session_id FK
        string role
        text content
        string moderation_status
        boolean analyzable
        datetime created_at
    }

    CHAT_MESSAGE_SOURCES {
        uuid id PK
        uuid chat_message_id FK
        uuid portfolio_chunk_id FK
        float score
        int page_number
        text snippet
        datetime created_at
    }

    QUESTION_CLUSTERS {
        uuid id PK
        uuid portfolio_id FK
        string title
        text summary
        string category
        int question_count
        string status
        datetime first_asked_at
        datetime last_asked_at
        datetime created_at
        datetime updated_at
    }

    QUESTION_CLUSTER_ITEMS {
        uuid id PK
        uuid question_cluster_id FK
        uuid chat_message_id FK
        float similarity_score
        datetime created_at
    }

    OWNER_NOTIFICATIONS {
        uuid id PK
        uuid user_id FK
        uuid portfolio_id FK
        uuid question_cluster_id FK
        string type
        string title
        text content
        boolean is_read
        datetime created_at
    }

    SAVED_PORTFOLIOS {
        uuid user_id FK
        uuid portfolio_id FK
        datetime created_at
    }

    PORTFOLIO_INDEX_JOBS {
        uuid id PK
        uuid portfolio_id FK
        string job_type
        string status
        text error_message
        datetime started_at
        datetime finished_at
        datetime created_at
    }
```

## 주요 Enum

| 구분 | 값 |
| --- | --- |
| `portfolio.status` | `DRAFT`, `PROCESSING`, `READY`, `PUBLISHED`, `FAILED`, `DELETED` |
| `portfolio.visibility` | `PRIVATE`, `PUBLIC`, `LINK_ONLY` |
| `portfolio_chunk.source_type` | `PDF_TEXT`, `OWNER_NOTE`, `SUMMARY` |
| `chat_message.role` | `USER`, `ASSISTANT`, `SYSTEM` |
| `moderation_status` | `PENDING`, `PASSED`, `BLOCKED` |
| `question_cluster.category` | `PROJECT_ROLE`, `TECH_STACK`, `CAREER`, `DESIGN_DECISION`, `IMPLEMENTATION_DETAIL`, `COLLABORATION`, `ACHIEVEMENT`, `UNCLEAR_PORTFOLIO_CONTENT`, `IRRELEVANT`, `ABUSIVE` |
| `question_cluster.status` | `OPEN`, `REVIEWED`, `RESOLVED`, `IGNORED` |
