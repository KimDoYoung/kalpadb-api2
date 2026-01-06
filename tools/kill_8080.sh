#!/bin/bash

# Kill processes using port 8080 for kalpadb-api2
# Usage: ./tools/kill_8080.sh

PORT=8080

echo "=========================================="
echo "포트 $PORT 정리 시작"
echo "=========================================="
echo ""

echo "🔍 포트 $PORT를 사용하는 프로세스 확인 중..."
echo ""

# Check what's using port $PORT
PROCESSES=$(netstat -ano 2>/dev/null | grep :$PORT)

if [ -z "$PROCESSES" ]; then
    echo "✅ 포트 $PORT를 사용하는 프로세스가 없습니다."
    echo ""
    echo "=========================================="
    echo "✅ 포트 $PORT는 이미 FREE 상태입니다!"
    echo "=========================================="
    exit 0
fi

echo "📋 포트 $PORT를 사용하는 프로세스 목록:"
echo "$PROCESSES"
echo ""

# Extract PIDs and kill them
PIDS=$(netstat -ano 2>/dev/null | grep :$PORT | awk '{print $NF}' | sort | uniq)

if [ ! -z "$PIDS" ]; then
    for PID in $PIDS; do
        if [ ! -z "$PID" ] && [ "$PID" != "0" ]; then
            echo "🔄 PID $PID 프로세스 종료 중..."

            # Get process name
            PROCESS_NAME=$(tasklist 2>/dev/null | grep " $PID " | awk '{print $1}' 2>/dev/null || echo "Unknown")
            echo "   프로세스명: $PROCESS_NAME"

            taskkill //f //pid $PID 2>/dev/null
            if [ $? -eq 0 ]; then
                echo "   ✅ PID $PID 종료 완료"
            else
                echo "   ⚠️ PID $PID 종료 실패"
            fi
        fi
    done
fi

echo ""
echo "🧹 Gradle 데몬 정지 중..."
cd "$(dirname "$0")/.." && ./gradlew --stop 2>/dev/null || true
echo "   ✅ Gradle 데몬 정지 완료"

echo ""
echo "⏳ 프로세스 완전 종료 대기 중... (2초)"
sleep 2

echo ""
echo "🔍 포트 $PORT 상태 확인 중..."
sleep 1

REMAINING=$(netstat -ano 2>/dev/null | grep :$PORT)

echo ""
echo "=========================================="
if [ -z "$REMAINING" ]; then
    echo "✅ 포트 $PORT은 완전히 FREE 상태입니다!"
    echo "=========================================="
    echo ""
    echo "🚀 서버를 실행하려면:"
    echo "   ./gradlew bootRun"
    exit 0
else
    echo "⚠️ 경고: 포트 $PORT를 여전히 사용 중입니다!"
    echo "=========================================="
    echo ""
    echo "📋 사용 중인 프로세스:"
    echo "$REMAINING"
    echo ""
    echo "🔧 다시 정리하려면:"
    echo "   bash tools/kill_8080.sh"
    exit 1
fi