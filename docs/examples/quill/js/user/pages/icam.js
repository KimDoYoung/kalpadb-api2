/**
 * ICAM Page Logic
 * 업무시스템(ICAM) 페이지의 Alpine.js 초기화 및 상호작용 로직
 */

import { initAlpineBase } from '../modules/alpine-base.js';
import { setupScrollListener } from '../modules/scroll.js';
import { setupThemeLangWatchers } from '../modules/theme-lang-watcher.js';

export function initIcamPage() {
    return {
        ...initAlpineBase(),
        submenuOpen: false,

        /**
         * 초기화 함수
         */
        init() {
            // 1. Scroll 리스너 설정
            setupScrollListener(this);

            // 2. Dark mode와 lang 변경 감시
            setupThemeLangWatchers(this);
        },

        /**
         * 문의하기 버튼 클릭 핸들러
         */
        contactUs() {
            // 모달 오픈 또는 문의 페이지 이동
            const contactModal = document.getElementById('contact-modal');
            if (contactModal) {
                contactModal.showModal();
            } else {
                window.location.href = '/user1/contact';
            }
        }
    };
}
