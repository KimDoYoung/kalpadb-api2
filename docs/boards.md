# Boards & Posts API 설계서

## 개요

게시판 시스템을 위한 REST API 설계입니다.
- **Boards**: 게시판 마스터 (다양한 게시판 관리)
- **Posts**: 게시글 (HTML, Markdown 형식 지원)

---

## 1. Boards API (게시판 관리)

### 1.1 게시판 목록 조회
```
GET /api/boards
```

**응답:**
```json
{
  "code": 200,
  "message": "게시판 목록 조회 성공",
  "data": [
    {
      "id": 1,
      "boardCode": "notice",
      "boardNameKor": "공지사항",
      "contentType": "html",
      "description": "시스템 공지사항",
      "createdAt": "2026-02-01T10:00:00",
      "updatedAt": "2026-02-01T10:00:00"
    },
    {
      "id": 2,
      "boardCode": "free",
      "boardNameKor": "자유게시판",
      "contentType": "html",
      "description": "자유로운 주제로 글을 쓸 수 있는 게시판",
      "createdAt": "2026-02-01T10:00:00",
      "updatedAt": "2026-02-01T10:00:00"
    }
  ]
}
```

---

### 1.2 게시판 생성 (관리자만)
```
POST /api/boards
Content-Type: application/json
Authorization: Bearer {token}
```

**요청:**
```json
{
  "boardCode": "tech",
  "boardNameKor": "기술 게시판",
  "contentType": "markdown",
  "description": "기술 관련 글"
}
```

**응답:**
```json
{
  "code": 201,
  "message": "게시판이 생성되었습니다",
  "data": {
    "id": 3,
    "boardCode": "tech",
    "boardNameKor": "기술 게시판",
    "contentType": "markdown",
    "description": "기술 관련 글",
    "createdAt": "2026-02-03T14:00:00",
    "updatedAt": "2026-02-03T14:00:00"
  }
}
```

---

### 1.3 게시판 상세 조회
```
GET /api/boards/{boardId}
```

**응답:**
```json
{
  "code": 200,
  "message": "게시판 조회 성공",
  "data": {
    "id": 1,
    "boardCode": "notice",
    "boardNameKor": "공지사항",
    "contentType": "html",
    "description": "시스템 공지사항",
    "createdAt": "2026-02-01T10:00:00",
    "updatedAt": "2026-02-01T10:00:00"
  }
}
```

---

### 1.4 게시판 수정 (관리자만)
```
PUT /api/boards/{boardId}
Content-Type: application/json
Authorization: Bearer {token}
```

**요청:**
```json
{
  "boardNameKor": "공지사항 (수정됨)",
  "description": "수정된 설명"
}
```

**응답:** (1.3과 동일)

---

### 1.5 게시판 삭제 (관리자만)
```
DELETE /api/boards/{boardId}
Authorization: Bearer {token}
```

**응답:**
```json
{
  "code": 200,
  "message": "게시판이 삭제되었습니다",
  "data": null
}
```

---

## 2. Posts API (게시글 관리)

### 2.1 게시글 목록 조회
```
GET /api/posts?boardId=1&keyword=검색어&startDate=20260201&endDate=20260228&page=0&size=10&sort=createdAt,desc
```

**파라미터:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| boardId | Long | N | 게시판 ID |
| keyword | String | N | 제목/내용 검색 |
| startDate | String | N | 시작 기준일 (YYYYMMDD) |
| endDate | String | N | 종료 기준일 (YYYYMMDD) |
| page | int | N | 페이지 (기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 10) |
| sort | String | N | 정렬 (기본값: createdAt,desc) |

**응답:**
```json
{
  "code": 200,
  "message": "게시글 목록 조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "boardId": 1,
        "title": "공지사항 제목",
        "author": "관리자",
        "contentType": "html",
        "summary": "게시글 요약",
        "viewCount": 125,
        "baseYmd": "20260203",
        "createdAt": "2026-02-03T10:00:00",
        "updatedAt": "2026-02-03T10:00:00"
      }
    ],
    "totalElements": 45,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

---

### 2.2 게시글 생성
```
POST /api/posts
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**요청 파라미터:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| boardId | Long | Y | 게시판 ID |
| title | String | Y | 게시글 제목 (최대 500자) |
| author | String | N | 작성자 (미지정 시 로그인 사용자) |
| contentType | String | Y | html 또는 markdown |
| content | String | Y | 게시글 내용 |
| baseYmd | String | Y | 기준일 (YYYYMMDD) |
| files | MultipartFile[] | N | 첨부파일 |

