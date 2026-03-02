/**
 * KFS Homepage - Admin JavaScript Entry Point
 *
 * 이 파일은 관리자 페이지용 JavaScript 모듈을 번들링하기 위한 Entry Point입니다.
 * esbuild를 통해 kfs_admin.js로 빌드됩니다.
 */

// 1. i18n 번역 시스템 (관리자도 다국어 지원 가능)
import '../i18n/translator.js';

// 2. 공통 기능 전체 로드 (API, Auth, 파일 업로드 등 포함)
import './common_admin.js';

// 3. Quill 에디터 관련 (관리자 전용)
// import './quill_setup.js';  // Quill 에디터 공통 설정

console.log('KFS Admin JavaScript modules loaded successfully');
