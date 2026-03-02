/**
 * Index1 Page Initialization
 * src/main/resources/templates/user1/index1.html용 JavaScript
 */

import { initAlpineBase } from '../modules/alpine-base.js';
import { setupScrollListener } from '../modules/scroll.js';
import { setupThemeLangWatchers } from '../modules/theme-lang-watcher.js';
import { initIndexSwipers } from '../modules/swiper-init.js';

/**
 * Index1 Alpine Data
 */
export function initIndexPage() {
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

            // 3. Swiper 초기화 (Hero + Voices)
            initIndexSwipers();
        }
    };
}
