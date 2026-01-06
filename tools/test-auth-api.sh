#!/bin/bash

# JWT 인증 API 테스트 스크립트
# 사용법: ./test-auth-api.sh

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# API 기본 URL
BASE_URL="http://localhost:8080"
AUTH_API="$BASE_URL/api/auth"

# 변수
ACCESS_TOKEN=""
REFRESH_TOKEN=""

# 로그 함수
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_test() {
    echo -e "${YELLOW}[TEST]${NC} $1"
}

# JSON 파싱 함수 (jq 없이 grep으로 처리)
extract_token() {
    local json=$1
    local key=$2
    echo "$json" | grep -o "\"$key\":\"[^\"]*" | cut -d'"' -f4
}

# 구분선
print_separator() {
    echo "=================================================="
}

# 메인 테스트 시작
print_separator
log_info "JWT 인증 API 테스트 시작"
log_info "API Base URL: $AUTH_API"
print_separator
echo ""

# Test 1: 로그인 (성공)
log_test "1. 로그인 - 정상 계정"
print_separator

LOGIN_RESPONSE=$(curl -s -X POST "$AUTH_API/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "kdy987",
    "password": "kalpa987!"
  }')

echo "응답:"
echo "$LOGIN_RESPONSE" | grep -o '{"success".*}' || echo "$LOGIN_RESPONSE"
echo ""

# 토큰 추출
ACCESS_TOKEN=$(extract_token "$LOGIN_RESPONSE" "accessToken")
REFRESH_TOKEN=$(extract_token "$LOGIN_RESPONSE" "refreshToken")

if [ -z "$ACCESS_TOKEN" ] || [ -z "$REFRESH_TOKEN" ]; then
    log_error "토큰 추출 실패"
    exit 1
fi

log_success "로그인 성공"
log_info "Access Token: ${ACCESS_TOKEN:0:50}..."
log_info "Refresh Token: ${REFRESH_TOKEN:0:50}..."
echo ""

# Test 2: 로그인 (실패 - 잘못된 비밀번호)
log_test "2. 로그인 - 잘못된 비밀번호"
print_separator

WRONG_PWD_RESPONSE=$(curl -s -X POST "$AUTH_API/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "kdy987",
    "password": "wrongpassword"
  }')

echo "응답:"
echo "$WRONG_PWD_RESPONSE" | grep -o '{"success".*}' || echo "$WRONG_PWD_RESPONSE"
echo ""

if echo "$WRONG_PWD_RESPONSE" | grep -q '"success":false'; then
    log_success "예상대로 실패 (401)"
else
    log_error "예상과 다른 응답"
fi
echo ""

# Test 3: 로그인 (실패 - 존재하지 않는 사용자)
log_test "3. 로그인 - 존재하지 않는 사용자"
print_separator

NO_USER_RESPONSE=$(curl -s -X POST "$AUTH_API/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "nonexistent",
    "password": "password"
  }')

echo "응답:"
echo "$NO_USER_RESPONSE" | grep -o '{"success".*}' || echo "$NO_USER_RESPONSE"
echo ""

if echo "$NO_USER_RESPONSE" | grep -q '"success":false'; then
    log_success "예상대로 실패 (404)"
else
    log_error "예상과 다른 응답"
fi
echo ""

# Test 4: 현재 사용자 정보 조회
log_test "4. 현재 사용자 정보 조회"
print_separator

