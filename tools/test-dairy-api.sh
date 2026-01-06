#!/bin/bash

# Diary API Test Script
# Usage: bash tools/test-dairy-api.sh [ymd] [limit]
# Example: bash tools/test-dairy-api.sh 20260110 5
# Default: Uses tomorrow's date

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

API_URL="http://localhost:8080/api"

# Get tomorrow's date in YYYYMMDD format
# Works on both Linux and macOS
if date -d "+1 day" +%Y%m%d > /dev/null 2>&1; then
  # Linux
  TOMORROW=$(date -d "+1 day" +%Y%m%d)
else
  # macOS
  TOMORROW=$(date -v+1d +%Y%m%d)
fi

# Check if ymd argument is provided
if [ -z "$1" ]; then
  echo -e "${RED}✗ Error: Date argument (ymd) is required${NC}"
  echo ""
  echo -e "${BLUE}Usage:${NC}"
  echo "  bash tools/test-dairy-api.sh <ymd> [limit]"
  echo ""
  echo -e "${BLUE}Example:${NC}"
  echo "  bash tools/test-dairy-api.sh 20260110      # Uses specified date"
  echo "  bash tools/test-dairy-api.sh 20260110 10   # Uses date and limit"
  echo ""
  echo -e "${BLUE}Note:${NC} ymd format must be YYYYMMDD (e.g., 20260110)"
  exit 1
fi

TEST_YMD="$1"
LIMIT="${2:-5}"

echo "=========================================="
echo -e "${BLUE}Diary API Test Script${NC}"
echo "=========================================="
echo "Test Date (ymd): $TEST_YMD"
echo "Limit: $LIMIT"
echo ""

# Test 1: Login and get token
echo -e "${YELLOW}[1] Logging in to get JWT token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"kdy987\", \"password\": \"kalpa987\\u0021\"}")

ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
  echo -e "${RED}✗ Login failed${NC}"
  echo "$LOGIN_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ Login successful${NC}"
echo "  Token: ${ACCESS_TOKEN:0:30}..."
echo ""

