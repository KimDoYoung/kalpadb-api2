#!/bin/bash

# buildwar.sh - WAR 파일 빌드 스크립트 (kalpadb-api)
# 사용법: ./buildwar.sh <profile>
# 예시:   ./buildwar.sh jskn
#         ./buildwar.sh fedora
#         ./buildwar.sh home

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

VALID_PROFILES=("jskn" "fedora" "home")
WAR_NAME="kalpadb-api.war"
WAR_PATH="build/libs/${WAR_NAME}"
CONTEXT_PATH="/kalpadb-api"

# 프로필 인자 확인
PROFILE=$1

if [ -z "$PROFILE" ]; then
  echo -e "${RED}[오류] 프로필을 입력하세요.${NC}"
  echo -e "사용법: $0 <profile>"
  echo -e "사용 가능한 프로필: ${VALID_PROFILES[*]}"
  exit 1
fi

# 유효한 프로필인지 확인
VALID=false
for p in "${VALID_PROFILES[@]}"; do
  if [ "$p" = "$PROFILE" ]; then
    VALID=true
    break
  fi
done

if [ "$VALID" = false ]; then
  echo -e "${RED}[오류] 알 수 없는 프로필: ${PROFILE}${NC}"
  echo -e "사용 가능한 프로필: ${VALID_PROFILES[*]}"
  exit 1
fi

# 빌드 정보 출력
echo -e "${GREEN}==================== WAR 빌드 정보 ====================${NC}"
echo -e "${BLUE}프로젝트: kalpadb-api${NC}"
echo -e "${BLUE}프로필: ${PROFILE}${NC}"
echo -e "${BLUE}WAR 파일: ${WAR_PATH}${NC}"
echo -e "${BLUE}컨텍스트 경로: ${CONTEXT_PATH}${NC}"
echo ""
echo -e "${YELLOW}다음 작업을 수행합니다:${NC}"
echo "  1. JS/CSS 번들 빌드 (npm run build:bundle)"
echo "  2. Gradle 캐시 정리 (clean)"
echo "  3. 프로젝트 빌드 (build -x test, profile=${PROFILE})"
echo "  4. WAR 파일 생성: ${WAR_PATH}"
echo ""
echo -e "${YELLOW}배포 서버 준비사항:${NC}"
echo "  - Tomcat webapps에 ${WAR_NAME} 복사"
echo "  - .env 파일 또는 환경변수로 DB/JWT 설정"
echo ""
echo -e "${BLUE}진행하시려면 Enter를 누르세요. (Ctrl+C로 취소)${NC}"
read -r

echo ""
echo -e "${GREEN}========== 빌드 시작 ==========${NC}"

# JS/CSS 번들 빌드
echo -e "${BLUE}[1/2] npm 번들 빌드 중...${NC}"
npm run build:bundle

# Gradle 빌드 (프로필 적용)
echo -e "${BLUE}[2/2] Gradle 빌드 중 (profile=${PROFILE})...${NC}"
./gradlew clean build -x test -Dspring.profiles.active=${PROFILE}

# .env 파일 복사
if [ -f ".env" ]; then
  cp .env build/libs/.env
  echo -e "${GREEN}✓ .env 파일 복사: build/libs/.env${NC}"
else
  echo -e "${YELLOW}⚠ .env 파일이 없습니다. build/libs/.env 복사 생략${NC}"
fi

echo ""
echo -e "${GREEN}========== 빌드 완료 ==========${NC}"
echo -e "${GREEN}✓ WAR 파일: ${WAR_PATH}${NC}"
echo -e "${GREEN}✓ 적용 프로필: ${PROFILE}${NC}"
echo ""
echo -e "${BLUE}=== 배포 방법 ===${NC}"
echo ""
echo -e "${YELLOW}1. WAR 파일 서버로 전송:${NC}"
echo "   scp ${WAR_PATH} user@server:\$TOMCAT_HOME/webapps/"
echo ""
echo -e "${YELLOW}2. 환경변수 설정 (Docker 예시):${NC}"
echo "   docker run --env-file .env -v \$WEBAPPS:/usr/local/tomcat/webapps tomcat:10"
echo ""
echo -e "${YELLOW}3. Tomcat 재시작:${NC}"
echo "   \$TOMCAT_HOME/bin/shutdown.sh && \$TOMCAT_HOME/bin/startup.sh"
echo ""
echo -e "${YELLOW}4. 접속 URL:${NC}"
echo "   http://서버주소${CONTEXT_PATH}/"
