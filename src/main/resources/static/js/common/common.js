/**
 * 공통 유틸리티 함수
 */

// CSRF 토큰 가져오기
function getCsrfToken() {
  return document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
}

// CSRF 헤더명 가져오기
function getCsrfHeaderName() {
  return document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
}

// API 호출 (CSRF 토큰 자동 포함)
async function apiCall(url, options = {}) {
  const defaults = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // CSRF 토큰 추가
  const csrfToken = getCsrfToken();
  if (csrfToken) {
    defaults.headers[getCsrfHeaderName()] = csrfToken;
  }

  const mergedOptions = {
    ...defaults,
    ...options,
    headers: {
      ...defaults.headers,
      ...options.headers,
    },
  };

  const response = await fetch(url, mergedOptions);
  return response;
}

// 메시지 표시
function showMessage(message, type = 'info') {
  const alertDiv = document.createElement('div');
  alertDiv.className = `alert alert-${type}`;
  alertDiv.textContent = message;
  document.body.prepend(alertDiv);
  setTimeout(() => alertDiv.remove(), 3000);
}

// 로딩 상태 표시
function setLoading(isLoading) {
  if (isLoading) {
    document.body.style.opacity = '0.6';
    document.body.style.pointerEvents = 'none';
  } else {
    document.body.style.opacity = '1';
    document.body.style.pointerEvents = 'auto';
  }
}

// DOM 준비 완료 대기
function onDomReady(callback) {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', callback);
  } else {
    callback();
  }
}

// 배열에서 항목 제거
function removeFromArray(array, item) {
  const index = array.indexOf(item);
  if (index > -1) {
    array.splice(index, 1);
  }
  return array;
}

// 객체 깊은 복사
function deepCopy(obj) {
  return JSON.parse(JSON.stringify(obj));
}

// 날짜 포맷팅
function formatDate(date, format = 'yyyy-MM-dd') {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');

  return format
    .replace('yyyy', year)
    .replace('MM', month)
    .replace('dd', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
}
