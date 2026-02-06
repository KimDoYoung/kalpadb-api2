drop table if exists users;
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자ID',
  `user_id` VARCHAR(50) NOT NULL COMMENT '사용자id',  -- NOT NULL 추가
  `user_pw` VARCHAR(200) NOT NULL COMMENT '사용자PW',
  `user_nm` VARCHAR(100) DEFAULT NULL COMMENT '사용자명',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)  -- 마지막 콤마 제거
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자';

-- 기본 사용자 데이터 (bcrypt 암호화, 비밀번호: 1111)
INSERT INTO users (user_id, user_pw, user_nm) VALUES
('kdy987', '$2a$10$vUYXTNVJV7h9pXpQR0W5s.E7pGvS.0OcvJqUMo3D3VFxq4nqquM3e', 'KimDoYoung'),
('admin', '$2a$10$Z3RTwwcpMPh4Egi/3P75N.x5JCu3iiUkPz7v2mwvFTHh2.nNvZX7K', 'Admin');

-- 사용자 설정
DROP TABLE IF EXISTS `user_settings`;
CREATE TABLE IF NOT EXISTS `user_settings` (
    setting_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '설정ID',
    user_id VARCHAR(50) NOT NULL COMMENT '사용자ID',
    setting_key VARCHAR(50) NOT NULL COMMENT '설정키',     -- 'theme', 'language', 'timezone' 등
    setting_value TEXT COMMENT '설정값',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_setting (user_id, setting_key)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자설정';

-- diary (개선: id를 PK로, ymd는 UNIQUE 인덱스)
DROP TABLE IF EXISTS `diary`;
CREATE TABLE IF NOT EXISTS `diary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '고유번호 (파일 매칭용)',
  `ymd` VARCHAR(8) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE COMMENT '일자 (조회용)',
  `content` TEXT COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '내용',
  `summary` VARCHAR(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '요약',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY (`id`),
  INDEX idx_ymd (`ymd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일기';

DROP TABLE IF EXISTS `jangbi`;
CREATE TABLE `jangbi` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ymd` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '구입일',
  `item` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '품목',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '위치',
  `cost` int(11) DEFAULT NULL COMMENT '가격',
  `spec` text COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '스펙(특징)',
  `lvl` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '2',
  `modify_dt` datetime NOT NULL DEFAULT current_timestamp() COMMENT '수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=247 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='구매물품';


-- attach_files
drop table if exists files;
CREATE TABLE IF NOT EXISTS files (
    file_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    saved_folder VARCHAR(500) NOT NULL COMMENT '저장된 폴더 경로',
    org_file_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    physical_file_name VARCHAR(255) NOT NULL COMMENT '물리적 파일명',
    file_size BIGINT NOT NULL COMMENT '파일 크기(bytes)',
    mime_type VARCHAR(100) COMMENT 'MIME 타입',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_physical_name (physical_file_name),
    INDEX idx_org_file_name (org_file_name)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일정보';

-- 파일 매칭 테이블 (개선: VARCHAR로 table명 기술, 확장성 우선)
drop table if exists file_match;
CREATE TABLE IF NOT EXISTS file_match (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '매칭 고유번호',
    table_name VARCHAR(100) NOT NULL COMMENT '대상 테이블명',
    target_id BIGINT NOT NULL COMMENT '대상 테이블 항목 ID',
    file_id BIGINT NOT NULL COMMENT '파일 ID',
    file_type VARCHAR(50) NOT NULL COMMENT '파일 유형 (ATTACHMENT, CONTENT_IMAGE, THUMBNAIL 등)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    
    FOREIGN KEY (file_id) REFERENCES files(file_id) ON DELETE CASCADE,
    
    -- file_type까지 포함하여 중복 방지
    UNIQUE KEY unique_target_file (table_name, target_id, file_id, file_type),
    
    INDEX idx_table_target (table_name, target_id),
    INDEX idx_table_target_type (table_name, target_id, file_type),
    INDEX idx_file_id (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일 매칭';

DROP TABLE IF EXISTS `todo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `todo` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `content` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '내용',
  `input_dt` datetime NOT NULL DEFAULT current_timestamp() COMMENT '입력일시',
  `done_yn` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N' COMMENT '완료YN',
  `done_dt` datetime DEFAULT NULL COMMENT '완료일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=763 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='해야할 일';


-- 게시판 마스터
CREATE TABLE IF NOT EXISTS boards (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  board_code    VARCHAR(50)  NOT NULL COMMENT '게시판 코드(유니크)',
  board_name_kor VARCHAR(100) NOT NULL COMMENT '게시판 한글명',
  content_type   VARCHAR(10)  NOT NULL DEFAULT 'html' COMMENT '내용의 문서종류(text|html|markdown|json)',
  description    TEXT NULL COMMENT '게시판 설명',
  created_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  updated_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (id),
  UNIQUE KEY uk_boards_board_code (board_code),
  KEY idx_boards_name_kor (board_name_kor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 게시글
CREATE TABLE IF NOT EXISTS posts (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  board_id     BIGINT UNSIGNED NOT NULL COMMENT 'boards.id FK',
  -- 선택1) board_code를 함께 보관해 조인 없이 조회 최적화(정합성은 트리거/앱에서 보정)
  -- board_code  VARCHAR(50)  NULL COMMENT '조인 없이 필터용 캐시 컬럼(선택)',
--
  title    VARCHAR(500) NOT NULL COMMENT '제목 한글',
  author   VARCHAR(100) NOT NULL DEFAULT '관리자' COMMENT '작성자 한글',
  content  LONGTEXT NULL COMMENT '내용(한글)',
  view_count   INT NOT NULL DEFAULT 0 COMMENT '조회횟수',
  base_ymd     VARCHAR(8) NOT NULL COMMENT '기준일(YYYYMMDD)',
  created_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일',
  updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (id),
  CONSTRAINT fk_posts_board
    FOREIGN KEY (board_id) REFERENCES boards(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  -- 자주 찾을 것 같은 인덱스
  KEY idx_posts_board_id (board_id),
  KEY idx_posts_base_ymd (base_ymd),
  KEY idx_posts_title (title)
  -- 선택2) board_code 컬럼을 둘 경우:
  -- KEY idx_posts_board_code (board_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;