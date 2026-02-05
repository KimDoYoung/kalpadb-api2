# ap node system

## 개요

웹 UI 로 파일시스템을 구현한다.
유닉스 파일시스템을 모방하여 테이블을 설계함.
2개의 테이블로 설계

## tables

```sql
CREATE TABLE `ap_node` (
  `id` VARCHAR(40) NOT NULL COMMENT 'UUID',
  `node_type` CHAR(1) NOT NULL COMMENT 'F:파일, D:디렉토리',
  `parent_id` VARCHAR(40) DEFAULT NULL COMMENT '부모 노드 ID (NULL이면 루트)',
  `name` VARCHAR(255) NOT NULL COMMENT '노드 이름',
  `depth` INT NOT NULL DEFAULT 0 COMMENT '트리 깊이 (루트=0)',
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',
  `delete_dt` DATETIME NULL COMMENT '삭제 일시',
  `create_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_dt` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
  
  -- 디렉토리 전용 필드
  `child_count` INT DEFAULT 0 COMMENT '하위 노드 수 (캐시)',
  `total_size` BIGINT DEFAULT 0 COMMENT '하위 전체 크기 (캐시)',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parent_name` (`parent_id`, `name`, `is_deleted`),
  INDEX `idx_parent` (`parent_id`),
  INDEX `idx_type_deleted` (`node_type`, `is_deleted`),
  
  CONSTRAINT `fk_node_parent` 
    FOREIGN KEY (`parent_id`) REFERENCES `ap_node`(`id`) 
    ON DELETE RESTRICT,
  
  CONSTRAINT `chk_depth` CHECK (`depth` >= 0 AND `depth` <= 20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ap_file` (
  `node_id` VARCHAR(40) NOT NULL COMMENT 'ap_node의 ID',
  `saved_path` VARCHAR(1000) NOT NULL COMMENT '실제 저장 경로',
  `original_name` VARCHAR(500) NOT NULL COMMENT '원본 파일명',
  `file_size` BIGINT NOT NULL COMMENT '파일 크기 (bytes)',
  `content_type` VARCHAR(100) NULL COMMENT 'MIME 타입',
  `sha256_hash` CHAR(64) NULL COMMENT 'SHA-256 해시 (중복 제거용)',
  
  -- 이미지 전용 필드
  `width` INT NULL,
  `height` INT NULL,
  `thumbnail_path` VARCHAR(500) NULL,
  
  `upload_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`node_id`),
  INDEX `idx_hash` (`sha256_hash`),
  INDEX `idx_content_type` (`content_type`),
  
  CONSTRAINT `fk_file_node` 
    FOREIGN KEY (`node_id`) REFERENCES `ap_node`(`id`) 
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### table ddl 설명

- delete_yn → is_deleted (BOOLEAN): 표준화
- parent_node_id 제거: ap_file에서 불필요한 중복 제거
- saved_dir_name 제거: saved_path로 통합 (전체 경로)
- hashcode → sha256_hash: 명확한 네이밍 + 길이 고정
- full_name 제거: 필요시 재귀 쿼리로 생성
- depth 추가: 순환 참조 방지 + 깊이 제한
- UNIQUE 제약: 동일 디렉토리 내 중복 이름 방지
- 외래키 + 인덱스: 데이터 무결성 + 성능

## 기능

1. root '/' 폴더의 하위에 폴더 생성, 이후 하위 폴더 생성,
2. 폴더의 이동, 삭제, 폴더명 변경
3. 특정 폴더에 파일 업로드
4. 업로드된 파일의 삭제, 파일명변경, 파일를 다른 폴더로 이동
5. 폴더 및 파일 찾기

## UI