# Test 2: Create diary
echo -e "${YELLOW}[2] Creating diary for $TEST_YMD...${NC}"
CREATE_RESPONSE=$(curl -s -X POST "$API_URL/dairy" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{\"ymd\": \"$TEST_YMD\", \"content\": \"Test diary content for $TEST_YMD\", \"summary\": \"Test summary\"}")

echo "$CREATE_RESPONSE" | grep -q "success.*true" && echo -e "${GREEN}✓ Create diary success${NC}" || echo -e "${RED}✗ Create diary failed${NC}"
echo "  Response: $(echo $CREATE_RESPONSE | cut -c1-100)..."
echo ""

# Test 3: Get specific diary
echo -e "${YELLOW}[3] Getting diary by ymd: $TEST_YMD${NC}"
GET_RESPONSE=$(curl -s -X GET "$API_URL/dairy/$TEST_YMD" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "$GET_RESPONSE" | grep -q "$TEST_YMD" && echo -e "${GREEN}✓ Get diary success${NC}" || echo -e "${RED}✗ Get diary failed${NC}"
CONTENT=$(echo "$GET_RESPONSE" | grep -o '"content":"[^"]*' | cut -d'"' -f4)
echo "  Content: $CONTENT"
echo ""

# Test 4: Get diary list with pagination
echo -e "${YELLOW}[4] Getting diary list (page=0, size=5)${NC}"
LIST_RESPONSE=$(curl -s -X GET "$API_URL/dairy?page=0&size=5" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "$LIST_RESPONSE" | grep -q "content" && echo -e "${GREEN}✓ Get list success${NC}" || echo -e "${RED}✗ Get list failed${NC}"
TOTAL=$(echo "$LIST_RESPONSE" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
PAGE_SIZE=$(echo "$LIST_RESPONSE" | grep -o '"size":[0-9]*' | cut -d':' -f2)
echo "  Total elements: $TOTAL, Page size: $PAGE_SIZE"
echo ""

# Test 5: Get diary list with date range
echo -e "${YELLOW}[5] Getting diary list with date range (from 20260101 to 20260131)${NC}"
RANGE_RESPONSE=$(curl -s -X GET "$API_URL/dairy?fromYmd=20260101&toYmd=20260131&page=0&size=10" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "$RANGE_RESPONSE" | grep -q "content" && echo -e "${GREEN}✓ Get range filter success${NC}" || echo -e "${RED}✗ Get range filter failed${NC}"
RANGE_TOTAL=$(echo "$RANGE_RESPONSE" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
echo "  Total in range: $RANGE_TOTAL"
echo ""

# Test 6: Get diary list with keyword search
echo -e "${YELLOW}[6] Getting diary list with keyword search (keyword=Test)${NC}"
SEARCH_RESPONSE=$(curl -s -X GET "$API_URL/dairy?keyword=Test&page=0&size=10" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "$SEARCH_RESPONSE" | grep -q "content" && echo -e "${GREEN}✓ Get keyword search success${NC}" || echo -e "${RED}✗ Get keyword search failed${NC}"
SEARCH_TOTAL=$(echo "$SEARCH_RESPONSE" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
echo "  Total with keyword: $SEARCH_TOTAL"
echo ""

# Test 7: Get diary list with summary only
echo -e "${YELLOW}[7] Getting diary list with summaryOnly=true${NC}"
SUMMARY_RESPONSE=$(curl -s -X GET "$API_URL/dairy?summaryOnly=true&page=0&size=5" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "$SUMMARY_RESPONSE" | grep -q "summary" && echo -e "${GREEN}✓ Get summary only success${NC}" || echo -e "${RED}✗ Get summary only failed${NC}"
echo "  Response: $(echo $SUMMARY_RESPONSE | cut -c1-100)..."
echo ""

# Test 8: Get recent diaries
echo -e "${YELLOW}[8] Getting recent $LIMIT diaries${NC}"
RECENT_RESPONSE=$(curl -s -X GET "$API_URL/dairy/recent?limit=$LIMIT" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "$RECENT_RESPONSE" | grep -q "ymd" && echo -e "${GREEN}✓ Get recent diaries success${NC}" || echo -e "${RED}✗ Get recent diaries failed${NC}"
echo "  Response: $(echo $RECENT_RESPONSE | cut -c1-100)..."
echo ""

# Test 9: Update diary
echo -e "${YELLOW}[9] Updating diary for $TEST_YMD${NC}"
UPDATE_RESPONSE=$(curl -s -X PUT "$API_URL/dairy/$TEST_YMD" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{\"content\": \"Updated test diary content for $TEST_YMD\", \"summary\": \"Updated summary\"}")

echo "$UPDATE_RESPONSE" | grep -q "success.*true" && echo -e "${GREEN}✓ Update diary success${NC}" || echo -e "${RED}✗ Update diary failed${NC}"
echo "  Response: $(echo $UPDATE_RESPONSE | cut -c1-100)..."
echo ""

# Test 10: Try to create duplicate diary (should fail)
echo -e "${YELLOW}[10] Creating duplicate diary (should fail with 409)${NC}"
DUPLICATE_RESPONSE=$(curl -s -X POST "$API_URL/dairy" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{\"ymd\": \"$TEST_YMD\", \"content\": \"Duplicate\", \"summary\": \"Dup\"}")

echo "$DUPLICATE_RESPONSE" | grep -q "이미 해당 날짜의 일기가 존재합니다" && echo -e "${GREEN}✓ Duplicate prevention works${NC}" || echo -e "${RED}✗ Duplicate prevention failed${NC}"
echo "  Response: $(echo $DUPLICATE_RESPONSE | cut -c1-100)..."
echo ""

# Test 11: Try invalid date format (should fail)
echo -e "${YELLOW}[11] Creating diary with invalid date format (should fail with 400)${NC}"
INVALID_DATE_RESPONSE=$(curl -s -X POST "$API_URL/dairy" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{\"ymd\": \"2026-01-10\", \"content\": \"Invalid date\", \"summary\": \"Test\"}")

echo "$INVALID_DATE_RESPONSE" | grep -q "YYYYMMDD" && echo -e "${GREEN}✓ Invalid date format rejected${NC}" || echo -e "${RED}✗ Invalid date format check failed${NC}"
echo "  Response: $(echo $INVALID_DATE_RESPONSE | cut -c1-100)..."
echo ""

# Test 12: Try to get non-existent diary (should fail)
echo -e "${YELLOW}[12] Getting non-existent diary (should fail with 404)${NC}"
NOTFOUND_RESPONSE=$(curl -s -X GET "$API_URL/dairy/99999999" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "$NOTFOUND_RESPONSE" | grep -q "일기를 찾을 수 없습니다" && echo -e "${GREEN}✓ Not found handling works${NC}" || echo -e "${RED}✗ Not found handling failed${NC}"
echo "  Response: $(echo $NOTFOUND_RESPONSE | cut -c1-100)..."
echo ""

# Test 13: API without authentication (should fail)
echo -e "${YELLOW}[13] Accessing diary API without token (should fail with 401)${NC}"
NOAUTH_RESPONSE=$(curl -s -X GET "$API_URL/dairy")

echo "$NOAUTH_RESPONSE" | grep -q "success.*false" && echo -e "${GREEN}✓ Authentication check works${NC}" || echo -e "${RED}✗ Authentication check failed${NC}"
echo "  Response: $(echo $NOAUTH_RESPONSE | cut -c1-100)..."
echo ""

echo "=========================================="
echo -e "${BLUE}Test Complete!${NC}"
echo "=========================================="
echo ""
echo -e "${BLUE}Usage:${NC}"
echo "  bash tools/test-dairy-api.sh              # Uses tomorrow's date"
echo "  bash tools/test-dairy-api.sh 20260110     # Uses specified date"
echo "  bash tools/test-dairy-api.sh 20260110 10  # Uses date and limit"
