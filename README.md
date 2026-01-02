# KalpaDB RESTful API

MariaDB kalpadb에 대한 RESTful API 프로젝트

## 기술 스택

- **Java 21**
- **Spring Boot 4.0.1**
- **MariaDB 10.3.35**
- **JWT Authentication**
- **Gradle 8.11**

## 프로젝트 구조

```text
src/main/java/kr/co/kalpa/dbapi2/
├── config/          # 설정 클래스 (Security, JPA, Web 등)
├── controller/      # REST 컨트롤러
├── service/         # 비즈니스 로직
├── repository/      # JPA Repository
├── entity/          # JPA Entity (DB 테이블 매핑)
├── dto/             # 데이터 전송 객체
│   ├── request/     # 요청 DTO
│   └── response/    # 응답 DTO
├── security/        # JWT 인증/인가
├── exception/       # 예외 처리
└── util/            # 유틸리티
```

## 시작하기

### 1. 사전 요구사항

- JDK 21 설치
- MariaDB 10.3.35 설치 및 실행
- Gradle 8.11+ (또는 Gradle Wrapper 사용)

### 2. 데이터베이스 설정

```sql
CREATE DATABASE kalpadb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE kalpadb_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 설정 파일 수정

`src/main/resources/application.properties` 파일에서 DB 정보 수정:

```properties
spring.datasource.url=jdbc:mariadb://jskn.iptime.org:3306/kalpadb
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. 빌드 및 실행

```bash
# Gradle Wrapper 생성 (최초 1회)
gradle wrapper --gradle-version 8.11

# 빌드
./gradlew build

# 테스트
./gradlew test

# 개발 환경 실행
./gradlew bootRun -Pprofile=dev

# 운영 환경 실행
./gradlew bootRun -Pprofile=prod

# JAR 실행
java -jar build/libs/kalpadb-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## API 엔드포인트

기본 컨텍스트 경로: `/api`

예시:

- `POST /api/auth/login` - 로그인
- `GET /api/users` - 사용자 목록 조회
- `POST /api/users` - 사용자 생성
- `GET /api/users/{id}` - 특정 사용자 조회
- `PUT /api/users/{id}` - 사용자 수정
- `DELETE /api/users/{id}` - 사용자 삭제

## MariaDB 연결 확인

```bash
# MariaDB 접속 테스트
mysql -h jskn.iptime.org -u root -p kalpadb

# 테이블 목록 확인
SHOW TABLES;
```

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests UserControllerTest

# 테스트 커버리지 리포트 생성
./gradlew test jacocoTestReport

# 리포트 확인
open build/reports/tests/test/index.html
open build/reports/jacoco/test/html/index.html
```

## 개발 가이드

### 새 엔티티 추가 시 체크리스트

1. `entity/` - JPA Entity 클래스 생성
2. `repository/` - Repository 인터페이스 생성
3. `dto/request/` - 요청 DTO 생성
4. `dto/response/` - 응답 DTO 생성
5. `service/` - Service 클래스 생성
6. `controller/` - Controller 클래스 생성
7. `test/` - 테스트 코드 작성

## 환경 변수

프로덕션 환경에서는 환경 변수 사용 권장:

```bash
export DB_URL=jdbc:mariadb://prod-server:3306/kalpadb
export DB_USERNAME=prod_user
export DB_PASSWORD=prod_password
export JWT_SECRET=your-production-secret-key
```

## 라이센스

MIT License

## 작성자

KalpaCorp