```text

┌─────────────────────────────────────────────────────────────┐
│ [≡ 메뉴] PCMS2                    [🔍 검색]      [사용자]     │
├───────────┬─────────────────────────────────────────────────┤
│ 📁 Tree   │ 🏠 / Documents / 2024      [⊞Grid] [≡List] [⋮] │
│           ├─────────────────────────────────────────────────┤
│ 📁 /      │                                                 │
│ ├─📁 Docs │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐  │
│ │ ├─📁2024│  │ 📄     │ │ 📄     │ │ 🖼️     │ │ 📁     │  │
│ │ └─📁Arch│  │report  │ │budget  │ │chart   │ │backup  │  │
│ ├─📁Photos│  │2.5 MB  │ │1.2 MB  │ │500 KB  │ │        │  │
│ └─📁Proj  │  │Feb 4   │ │Feb 3   │ │Feb 2   │ │        │  │
│           │  └────────┘ └────────┘ └────────┘ └────────┘  │
│ 휴지통    │                                                 │
│ 최근파일  │  [선택: 2개] [다운로드] [이동] [삭제] [공유]    │
└───────────┴─────────────────────────────────────────────────┘

```

## 구현 스택

- Vanilla JavaScript Tree
- examples/vanilla_tree1.js 참조

## id

- uuid로 생성한다.

```java
implementation 'com.github.f4b6a3:uuid-creator:5.3.7'
// Java 코드
import com.github.f4b6a3.uuid.UuidCreator;
String nodeId = UuidCreator.getTimeOrderedEpoch().toString();
```

## root folder의 생성

- root 폴더의 생성

```sql
-- 루트 디렉토리 (parent_id = NULL)
INSERT INTO ap_node (id, node_type, parent_id, name, depth) 
VALUES (UUID(), 'D', NULL, '/', 0);
```

- 하위 폴더의 생성

```sql
-- /documents 생성
INSERT INTO ap_node (id, node_type, parent_id, name, depth)
SELECT UUID(), 'D', id, 'documents', 1
FROM ap_node WHERE name = '/' AND parent_id IS NULL;

-- /documents/2024 생성
INSERT INTO ap_node (id, node_type, parent_id, name, depth)
SELECT UUID(), 'D', id, '2024', 2
FROM ap_node WHERE name = 'documents' AND parent_id = (
    SELECT id FROM ap_node WHERE name = '/' AND parent_id IS NULL
);
```

## 폴더 이동

```sql
-- /documents/2024를 /archive/2024로 이동
UPDATE ap_node 
SET parent_id = (SELECT id FROM ap_node WHERE name = 'archive'),
    depth = (SELECT depth + 1 FROM ap_node WHERE name = 'archive'),
    modify_dt = NOW()
WHERE id = '2024-folder-id';

-- 하위 모든 노드의 depth도 업데이트 (재귀 필요)
WITH RECURSIVE descendants AS (
    SELECT id, depth 
    FROM ap_node 
    WHERE id = '2024-folder-id'
    UNION ALL
    SELECT n.id, n.depth
    FROM ap_node n
    INNER JOIN descendants d ON n.parent_id = d.id
)
UPDATE ap_node n
INNER JOIN descendants d ON n.id = d.id
SET n.depth = d.depth + (새depth - 기존depth);
```

## 폴더 삭제 (논리 삭제)

```sql
-- 단일 폴더 삭제
UPDATE ap_node 
SET is_deleted = TRUE, 
    delete_dt = NOW() 
WHERE id = 'folder-id';

-- 하위 모든 항목도 삭제 (재귀)
WITH RECURSIVE descendants AS (
    SELECT id FROM ap_node WHERE id = 'folder-id'
    UNION ALL
    SELECT n.id 
    FROM ap_node n
    INNER JOIN descendants d ON n.parent_id = d.id
)
UPDATE ap_node 
SET is_deleted = TRUE, delete_dt = NOW()
WHERE id IN (SELECT id FROM descendants);
```

## 폴더명 변경

```sql
UPDATE ap_node 
SET name = '새폴더명', 
    modify_dt = NOW()
WHERE id = 'folder-id';
```

## 파일업로드

```sql
-- Step 1: 노드 생성
INSERT INTO ap_node (id, node_type, parent_id, name, depth)
VALUES ('file-node-id', 'F', 'parent-folder-id', 'report.pdf', 3);

-- Step 2: 파일 메타데이터 저장
INSERT INTO ap_file (
    node_id, 
    saved_path, 
    original_name, 
    file_size, 
    content_type,
    sha256_hash
) VALUES (
    'file-node-id',
    '/storage/2024/02/abc123.pdf',
    'report.pdf',
    1048576,
    'application/pdf',
    'sha256-hash-value'
);
```

