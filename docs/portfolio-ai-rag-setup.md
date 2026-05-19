# Portfolio AI RAG Setup

포트폴리오 챗봇은 VectorRAG 기반으로 구성했습니다. 현재 데이터는 PDF 페이지 텍스트, 페이지별 작성자 설명, 포트폴리오 요약처럼 문서 조각 중심이고 기존 저장소도 PostgreSQL이므로, 별도 그래프 DB와 엔티티/관계 추출 파이프라인이 필요한 GraphRAG보다 pgvector 기반 VectorRAG가 이 프로젝트의 초기 제품 범위에 더 적합합니다.

## Environment

기본값은 AI 자동 구성을 모두 꺼둡니다. 실제 RAG를 사용할 환경에서만 아래 값을 설정합니다.

```properties
AI_CHAT_PROVIDER=openai
AI_EMBEDDING_PROVIDER=openai
AI_VECTOR_STORE_TYPE=pgvector
AI_VECTOR_INITIALIZE_SCHEMA=false
AI_VECTOR_DIMENSIONS=1536

OPENAI_API_KEY=...
OPENAI_CHAT_MODEL=gpt-4o-mini
OPENAI_EMBEDDING_MODEL=text-embedding-3-small
```

Gemini를 채팅 모델로 사용할 때:

```properties
AI_CHAT_PROVIDER=google-genai
GEMINI_API_KEY=...
GEMINI_CHAT_MODEL=gemini-2.0-flash
```

Ollama를 로컬 모델로 사용할 때:

```properties
AI_CHAT_PROVIDER=ollama
AI_EMBEDDING_PROVIDER=ollama
AI_VECTOR_STORE_TYPE=pgvector
AI_VECTOR_DIMENSIONS=768
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_CHAT_MODEL=llama3.1
OLLAMA_EMBEDDING_MODEL=nomic-embed-text
```

`AI_VECTOR_DIMENSIONS`는 사용하는 embedding 모델 차원과 `vector_store.embedding` 차원이 반드시 같아야 합니다. 기본 migration은 OpenAI `text-embedding-3-small` 기준인 1536 차원으로 생성합니다. 다른 차원을 쓰면 `vector_store` 테이블을 해당 차원으로 재생성하거나 `AI_VECTOR_INITIALIZE_SCHEMA=true`로 초기화하세요.

## Flow

1. 작성자가 PDF를 업로드하고 페이지별 작성자 설명을 저장합니다.
2. `POST /api/v1/portfolios/{portfolioId}/reindex`를 호출해 PDF 텍스트, 작성자 설명, 요약을 `vector_store`에 색인합니다.
3. 조회자가 `POST /api/v1/portfolios/{portfolioId}/chat-sessions`로 세션을 만듭니다.
4. 조회자가 `POST /api/v1/chat-sessions/{chatSessionId}/messages`로 질문합니다.
5. 서버는 pgvector 유사도 검색 결과만 근거로 LLM에 답변을 요청하고, 답변과 출처를 저장합니다.
6. 답변 근거가 없거나 모델이 답변 불가를 반환하면 질문을 `question_clusters`에 누적합니다.
7. 작성자는 `GET /api/v1/portfolios/{portfolioId}/question-insights`로 답변하지 못한 질문 묶음을 확인합니다.

## Notes

- 여러 LLM provider를 동시에 라우팅하기보다 Spring AI의 활성 `ChatModel` 하나를 환경 변수로 선택합니다. 포트폴리오 챗봇 API는 모델 제공자보다 답변 품질과 출처 저장을 우선합니다.
- GraphRAG가 필요한 시점은 "프로젝트-기술-역할-성과" 같은 명시적 관계를 추출하고, 관계 질의가 검색 품질의 병목이 될 때입니다. 현재 구조에서는 VectorRAG 색인 메타데이터에 `portfolioId`, `pageNumber`, `sourceType`을 넣는 방식이 단순하고 운영하기 쉽습니다.
