/**
 * Organization Page Initialization
 * src/main/resources/templates/user1/company/organization.html용 JavaScript
 */

import { initAlpineBase } from '../modules/alpine-base.js';
import { setupScrollListener } from '../modules/scroll.js';
import { setupThemeLangWatchers } from '../modules/theme-lang-watcher.js';

/**
 * Organization Page Alpine Data
 */
export function initOrganizationPage() {
    return {
        ...initAlpineBase(),

        // Company submenu state
        submenuOpen: false,

        // Mobile expand/collapse state
        expandedMobile: {},

        // 영문 약어 매핑
        abbreviations: {
            "General Shareholders' Meeting": "Shareholders",
            "Board of Directors": "Board",
            "Auditor": "Auditor",
            "Chief Executive Officer": "CEO",
            "Administration Division": "Admin Div.",
            "Strategic Marketing Division": "Strategy Mkt.",
            "Financial Services Division 1": "FS Div. 1",
            "Financial Services Division 2": "FS Div. 2",
            "IT Development Division": "IT Dev.",
            "Compliance Division": "Compliance",
            "Financial Services Team 1": "FS Team 1",
            "Financial Services Team 2": "FS Team 2",
            "Discretionary Support Team": "DST",
            "Operations Support Team": "OPS"
        },

        // 조직 데이터 (백엔드에서 주입됨)
        orgData: window.ORGANIZATION_DATA || {},

        /**
         * 언어에 따른 표시 이름 가져오기
         * @param {Object} nameObj - { ko: '한국어', en: '영문' }
         * @returns {string} 표시할 이름
         */
        getDisplayName(nameObj) {
            if (!nameObj) return '';
            const name = nameObj[this.lang];
            // 영문이고 약어가 있으면 약어 사용
            if (this.lang === 'en' && this.abbreviations[name]) {
                return this.abbreviations[name];
            }
            return name;
        },

        /**
         * 모바일 부서 토글
         * @param {number} index - 부서 인덱스
         */
        toggleMobile(index) {
            this.$nextTick(() => {
                this.expandedMobile[index] = !this.expandedMobile[index];
            });
        },

        /**
         * x-init에서 호출될 초기화 함수
         */
        init() {
            // 1. Scroll 리스너 설정
            setupScrollListener(this);

            // 2. Dark mode와 lang 변경 감시
            setupThemeLangWatchers(this);

            // 3. 초기 모바일 상태 초기화
            this.expandedMobile = {};
        }
    };
}