**요청 예시 (cURL):**
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer {token}" \
  -F "boardId=1" \
  -F "title=공지사항" \
  -F "author=관리자" \
  -F "contentType=html" \
  -F "content=<p>게시글 내용</p>" \
  -F "baseYmd=20260203" \
  -F "files=@파일1.pdf" \
  -F "files=@파일2.docx"
```

**응답:**
```json
{
  "code": 201,
  "message": "게시글이 생성되었습니다",
  "data": {
    "id": 1,
    "boardId": 1,
    "title": "공지사항",
    "author": "관리자",
    "contentType": "html",
    "content": "<p>게시글 내용</p>",
    "viewCount": 0,
    "baseYmd": "20260203",
    "createdAt": "2026-02-03T14:00:00",
    "updatedAt": "2026-02-03T14:00:00"
  }
}
```

**내부 처리:**
1. Post 저장
2. content HTML/Markdown 파싱 → EDITOR_IMAGE 파일ID 추출
3. file_match 레코드 생성 (table_name='posts', file_type='EDITOR_IMAGE')
4. files 파라미터의 첨부파일 업로드 → file_match 생성 (file_type='ATTACHMENT')

---

### 2.3 게시글 상세 조회
```
GET /api/posts/{postId}
```

**응답:**
```json
{
  "code": 200,
  "message": "게시글 조회 성공",
  "data": {
    "id": 1,
    "boardId": 1,
    "title": "공지사항",
    "author": "관리자",
    "contentType": "html",
    "content": "<p>게시글 내용</p>",
    "viewCount": 126,
    "baseYmd": "20260203",
    "createdAt": "2026-02-03T10:00:00",
    "updatedAt": "2026-02-03T10:00:00"
  }
}
```

---

### 2.4 게시글 조회수 증가
```
PATCH /api/posts/{postId}/view-count
```

**응답:**
```json
{
  "code": 200,
  "message": "조회수가 증가되었습니다",
  "data": {
    "id": 1,
    "viewCount": 127
  }
}
```

---

### 2.5 게시글 수정
```
PUT /api/posts/{postId}
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**요청 파라미터:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| title | String | N | 제목 |
| author | String | N | 작성자 |
| contentType | String | N | html 또는 markdown |
| content | String | N | 내용 |
| baseYmd | String | N | 기준일 (YYYYMMDD) |
| newFiles | MultipartFile[] | N | 새로 추가할 첨부파일 |
| deletedFileIds | String | N | 삭제할 파일ID (JSON 배열) |

**요청 예시:**
```bash
curl -X PUT http://localhost:8080/api/posts/1 \
  -H "Authorization: Bearer {token}" \
  -F "title=수정된 제목" \
  -F "content=<p>수정된 내용</p>" \
  -F "newFiles=@새파일.pdf" \
  -F "deletedFileIds=[2, 3]"
```

**응답:** (2.3과 동일)

**내부 처리:**
1. Post 정보 업데이트
2. Content 변경 시:
   - HTML/Markdown 파싱 → 새로운 EDITOR_IMAGE 파일ID 추출
   - 기존 file_match와 비교
   - 변경사항 반영 (고아 이미지 허용)
3. newFiles 업로드 → file_match 생성 (ATTACHMENT)
4. deletedFileIds 파일들 → file_match 레코드 삭제 (파일은 보존)

---

### 2.6 게시글 삭제
```
DELETE /api/posts/{postId}
Authorization: Bearer {token}
```

**응답:**
```json
{
  "code": 200,
  "message": "게시글이 삭제되었습니다",
  "data": null
}
```

**주의:** 파일은 보존됨 (file_match만 삭제)

---

### 2.7 게시글 파일 목록 조회
```
GET /api/posts/{postId}/files
```

**응답:**
```json
{
  "code": 200,
  "message": "파일 목록 조회 성공",
  "data": [
    {
      "fileId": 5,
      "orgFileName": "document.pdf",
      "fileSize": 1024000,
      "mimeType": "application/pdf",
      "createdAt": "2026-02-03T14:00:00"
    },
    {
      "fileId": 6,
      "orgFileName": "image.jpg",
      "fileSize": 256000,
      "mimeType": "image/jpeg",
      "createdAt": "2026-02-03T14:00:00"
    }
  ]
}
```

**주의:** ATTACHMENT 파일만 반환 (EDITOR_IMAGE는 content에 포함)

---

## 3. 에러 응답

### 400 Bad Request
```json
{
  "code": 400,
  "message": "필수 파라미터가 누락되었습니다",
  "data": null
}
```

