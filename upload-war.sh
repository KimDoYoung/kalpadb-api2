#!/bin/bash

# 변수 설정
USER="kdy987"
HOST="jskn.iptime.org"
PORT="2020"
SOURCE_FILE="./build/libs/kalpadb-api.war"
TARGET_DIR="/home/kdy987/tmp"
DEPLOY_SCRIPT="/home/kdy987/.local/bin/deploy-kalpadb.sh"

echo "1. 파일 전송 시작: $SOURCE_FILE (Port: $PORT)..."

# SFTP 실행 (포트 옵션 위치 주의: -P $PORT가 주소 앞에 와야 함)
sftp -P $PORT -b - $USER@$HOST <<EOF
put $SOURCE_FILE $TARGET_DIR
bye
EOF

if [ $? -eq 0 ]; then
    echo "✅ 전송 완료!"
    echo "2. 서버 배포 스크립트 실행: $DEPLOY_SCRIPT..."
    
    # SSH 실행 (ssh는 소문자 -p를 사용합니다)
    ssh -p $PORT $USER@$HOST "bash $DEPLOY_SCRIPT"
    
    if [ $? -eq 0 ]; then
        echo "🎉 배포 성공!"
    else
        echo "❌ 배포 스크립트 실행 중 오류 발생"
    fi
else
    echo "❌ 파일 전송 실패 (네트워크 연결 또는 경로를 확인하세요)"
    exit 1
fi