## 파일관리

### 파일 삭제

```sql
-- 논리 삭제 (ap_file은 ON DELETE CASCADE로 자동 처리)
UPDATE ap_node 
SET is_deleted = TRUE, delete_dt = NOW() 
WHERE id = 'file-node-id';

-- 또는 물리 삭제
DELETE FROM ap_node WHERE id = 'file-node-id';
-- ap_file 레코드는 FK CASCADE로 자동 삭제됨
```

### 파일명 변경

```sql
-- 파일명은 ap_node.name에만 저장되므로
UPDATE ap_node 
SET name = '새파일명.pdf', 
    modify_dt = NOW()
WHERE id = 'file-node-id';
```

### 파일 이동

```sql
-- 다른 폴더로 이동
UPDATE ap_node 
SET parent_id = 'new-folder-id',
    depth = (SELECT depth + 1 FROM ap_node WHERE id = 'new-folder-id'),
    modify_dt = NOW()
WHERE id = 'file-node-id';
```

## 찾기

```sql
-- 전체 검색
SELECT n.*, f.file_size, f.content_type
FROM ap_node n
LEFT JOIN ap_file f ON n.id = f.node_id
WHERE n.name LIKE '%report%' 
  AND n.is_deleted = FALSE;

-- 특정 폴더 내에서만 검색
SELECT n.*, f.file_size
FROM ap_node n
LEFT JOIN ap_file f ON n.id = f.node_id
WHERE n.parent_id = 'folder-id'
  AND n.name LIKE '%report%'
  AND n.is_deleted = FALSE;
```

## 전체 경로 검색

```sql
-- 파일의 전체 경로 구하기
WITH RECURSIVE path AS (
    SELECT id, parent_id, name, CAST(name AS CHAR(1000)) as full_path
    FROM ap_node WHERE id = 'file-node-id'
    UNION ALL
    SELECT n.id, n.parent_id, n.name, 
           CONCAT(n.name, '/', p.full_path)
    FROM ap_node n
    INNER JOIN path p ON n.id = p.parent_id
)
SELECT full_path FROM path WHERE parent_id IS NULL;
```

## 폴더 트리 구조 조회

```sql
-- 전체 트리 (depth 제한)
WITH RECURSIVE tree AS (
    SELECT id, parent_id, name, node_type, 0 as level,
           CAST(name AS CHAR(1000)) as path
    FROM ap_node 
    WHERE parent_id IS NULL AND is_deleted = FALSE
    
    UNION ALL
    
    SELECT n.id, n.parent_id, n.name, n.node_type, t.level + 1,
           CONCAT(t.path, '/', n.name)
    FROM ap_node n
    INNER JOIN tree t ON n.parent_id = t.id
    WHERE n.is_deleted = FALSE AND t.level < 5
)
SELECT * FROM tree ORDER BY path;
```

1. docs/ap_node_file.md를 읽을 것.
2. api contoller ApNodeApiController와 그에 따른 service, dto등을 작성할 것
3. ui controller ApNodeController를 작성할 것.
4. templates/apnode/ folder 생성, list.html을 작성하고 `기능`을 구현해 볼 것.


### 수정요망

1. folder을 이동, 삭제, 이름변경의 UI는 어떻게 하는게 좋을까? 현재 구현되어 있지 않음.
2. file의 이동, 이름변경의 UI는 어떻게 하는게 좋을까?
3. file이 이미지일때 thumb로 보기할때 icon을 emoji를 쓰는데 svg로 파일들로 만들어서 사용하면 더 멋있지 않을까?
4. file이 이미지일때 thumb로 보기할때 card의 오른쪽 윗쪽에 x -> 삭제를 의미하게 할 수 있을까?
5. file이 선택되면 file명이 길때 full 파일명을 보여주면 좋을 듯.
6. file이 이미지일때 미리보기가 있으면 좋을 것 같은데...
7. 각 file들이 url을 갖게 해야할 듯.
8. 하단에 status bar를 갖고 있으면 어떨까?