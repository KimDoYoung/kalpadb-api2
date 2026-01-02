#!/bin/bash

# 프로젝트명 설정
PROJECT_NAME="kalpadb-api"
BASE_PACKAGE="kr.co.kalpa.dbapi2"
PACKAGE_PATH="kr/co/kalpa/dbapi2"

# 색상 출력용
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Spring Boot 프로젝트 구조 생성 시작${NC}"
echo -e "${BLUE}JDK 21, Spring Boot 4.0.1${NC}"
echo -e "${BLUE}MariaDB 10.3.35${NC}"
echo -e "${BLUE}========================================${NC}"

# 프로젝트 루트 생성
mkdir -p $PROJECT_NAME
cd $PROJECT_NAME

# Java 소스 디렉토리 구조 생성
echo -e "${GREEN}[1/5] Java 소스 디렉토리 생성 중...${NC}"
mkdir -p src/main/java/$PACKAGE_PATH/{config,controller,service,repository,entity,dto/request,dto/response,security,exception,util}
mkdir -p src/test/java/$PACKAGE_PATH/{controller,service,repository}

# Resources 디렉토리 구조 생성
echo -e "${GREEN}[2/5] Resources 디렉토리 생성 중...${NC}"
mkdir -p src/main/resources/static
mkdir -p src/test/resources

# .gitkeep 파일 생성 (빈 디렉토리도 git에 포함되도록)
find src -type d -empty -exec touch {}/.gitkeep \;

# build.gradle 생성
echo -e "${GREEN}[3/5] build.gradle 생성 중...${NC}"
cat > build.gradle << 'EOF'
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.1'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'jacoco'
}

group = 'kr.co.kalpa'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // MariaDB
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client:3.3.2'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
    
    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'io.rest-assured:rest-assured:5.5.0'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
    finalizedBy jacocoTestReport
}

jacoco {
    toolVersion = "0.8.12"
}

jacocoTestReport {
    dependsOn test
    reports {
        html.required = true
        xml.required = true
        csv.required = false
    }
}

tasks.named('bootRun') {
    systemProperty 'spring.profiles.active', project.findProperty('profile') ?: 'dev'
}
EOF

# settings.gradle 생성
cat > settings.gradle << EOF
rootProject.name = 'kalpadb-api'
EOF

# application.properties 생성
echo -e "${GREEN}[4/5] application.properties 생성 중...${NC}"
cat > src/main/resources/application.properties << 'EOF'
# Application
spring.application.name=kalpadb-api

# Server
server.port=8080
server.servlet.context-path=/api

# Database - MariaDB 10.3.35
spring.datasource.url=jdbc:mariadb://jskn.iptime.org:3306/kalpadb?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# HikariCP Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA - MariaDB Dialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDB103Dialect
spring.jpa.open-in-view=false

# JWT
jwt.secret=your-secret-key-change-this-in-production-make-it-long-and-secure-at-least-256-bits
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# Logging
logging.level.kr.co.kalpa.dbapi2=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE

# Jackson
spring.jackson.time-zone=Asia/Seoul
spring.jackson.serialization.write-dates-as-timestamps=false
EOF

# application-dev.properties 생성
cat > src/main/resources/application-dev.properties << 'EOF'
# Development Environment
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# DevTools
spring.devtools.restart.enabled=true

# Logging
logging.level.kr.co.kalpa.dbapi2=DEBUG
EOF

# application-prod.properties 생성
cat > src/main/resources/application-prod.properties << 'EOF'
# Production Environment
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Logging
logging.level.kr.co.kalpa.dbapi2=INFO
logging.level.org.springframework.web=WARN
logging.level.org.hibernate.SQL=WARN

# Security
server.error.include-message=never
server.error.include-stacktrace=never
EOF

# application-test.properties 생성
cat > src/test/resources/application-test.properties << 'EOF'
# Test Environment
spring.datasource.url=jdbc:mariadb://jskn.iptime.org:3306/kalpadb_test?useSSL=false&serverTimezone=Asia/Seoul
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDB103Dialect

# JWT for Test
jwt.secret=test-secret-key-for-testing-only-not-for-production
jwt.expiration=3600000
EOF

# .gitignore 생성
echo -e "${GREEN}[5/5] .gitignore 생성 중...${NC}"
cat > .gitignore << 'EOF'
# Gradle
.gradle
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/
gradle-app.setting
!gradle-wrapper.jar
!gradle-wrapper.properties

