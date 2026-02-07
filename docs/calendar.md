# calendar

## 개요

달력을 베이스로 한 스케줄 표시. 

## 기존 테이블의 설계 및 달력 표시 소스

- 테이블 설계

```sql
CREATE TABLE `calendar` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ymd` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '일자',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '내용',
  `lvl` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '2' COMMENT '레벨',
  `modify_dt` datetime NOT NULL DEFAULT current_timestamp() COMMENT '수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=247 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일정';
```

- 달력 표시 소스
    - docs/examples/calendar 안에 기존 소스가 있음. 

## 예상되는 문제점

1. 자바로 만들어진 음력/양력 변환 로직이 있어야한다. 누군가가 만든 것을 찾아야함
2. 매달/매년/특정일을 모두 소화해서 sql을 작성해야한다.
3. 공휴일 정보를 가져와야한다. 
    - 공공데이터에서 가져올 수 있다
    - https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?serviceKey=e5e22c284503db8332064a44cffb10183a349dc92339f026f6ed7d9e0639f84a&solYear=2015&solMonth=09
    - 이것을 매번 가져와야하는가? 아니면 미리 가져와서 db에 저장해야하는가?

## 개선 방향

1. 원활하게 달력에 매달/매년/특정일에 따른 일정을 표시할 수 있어야한다.
2. 공휴일 정보와 나의 일정 정보를 함께 표시해야한다.
3. [공공데이터](https://www.data.go.kr/data/15012690/openapi.do)에서 데이터를 가져와서 calendar_public에 표시한다.


## sqls
```sql
-- 달력
DROP TABLE IF EXISTS `calendar`;
CREATE TABLE if not exists `calendar` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `gubun` CHAR(1) NOT NULL COMMENT 'H:공휴일, E:이벤트, Y:매년, M:매월, S:절기',
  `sorl` CHAR(1) NOT NULL DEFAULT 'S' COMMENT 'S:양력, L:음력',
  `ymd` VARCHAR(8) NOT NULL COMMENT 'H,E:YYYYMMDD | Y:MMDD | M:DD',
  `content` VARCHAR(200) NOT NULL COMMENT '내용',
  `created_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX idx_gubun_ymd (gubun, ymd),
  INDEX idx_sorl (sorl)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='캘린더';

-- 공공데이터 전용 테이블
drop table if exists calendar_public;
CREATE TABLE if not exists `calendar_public` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_type` VARCHAR(10) NOT NULL COMMENT 'HOLIDAY:공휴일, ANNIVERSARY:기념일, SOLAR_TERM:절기',
  `ymd` VARCHAR(8) NOT NULL COMMENT '날짜(YYYYMMDD)',
  `content` VARCHAR(200) NOT NULL COMMENT '내용',
  `created_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_ymd_type (ymd, data_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공공데이터(공휴일,기념일,절기)';
```

-- 1. 공휴일 (H) - YYYYMMDD
INSERT INTO calendar VALUES 
(null, 'H', 'S', '20260101', '신정', NOW()),           -- 양력 신정
(null, 'H', 'L', '20260101', '설날', NOW());           -- 음력 설날 (2026년 음력 1월 1일)

-- 2. 일반 이벤트 (E) - YYYYMMDD
INSERT INTO calendar VALUES 
(null, 'E', 'S', '20260214', '발렌타인데이 선물', NOW());

-- 3. 매년 반복 (Y) - MMDD
INSERT INTO calendar VALUES 
(null, 'Y', 'S', '0214', '자동차보험만기', NOW()),      -- 매년 양력 2월 14일
(null, 'Y', 'L', '0115', '할머니기일', NOW());         -- 매년 음력 1월 15일

-- 4. 매월 반복 (M) - DD
INSERT INTO calendar VALUES 
(null, 'M', 'S', '25', '월급날', NOW()),               -- 매월 25일
(null, 'M', 'L', '01', '초하루', NOW());               -- 매월 음력 1일
```

## 양/음력변환

- GitHub: https://github.com/usingsky/KoreanLunarCalendar 사용

## 로직

### server
    - 주어진 날짜 범위에서 start_ymd ~ end_ymd 에서 db 조회
    - calendar에서 음력/양력을 구분하여 조회한다.
    - 매년/매달 주기적인 일자를 확장하여 조회한다.
    - calendar_public에서 공휴일/기념일/절기 조회한다.
    - 이 모두를 합쳐서 리턴한다.
### client
    - 주어진 날짜범위에서 달력을 생성한다.
