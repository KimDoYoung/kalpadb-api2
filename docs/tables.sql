drop table if exists users;
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자ID',
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '사용자id',
  `user_pw` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '사용자PW',
  `user_nm` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '사용자명',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자';

-- 기본 사용자 데이터 (bcrypt 암호화, 비밀번호: 1111)
INSERT INTO users (user_id, user_pw, user_nm) VALUES
('kdy987', '$2a$10$vUYXTNVJV7h9pXpQR0W5s.E7pGvS.0OcvJqUMo3D3VFxq4nqquM3e', 'KimDoYoung'),
('admin', '$2a$10$Z3RTwwcpMPh4Egi/3P75N.x5JCu3iiUkPz7v2mwvFTHh2.nNvZX7K', 'Admin');

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

drop table if exists essay;
CREATE TABLE IF NOT EXISTS  `essay` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `title` varchar(300) NOT NULL COMMENT '제목',
  `content` text DEFAULT NULL COMMENT '내용',
  `tags` varchar(200) DEFAULT NULL COMMENT '태그',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '최초생성일시',
  `lastmodify_dt` datetime DEFAULT NULL COMMENT '최종수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=179 DEFAULT CHARSET=utf8;

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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    -- 아래 라인에서 COMMENT 부분을 제거했습니다.
    FOREIGN KEY (file_id) REFERENCES files(file_id) ON DELETE CASCADE,
    UNIQUE KEY unique_target_file (table_name, target_id, file_id) COMMENT '중복 방지',
    INDEX idx_table_target (table_name, target_id),
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
