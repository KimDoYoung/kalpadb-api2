# dairy 테이블에 대한 설계

## dairy 테이블

1. CREATE (생성)

    POST /api/dairy
    Content-Type: application/json
    {
    "ymd": "20260106",
    "content": "오늘의 일기 내용...",
    "summary": "오늘의 요약"
    }
2. READ (조회)

    GET /api/dairy/{ymd}                    -- 특정 날짜 일기 조회
    GET /api/dairy                          -- 모든 일기 조회 (페이징)
    파라미터:
        - page=0              -- 페이지 번호 (0부터 시작)
        - size=10             -- 한 페이지 크기 (기본값: 10)
        - sort=ymd,desc       -- 정렬 (ymd,desc / ymd,asc)
        - fromYmd=20260101    -- 시작 날짜
        - toYmd=20260131      -- 종료 날짜
        - keyword=검색어      -- 내용/요약 검색어 (선택)
        - summaryOnly=true  -- 요약만
    GET /api/dairy?fromYmd=20260101&toYmd=20260131  -- 날짜 범위 조회
    파라메터
        - summaryOnly=true
    GET /api/dairy/recent?limit=10          -- 최근 N개 조회
        - sort=ymd,desc

3. UPDATE (수정)

    - PUT /api/dairy/{ymd}                    -- 전체 수정

## 구현 요약

### 완성된 API 엔드포인트

| HTTP Method | 엔드포인트 | 설명 | 인증 |
|-----------|---------|------|------|
| POST | `/api/dairy` | 새로운 일기 생성 | 필수 |
| GET | `/api/dairy/{ymd}` | 특정 날짜의 일기 조회 | 필수 |
| GET | `/api/dairy` | 일기 목록 조회 (페이징, 필터링) | 필수 |
| GET | `/api/dairy/recent` | 최근 N개의 일기 조회 | 필수 |
| PUT | `/api/dairy/{ymd}` | 일기 수정 | 필수 |

### 기술 스택

- **Framework**: Spring Boot 4.0.1
- **Database**: MariaDB 10.3.35 (JPA/Hibernate)
- **Authentication**: JWT (필수)
- **Validation**: Jakarta Bean Validation
- **Logging**: SLF4J with Logback
- **Build Tool**: Gradle

### 주요 기능

1. **날짜 검증**: YYYYMMDD 형식의 8자리 숫자로 검증
2. **페이징**: Spring Data의 `Pageable` 활용
3. **필터링**: 날짜 범위(fromYmd~toYmd) 및 키워드 검색
4. **정렬**: ymd 필드 기준 오름/내림차순 정렬
5. **요약 전용 조회**: summaryOnly=true 시 요약만 반환
6. **예외 처리**:
   - 404 NOT FOUND (일기 없음)
   - 409 CONFLICT (중복 생성)
   - 400 BAD REQUEST (잘못된 날짜 형식)

### 구현 파일 목록

- `Entity`: Diary.java
- `Repository`: DiaryRepository.java
- `Service`: DiaryService.java
- `Controller`: DiaryController.java
- `DTOs`: DiaryCreateRequest, DiaryUpdateRequest, DiaryResponse, DiaryPageResponse
- `Exceptions`: DiaryNotFoundException, DiaryAlreadyExistsException, InvalidYmdFormatException
- `Utility`: YmdValidator.java

## test
