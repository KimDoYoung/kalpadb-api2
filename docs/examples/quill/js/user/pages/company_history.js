/**
 * Company History Page Initialization
 * src/main/resources/templates/user1/company/history.html용 JavaScript
 */

import { initAlpineBase } from '../modules/alpine-base.js';
import { setupScrollListener } from '../modules/scroll.js';
import { setupThemeLangWatchers } from '../modules/theme-lang-watcher.js';

/**
 * History Page Alpine Data
 */
export function initHistoryPage() {
    return {
        ...initAlpineBase(),

        // Company submenu state
        submenuOpen: false,

        /**
         * x-init에서 호출될 초기화 함수
         */
        init() {
            // 1. Scroll 리스너 설정
            setupScrollListener(this);

            // 2. Dark mode와 lang 변경 감시
            setupThemeLangWatchers(this);

            // 3. 타임라인 애니메이션 설정
            this.setupTimelineAnimations();
        },

        /**
         * 타임라인 애니메이션 설정 (Intersection Observer 사용)
         */
        setupTimelineAnimations() {
            if (typeof window === 'undefined' || !('IntersectionObserver' in window)) {
                return; // IntersectionObserver 미지원 브라우저
            }

            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        // 요소가 뷰포트에 들어올 때 애니메이션 클래스 추가
                        entry.target.classList.add('animate-in');
                        observer.unobserve(entry.target);
                    }
                });
            }, {
                threshold: 0.1,
                rootMargin: '0px 0px -100px 0px'
            });

            // 모든 타임라인 아이템 관찰
            document.querySelectorAll('.timeline-item-card').forEach(item => {
                observer.observe(item);
            });

            // 페이지 언로드 시 observer 정리
            window.addEventListener('unload', () => {
                observer.disconnect();
            });
        }
    };
}
