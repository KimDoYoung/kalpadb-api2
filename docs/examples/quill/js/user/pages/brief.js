/**
 * Company Brief Page Initialization
 * src/main/resources/templates/user1/company/brief.html용 JavaScript
 */

import { initAlpineBase } from '../modules/alpine-base.js';
import { setupScrollListener } from '../modules/scroll.js';
import { setupThemeLangWatchers } from '../modules/theme-lang-watcher.js';

/**
 * Brief Page Alpine Data
 */
export function initBriefPage() {
    return {
        ...initAlpineBase(),

        // Company submenu state (brief.html의 nav에서 사용)
        submenuOpen: false,

        /**
         * x-init에서 호출될 초기화 함수
         */
        init() {
            // 1. Scroll 리스너 설정
            setupScrollListener(this);

            // 2. Dark mode와 lang 변경 감시
            setupThemeLangWatchers(this);
        }
    };
}
