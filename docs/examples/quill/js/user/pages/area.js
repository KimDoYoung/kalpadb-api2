/**
 * Business Area Page Initialization
 * src/main/resources/templates/user1/business/area.html용 JavaScript
 */

import { initAlpineBase } from '../modules/alpine-base.js';
import { setupScrollListener } from '../modules/scroll.js';
import { setupThemeLangWatchers } from '../modules/theme-lang-watcher.js';

/**
 * Business Area Page Alpine Data
 */
export function initAreaPage() {
    return {
        ...initAlpineBase(),

        /**
         * x-init에서 호출될 초기화 함수
         */
        init() {
            // 1. Scroll 리스너 설정
            setupScrollListener(this);

            // 2. Dark mode와 lang 변경 감시
            setupThemeLangWatchers(this);
        },

        // Handle contact us click
        contactUs() {
            // Open contact modal or redirect to contact page
            const contactModal = document.getElementById('contactModal');
            if (contactModal) {
                contactModal.style.display = 'block';
            } else {
                // Fallback: redirect to contact page
                window.location.href = '/contact';
            }
        },
    };
}