# IDE
.idea
*.iml
*.ipr
*.iws
.vscode/
*.swp
*.swo
*~
.classpath
.project
.settings/
bin/

# OS
.DS_Store
Thumbs.db

# Logs
logs/
*.log

# Application
application-local.properties
application-*.yml
!application.properties
!application-dev.properties
!application-prod.properties
!application-test.properties

# Test
/out/
/target/
EOF

# README.md 생성
cat > README.md << EOF
# KalpaDB RESTful API

MariaDB kalpadb에 대한 RESTful API 프로젝트

## 기술 스택

- **Java 21**
- **Spring Boot 4.0.1**
- **MariaDB 10.3.35**
- **JWT Authentication**
- **Gradle 8.11**

## 프로젝트 구조

\`\`\`
src/main/java/$PACKAGE_PATH/
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
\`\`\`

## 시작하기

### 1. 사전 요구사항

- JDK 21 설치
- MariaDB 10.3.35 설치 및 실행
- Gradle 8.11+ (또는 Gradle Wrapper 사용)

### 2. 데이터베이스 설정

\`\`\`sql
CREATE DATABASE kalpadb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE kalpadb_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
\`\`\`

### 3. 설정 파일 수정

\`src/main/resources/application.properties\` 파일에서 DB 정보 수정:

\`\`\`properties
spring.datasource.url=jdbc:mariadb://jskn.iptime.org:3306/kalpadb
spring.datasource.username=your_username
spring.datasource.password=your_password
\`\`\`

### 4. 빌드 및 실행

\`\`\`bash
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
\`\`\`

## API 엔드포인트

기본 컨텍스트 경로: \`/api\`

예시:
- \`POST /api/auth/login\` - 로그인
- \`GET /api/users\` - 사용자 목록 조회
- \`POST /api/users\` - 사용자 생성
- \`GET /api/users/{id}\` - 특정 사용자 조회
- \`PUT /api/users/{id}\` - 사용자 수정
- \`DELETE /api/users/{id}\` - 사용자 삭제

## MariaDB 연결 확인

\`\`\`bash
# MariaDB 접속 테스트
mysql -h jskn.iptime.org -u root -p kalpadb

# 테이블 목록 확인
SHOW TABLES;
\`\`\`

## 테스트

\`\`\`bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests UserControllerTest

# 테스트 커버리지 리포트 생성
./gradlew test jacocoTestReport

# 리포트 확인
open build/reports/tests/test/index.html
open build/reports/jacoco/test/html/index.html
\`\`\`

## 개발 가이드

### 새 엔티티 추가 시 체크리스트

1. \`entity/\` - JPA Entity 클래스 생성
2. \`repository/\` - Repository 인터페이스 생성
3. \`dto/request/\` - 요청 DTO 생성
4. \`dto/response/\` - 응답 DTO 생성
5. \`service/\` - Service 클래스 생성
6. \`controller/\` - Controller 클래스 생성
7. \`test/\` - 테스트 코드 작성

## 환경 변수

프로덕션 환경에서는 환경 변수 사용 권장:

\`\`\`bash
export DB_URL=jdbc:mariadb://prod-server:3306/kalpadb
export DB_USERNAME=prod_user
export DB_PASSWORD=prod_password
export JWT_SECRET=your-production-secret-key
\`\`\`

## 라이센스

MIT License

## 작성자

KalpaCorp
EOF

# Main Application 클래스 생성
MAIN_APP_DIR="src/main/java/$PACKAGE_PATH"
cat > $MAIN_APP_DIR/KalpaDbApiApplication.java << EOF
package ${BASE_PACKAGE};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KalpaDbApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KalpaDbApiApplication.class, args);
    }

}
EOF

# gradle.properties 생성
cat > gradle.properties << 'EOF'
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
EOF

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✅ 프로젝트 생성 완료!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "프로젝트 위치: ${YELLOW}$(pwd)${NC}"
echo ""
echo -e "${BLUE}다음 단계:${NC}"
echo "1. cd $PROJECT_NAME"
echo "2. gradle wrapper --gradle-version 8.11"
echo "3. ./gradlew build"
echo "4. src/main/resources/application.properties 수정 (DB 설정)"
echo "5. ./gradlew bootRun -Pprofile=dev"
echo ""
echo -e "${YELLOW}⚠️  MariaDB 연결 확인:${NC}"
echo "mysql -h jskn.iptime.org -u root -p kalpadb"
echo ""
echo -e "${GREEN}Happy Coding! 🚀${NC}"