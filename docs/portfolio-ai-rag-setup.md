# Portfolio AI RAG Setup

포트폴리오 챗봇은 VectorRAG 기반으로 구성했습니다. 답변 근거는 작성자가 업로드하거나 직접 입력한 포트폴리오 데이터로 제한합니다. 현재 데이터는 PDF 페이지 텍스트, 페이지별 참고 텍스트, 페이지별 작성자 메모처럼 문서 조각 중심이고 기존 저장소도 PostgreSQL이므로, 별도 그래프 DB와 엔티티/관계 추출 파이프라인이 필요한 GraphRAG보다 pgvector 기반 VectorRAG가 이 프로젝트의 초기 제품 범위에 더 적합합니다.

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
OLLAMA_CHAT_MODEL=gemma4:e2b
OLLAMA_EMBEDDING_MODEL=nomic-embed-text
```

로컬 머신에서 먼저 Ollama 모델을 준비합니다.

```bash
ollama pull gemma4:e2b
ollama pull nomic-embed-text
ollama run gemma4:e2b
```

`AI_VECTOR_DIMENSIONS`는 Spring AI가 스키마를 직접 초기화할 때 사용하는 값입니다. Flyway migration은 `vector_store.embedding`을 차원 고정 없는 `VECTOR`로 변경하므로 OpenAI 1536차원과 Ollama `nomic-embed-text` 768차원 중 하나를 선택해 운영할 수 있습니다. embedding provider를 바꾼 뒤에는 기존 색인과 차원이 섞이지 않도록 포트폴리오를 다시 reindex하세요.

## Flow

1. 작성자가 PDF를 업로드하고 페이지별 참고 텍스트와 작성자 메모를 저장합니다.
2. `POST /api/v1/portfolios/{portfolioId}/reindex`를 호출해 작성자 이름, 직군/기술 태그, 포트폴리오 설명, PDF 텍스트, 페이지별 참고 텍스트, 작성자 메모를 `vector_store`에 색인합니다.
3. 조회자가 `POST /api/v1/portfolios/{portfolioId}/chat-sessions`로 세션을 만듭니다.
4. 조회자가 `POST /api/v1/chat-sessions/{chatSessionId}/messages`로 질문합니다.
5. 서버는 pgvector 유사도 검색 결과와 현재 페이지의 입력 데이터를 근거로 LLM에 답변을 요청하고, 답변과 출처를 저장합니다.
6. 답변 근거가 없거나 모델이 답변 불가를 반환하면 질문을 `question_clusters`에 누적합니다.
7. 작성자는 `GET /api/v1/portfolios/{portfolioId}/question-insights`로 답변하지 못한 질문 묶음을 확인합니다.

## Notes

- 여러 LLM provider를 동시에 라우팅하기보다 Spring AI의 활성 `ChatModel` 하나를 환경 변수로 선택합니다. 포트폴리오 챗봇 API는 모델 제공자보다 답변 품질과 출처 저장을 우선합니다.
- GraphRAG가 필요한 시점은 "프로젝트-기술-역할-성과" 같은 명시적 관계를 추출하고, 관계 질의가 검색 품질의 병목이 될 때입니다. 현재 구조에서는 VectorRAG 색인 메타데이터에 `portfolioId`, `pageNumber`, `sourceType`을 넣는 방식이 단순하고 운영하기 쉽습니다.
