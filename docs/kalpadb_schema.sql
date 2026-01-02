-- MySQL dump 10.19  Distrib 10.3.35-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: kalpadb
-- ------------------------------------------------------
-- Server version	10.3.35-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ap_file`
--

DROP TABLE IF EXISTS `ap_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ap_file` (
  `node_id` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'NODE ID',
  `parent_node_id` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'parent node id',
  `saved_dir_name` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '저장폴더',
  `saved_file_name` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `org_file_name` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '파일명',
  `file_size` decimal(10,0) NOT NULL COMMENT '파일크기',
  `content_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'content 타입',
  `hashcode` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'hashcode',
  `note` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '파일에 대한 설명',
  `width` int(11) DEFAULT NULL,
  `height` int(11) DEFAULT NULL,
  `thumb_path` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `upload_dt` datetime DEFAULT current_timestamp() COMMENT '업로드일시',
  PRIMARY KEY (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ap_node`
--

DROP TABLE IF EXISTS `ap_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ap_node` (
  `id` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'id',
  `node_type` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '노드종류 F: file, D: Directory',
  `parent_id` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '부모노드id',
  `name` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '노드명',
  `full_name` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '전체이름',
  `owner_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '소유자ID',
  `group_auth` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '그룹권한: RWX',
  `guest_auth` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'guest권한',
  `delete_yn` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '업로드일시',
  `modify_dt` datetime DEFAULT NULL COMMENT '수정일시',
  `upload_id` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'upload 사용자id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `full_name` (`full_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='노드테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ap_user`
--

DROP TABLE IF EXISTS `ap_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ap_user` (
  `user_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '사용자ID',
  `user_pw` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '사용자PW',
  `user_nm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '사용자명',
  `user_home_folder` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '사용자 홈 폴더',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bible`
--

DROP TABLE IF EXISTS `bible`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bible` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `ord` int(11) NOT NULL COMMENT '순서',
  `gubun` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '신,구약구분 구:1, 신2',
  `han` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '한글명',
  `eng` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '영어명',
  `china` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '한자',
  `endno` int(11) NOT NULL COMMENT '장갯수',
  `abbr` varchar(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '약어',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '분류',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=143 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='성경구성';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `books`
--

DROP TABLE IF EXISTS `books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `books` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `title` varchar(200) NOT NULL COMMENT '제목',
  `author` varchar(100) DEFAULT NULL COMMENT '저자',
  `publisher` varchar(100) DEFAULT NULL COMMENT '출판사',
  `publish_year` varchar(8) DEFAULT NULL COMMENT '출판년월',
  `page` int(11) DEFAULT NULL COMMENT '페이지수',
  `startymd` varchar(8) DEFAULT NULL COMMENT '읽은 시작일',
  `endymd` varchar(8) DEFAULT NULL COMMENT '읽은 종료일',
  `content` text DEFAULT NULL COMMENT '소감',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '최초생성일시',
  `lastmodify_dt` datetime DEFAULT NULL COMMENT '최종수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `calendar`
--

DROP TABLE IF EXISTS `calendar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `calendar` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `gubun` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '종류',
  `sorl` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'S' COMMENT 'S:sun, L: Lunar ',
  `ymd` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '날짜',
  `content` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '내용',
  `modify_dt` datetime NOT NULL DEFAULT current_timestamp() COMMENT '수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='달력,스케줄';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `capture`
--

DROP TABLE IF EXISTS `capture`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `capture` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `folder_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_name` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `size` mediumtext COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sha1` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `srch_key` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT current_timestamp(),
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `movieExt` varchar(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `imageExt` varchar(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wdate` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wtime` varchar(6) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pyear` varchar(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `width` int(11) DEFAULT NULL,
  `height` int(11) DEFAULT NULL,
  `note` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12741 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dairy`
--

DROP TABLE IF EXISTS `dairy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dairy` (
  `ymd` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '일자',
  `content` text COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '내용',
  `summary` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '요약',
  PRIMARY KEY (`ymd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일기';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `essay`
--

DROP TABLE IF EXISTS `essay`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `essay` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `title` varchar(300) NOT NULL COMMENT '제목',
  `content` text DEFAULT NULL COMMENT '내용',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '최초생성일시',
  `lastmodify_dt` datetime DEFAULT NULL COMMENT '최종수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=179 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hdd`
--

DROP TABLE IF EXISTS `hdd`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hdd` (
  `id` int(11) NOT NULL,
  `volumn_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gubun` char(1) COLLATE utf8mb4_unicode_ci NOT NULL,
  `path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_name` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pdir` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `extension` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `size` double DEFAULT NULL,
  `sha1_cd` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `srch_key` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_modified_ymd` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pid` int(11) DEFAULT NULL,
  `right_pid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_hdd_vol_name` (`volumn_name`),
  KEY `idx_hdd_pid` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `history` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `frYmd` varchar(8) NOT NULL COMMENT '시작일자',
  `toYmd` varchar(8) DEFAULT NULL COMMENT '종료일자',
  `title` varchar(300) DEFAULT NULL COMMENT '제목',
  `content` text DEFAULT NULL COMMENT '내용',
  `lastmodify_dt` datetime DEFAULT current_timestamp() COMMENT '최종수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `jangbi`
--

DROP TABLE IF EXISTS `jangbi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jangbi` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ymd` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '구입일',
  `item` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '품목',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '위치',
  `cost` int(11) DEFAULT NULL COMMENT '가격',
  `spec` text COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '스펙(특징)',
  `lvl` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '2',
  `modify_dt` datetime NOT NULL DEFAULT current_timestamp() COMMENT '수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=247 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='구매물품';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `match_file_int`
--

DROP TABLE IF EXISTS `match_file_int`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `match_file_int` (
  `tbl` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'match table',
  `id` int(11) NOT NULL COMMENT 'table id',
  `node_id` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ap_file node_id',
  PRIMARY KEY (`tbl`,`id`,`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='integer id 에 대한 ap_file과의 매칭 ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `match_file_var`
--

DROP TABLE IF EXISTS `match_file_var`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `match_file_var` (
  `tbl` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'match table',
  `id` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'table id',
  `node_id` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ap_file node_id',
  PRIMARY KEY (`tbl`,`id`,`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='varchar id 에 대한 ap_file과의 매칭 ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `movie`
--

DROP TABLE IF EXISTS `movie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `movie` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `mid` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '영화id',
  `gubun` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '구분',
  `title1` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '제목(한글)',
  `title2` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '제목(영어)',
  `title3` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '제목(제작국언엉)',
  `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '분야',
  `gamdok` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '감독',
  `make_year` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '제작년',
  `nara` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '제작국적',
  `dvd_id` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'DVD ID',
  `title1num` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '검색문자열',
  `title1title2` varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '검색어',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9939 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='영화';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `movie_review`
--

DROP TABLE IF EXISTS `movie_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `movie_review` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `title` varchar(200) NOT NULL COMMENT '제목',
  `nara` varchar(30) DEFAULT NULL COMMENT '제작국가',
  `year` char(4) DEFAULT NULL COMMENT '제작년도',
  `lvl` int(11) DEFAULT NULL COMMENT '총평점수',
  `ymd` varchar(8) DEFAULT NULL COMMENT '본일자',
  `content` text DEFAULT NULL COMMENT '감상',
  `lastmodify_dt` datetime DEFAULT NULL COMMENT '최종수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=386 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `phrase`
--

DROP TABLE IF EXISTS `phrase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `phrase` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `bible_id` bigint(20) NOT NULL COMMENT 'bible의 id',
  `zno` smallint(5) unsigned NOT NULL COMMENT '장 번호',
  `sno` smallint(5) unsigned NOT NULL COMMENT '절 번호',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '내용',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='성경구절';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pwnote`
--

DROP TABLE IF EXISTS `pwnote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pwnote` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `site_nm` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '사이트명',
  `user_question` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '사용자가 작성한 질문',
  `user_answer` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '사용자 답변1',
  `user_encrypt_id_pw` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '사용자 암호화된 id_pw',
  `sys_question` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '시스템 질문',
  `sys_answer` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '시스템 질문',
  `sys_encrypt_id_pw` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '시스템 암호화된 id_pw',
  `upd_dt` datetime DEFAULT NULL COMMENT '최종수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='암호관리';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `snote`
--

DROP TABLE IF EXISTS `snote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `snote` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` text COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '노트',
  `create_dt` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '생성일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `technote`
--

DROP TABLE IF EXISTS `technote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `technote` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '제목',
  `titlenum` varchar(500) COLLATE utf8_bin DEFAULT NULL COMMENT '제목keywordformat',
  `note` text COLLATE utf8_bin DEFAULT NULL COMMENT '내용',
  `update_dt` datetime DEFAULT NULL COMMENT '최근변경일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=228 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `todo`
--

DROP TABLE IF EXISTS `todo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `todo` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `content` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '내용',
  `input_dt` datetime NOT NULL DEFAULT current_timestamp() COMMENT '입력일시',
  `done_yn` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N' COMMENT '완료YN',
  `done_dt` datetime DEFAULT NULL COMMENT '완료일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=763 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='해야할 일';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `words`
--

DROP TABLE IF EXISTS `words`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `words` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `han` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `eng` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `china` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updateDt` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=298 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-02 11:00:25