### 401 Unauthorized
```json
{
  "code": 401,
  "message": "인증이 필요합니다",
  "data": null
}
```

### 403 Forbidden (관리자 권한 필요)
```json
{
  "code": 403,
  "message": "관리자만 접근 가능합니다",
  "data": null
}
```

### 404 Not Found
```json
{
  "code": 404,
  "message": "게시판 또는 게시글을 찾을 수 없습니다",
  "data": null
}
```

### 500 Internal Server Error
```json
{
  "code": 500,
  "message": "서버 오류가 발생했습니다",
  "data": null
}
```

---

## 4. 파일 관리 전략

### 4.1 Content에 포함된 이미지 (EDITOR_IMAGE)

**HTML의 경우:**
```html
<!-- Content에 저장됨 -->
<p>본문 내용</p>
<img src="/api/file/download/123">
<img src="/api/file/download/124">
```

**Markdown의 경우:**
```markdown
# 제목

본문 내용

![이미지1](/api/file/download/123)
![이미지2](/api/file/download/124)
```

**file_match 레코드:**
```
table_name='posts', target_id=1, file_id=123, file_type='EDITOR_IMAGE'
table_name='posts', target_id=1, file_id=124, file_type='EDITOR_IMAGE'
```

**특징:**
- Content 파싱으로 파일ID 자동 추출
- 수정 시 content 변경만 인식 (고아 이미지 허용)
- 조회 시 content에 포함된 이미지는 자동으로 렌더링됨

---

### 4.2 첨부파일 (ATTACHMENT)

**요청:**
```
POST /api/posts with files parameter
```

**file_match 레코드:**
```
table_name='posts', target_id=1, file_id=201, file_type='ATTACHMENT'
table_name='posts', target_id=1, file_id=202, file_type='ATTACHMENT'
```

**특징:**
- GET /api/posts/{postId}/files 로 조회 가능
- 다운로드는 /api/file/download/{fileId}로 가능
- 삭제 시 file_match만 삭제 (파일은 보존)

---

## 5. 데이터베이스 구조 참고

### boards 테이블
```sql
CREATE TABLE boards (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  board_code    VARCHAR(50)  NOT NULL UNIQUE,
  board_name_kor VARCHAR(100) NOT NULL,
  content_type   VARCHAR(10)  NOT NULL DEFAULT 'html', -- html, markdown
  description    TEXT NULL,
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
```

### posts 테이블
```sql
CREATE TABLE posts (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  board_id     BIGINT UNSIGNED NOT NULL,
  title        VARCHAR(500) NOT NULL,
  author       VARCHAR(100) NOT NULL DEFAULT '관리자',
  content_type VARCHAR(30)  NOT NULL DEFAULT 'html', -- html, markdown
  content      LONGTEXT NULL,
  view_count   INT NOT NULL DEFAULT 0,
  base_ymd     VARCHAR(8) NOT NULL,
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
  KEY idx_board_id (board_id),
  KEY idx_base_ymd (base_ymd)
);
```

### file_match 테이블
```sql
-- EDITOR_IMAGE: content에 포함된 이미지
-- ATTACHMENT: 첨부파일
INSERT INTO file_match
(table_name, target_id, file_id, file_type)
VALUES
('posts', 1, 123, 'EDITOR_IMAGE'),
('posts', 1, 201, 'ATTACHMENT');
```

---

## 6. 권한 관리

| 엔드포인트 | 필요 권한 | 설명 |
|----------|---------|------|
| GET /api/boards | 없음 | 누구나 조회 가능 |
| POST /api/boards | ADMIN | 관리자만 생성 가능 |
| PUT /api/boards/{id} | ADMIN | 관리자만 수정 가능 |
| DELETE /api/boards/{id} | ADMIN | 관리자만 삭제 가능 |
| GET /api/posts | 없음 | 누구나 조회 가능 |
| POST /api/posts | USER | 로그인 사용자만 작성 가능 |
| PUT /api/posts/{id} | OWNER | 작성자 또는 관리자만 수정 가능 |
| DELETE /api/posts/{id} | OWNER | 작성자 또는 관리자만 삭제 가능 |

---

## 7. 향후 확장 사항

- [ ] 게시글 댓글/대댓글 기능
- [ ] 게시글 좋아요 기능
- [ ] 게시판별 접근 권한 관리
- [ ] 게시글 카테고리/태그 관리
- [ ] 검색 최적화 (Elasticsearch)
- [ ] 썸네일 이미지 자동 생성
