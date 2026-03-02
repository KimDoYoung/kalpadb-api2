/**
 * Front Office System Product Page Initialization
 *
 * Front Office System 제품 페이지의 Alpine.js 상태 관리 및 기능을 담당합니다.
 */

import { initAlpineBase } from '../modules/alpine-base.js';
import { setupScrollListener } from '../modules/scroll.js';
import { setupThemeLangWatchers } from '../modules/theme-lang-watcher.js';

/**
 * Front Office 페이지 초기화 함수
 * Alpine.js x-data에서 호출됨: x-data="initFrontOfficePage()"
 *
 * @returns {Object} Alpine.js 상태 및 메서드
 */
export function initFrontOfficePage() {
    return {
        ...initAlpineBase(),
        activeTab: 'main', // 'main' 또는 'advanced'
        init() {
            // 공통 초기화: scroll listener, darkMode/lang watcher
            setupScrollListener(this);
            setupThemeLangWatchers(this);
        },
        /**
         * 탭 전환 함수
         * @param {string} tabName - 선택할 탭 이름
         */
        switchTab(tabName) {
            this.activeTab = tabName;
        },
        /**
         * "문의하기" 버튼 클릭 핸들러
         * 연락처 모달을 열거나 연락처 페이지로 이동
         */
        contactUs() {
            let modal = document.getElementById('contact-modal');
            if (modal) {
                modal.showModal();
            } else {
                window.location.href = '/user1/contact';
            }
        }
    };
}