ME_RESPONSE=$(curl -s -X GET "$AUTH_API/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "응답:"
echo "$ME_RESPONSE" | grep -o '{"success".*}' || echo "$ME_RESPONSE"
echo ""

if echo "$ME_RESPONSE" | grep -q '"success":true'; then
    log_success "사용자 정보 조회 성공"
else
    log_error "사용자 정보 조회 실패"
fi
echo ""

# Test 5: 토큰 검증 (유효한 토큰)
log_test "5. 토큰 검증 - 유효한 토큰"
print_separator

VALIDATE_RESPONSE=$(curl -s -X GET "$AUTH_API/validate" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "응답:"
echo "$VALIDATE_RESPONSE" | grep -o '{"success".*}' || echo "$VALIDATE_RESPONSE"
echo ""

if echo "$VALIDATE_RESPONSE" | grep -q '"valid":true'; then
    log_success "토큰 검증 성공 (유효)"
else
    log_error "토큰이 유효하지 않음"
fi
echo ""

# Test 6: 토큰 검증 (유효하지 않은 토큰)
log_test "6. 토큰 검증 - 유효하지 않은 토큰"
print_separator

INVALID_TOKEN_RESPONSE=$(curl -s -X GET "$AUTH_API/validate" \
  -H "Authorization: Bearer invalid_token_xyz")

echo "응답:"
echo "$INVALID_TOKEN_RESPONSE" | grep -o '{"success".*}' || echo "$INVALID_TOKEN_RESPONSE"
echo ""

if echo "$INVALID_TOKEN_RESPONSE" | grep -q '"valid":false'; then
    log_success "예상대로 유효하지 않은 토큰으로 처리됨"
else
    log_error "예상과 다른 응답"
fi
echo ""

# Test 7: 인증 없이 접근 (실패)
log_test "7. 인증 없이 /me 접근"
print_separator

NO_AUTH_RESPONSE=$(curl -s -X GET "$AUTH_API/me")

echo "응답:"
echo "$NO_AUTH_RESPONSE" | grep -o '{"success".*}' || echo "$NO_AUTH_RESPONSE"
echo ""

if echo "$NO_AUTH_RESPONSE" | grep -q '"success":false'; then
    log_success "예상대로 인증 실패 (401)"
else
    log_error "예상과 다른 응답"
fi
echo ""

# Test 8: 토큰 갱신
log_test "8. 토큰 갱신 (Refresh Token 사용)"
print_separator

REFRESH_RESPONSE=$(curl -s -X POST "$AUTH_API/refresh" \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }")

echo "응답:"
echo "$REFRESH_RESPONSE" | grep -o '{"success".*}' || echo "$REFRESH_RESPONSE"
echo ""

NEW_ACCESS_TOKEN=$(extract_token "$REFRESH_RESPONSE" "accessToken")

if [ -z "$NEW_ACCESS_TOKEN" ]; then
    log_error "새 Access Token 추출 실패"
else
    log_success "토큰 갱신 성공"
    log_info "새 Access Token: ${NEW_ACCESS_TOKEN:0:50}..."
    ACCESS_TOKEN="$NEW_ACCESS_TOKEN"
fi
echo ""

# Test 9: 새로운 Access Token으로 사용자 정보 조회
log_test "9. 새로운 Access Token으로 사용자 정보 조회"
print_separator

ME_RESPONSE2=$(curl -s -X GET "$AUTH_API/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "응답:"
echo "$ME_RESPONSE2" | grep -o '{"success".*}' || echo "$ME_RESPONSE2"
echo ""

if echo "$ME_RESPONSE2" | grep -q '"success":true'; then
    log_success "새 토큰으로 사용자 정보 조회 성공"
else
    log_error "새 토큰으로 조회 실패"
fi
echo ""

# Test 10: 로그아웃
log_test "10. 로그아웃"
print_separator

LOGOUT_RESPONSE=$(curl -s -X POST "$AUTH_API/logout" \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }")

echo "응답:"
echo "$LOGOUT_RESPONSE" | grep -o '{"success".*}' || echo "$LOGOUT_RESPONSE"
echo ""

if echo "$LOGOUT_RESPONSE" | grep -q '"success":true'; then
    log_success "로그아웃 성공"
else
    log_error "로그아웃 실패"
fi
echo ""

# Test 11: 로그아웃 후 Refresh Token 재사용 (실패해야 함)
log_test "11. 로그아웃 후 Refresh Token으로 토큰 갱신 (실패해야 함)"
print_separator

REFRESH_AFTER_LOGOUT=$(curl -s -X POST "$AUTH_API/refresh" \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }")

echo "응답:"
echo "$REFRESH_AFTER_LOGOUT" | grep -o '{"success".*}' || echo "$REFRESH_AFTER_LOGOUT"
echo ""

if echo "$REFRESH_AFTER_LOGOUT" | grep -q '"success":false'; then
    log_success "예상대로 로그아웃된 토큰으로 거부됨"
else
    log_error "로그아웃된 토큰이 여전히 사용 가능 (보안 문제!)"
fi
echo ""

# 완료
print_separator
log_success "모든 테스트 완료!"
print_separator
