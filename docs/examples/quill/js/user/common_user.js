/**
 * 공통 기능 및 설정 (사용자용 경량 버전)
 * Korean Fund Service Homepage
 *
 * 이 파일은 일반 사용자 페이지용 경량 버전입니다.
 * 관리자 전용 기능(API, Auth, 파일 업로드 등)은 제외되었습니다.
 */

// 테마 관리 Alpine.js 컴포넌트
function createThemeStore() {
    return {
        theme: localStorage.getItem('theme') || 'light',

        toggleTheme() {
            this.theme = this.theme === 'dark' ? 'light' : 'dark';
            localStorage.setItem('theme', this.theme);
            this.$dispatch('theme-changed', { theme: this.theme });
        },

        setTheme(theme) {
            if (['light', 'dark'].includes(theme)) {
                this.theme = theme;
                localStorage.setItem('theme', theme);
                this.$dispatch('theme-changed', { theme });
            }
        },

        init() {
            // 시스템 테마 감지
            if (!localStorage.getItem('theme')) {
                if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
                    this.setTheme('dark');
                }
            }

            // 시스템 테마 변경 감지
            if (window.matchMedia) {
                window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
                    if (!localStorage.getItem('theme')) {
                        this.setTheme(e.matches ? 'dark' : 'light');
                    }
                });
            }
        }
    };
}

// 언어 관리 Alpine.js 컴포넌트 (간소화된 버전, 주로 i18n 스토어 사용)
function createLanguageStore() {
    return {
        language: localStorage.getItem('language') || 'ko',

        async toggleLanguage() {
            const newLanguage = this.language === 'ko' ? 'en' : 'ko';
            await Alpine.store('i18n').switchLanguage(newLanguage);
            this.language = newLanguage;
        },

        init() {
            // i18n 스토어와 동기화
            this.$watch('$store.i18n.language', (value) => {
                this.language = value;
            });
        }
    };
}

// 네비게이션 컴포넌트
function createNavigation() {
    return {
        currentPage: '',

        init() {
            // 현재 페이지 감지
            const path = window.location.pathname;
            if (path.includes('about.html')) {
                this.currentPage = 'about';
            } else if (path.includes('sitemap.html')) {
                this.currentPage = 'sitemap';
            } else {
                this.currentPage = 'home';
            }
        },

        isCurrentPage(page) {
            return this.currentPage === page;
        }
    };
}

// 시간 표시 컴포넌트
function createTimeDisplay() {
    return {
        time: '',
        interval: null,

        updateTime() {
            try {
                const language = Alpine.store('i18n') ? Alpine.store('i18n').getCurrentLanguage() : 'ko';
                const locale = language === 'ko' ? 'ko-KR' : 'en-US';
                this.time = new Date().toLocaleString(locale);
            } catch (error) {
                // 폴백: 기본 로케일 사용
                this.time = new Date().toLocaleString();
            }
        },

        init() {
            this.updateTime();
            this.interval = setInterval(() => {
                this.updateTime();
            }, 1000);

            // 언어 변경시 시간 형식 업데이트 - addEventListener 사용
            document.addEventListener('language-changed', () => {
                this.updateTime();
            });
        },

        destroy() {
            if (this.interval) {
                clearInterval(this.interval);
            }
        }
    };
}

// 로딩 상태 관리
function createLoadingState() {
    return {
        isLoading: true,
        error: null,

        init() {
            // i18n 준비 완료 대기 - addEventListener 사용
            document.addEventListener('i18n-ready', () => {
                this.isLoading = false;
            });

            // 초기화 타임아웃 (3초)
            setTimeout(() => {
                if (this.isLoading) {
                    this.error = 'Translation system loading timeout';
                    this.isLoading = false;
                }
            }, 3000);
        }
    };
}

// Alpine.js 글로벌 설정
document.addEventListener('alpine:init', () => {
    // 전역 데이터
    Alpine.data('themeStore', createThemeStore);
    Alpine.data('languageStore', createLanguageStore);
    Alpine.data('navigation', createNavigation);
    Alpine.data('timeDisplay', createTimeDisplay);
    Alpine.data('loadingState', createLoadingState);

    // 페이지별 데이터 (pages/index1.js 참고)
    // initIndexPage는 main_user.js에서 등록됨

    // 전역 매직 프로퍼티
    Alpine.magic('t', () => {
        return (key, params = {}) => {
            return Alpine.store('i18n').t(key, params);
        };
    });

    // 전역 디렉티브 (향후 확장 가능)
    Alpine.directive('translate', (el, { expression }, { evaluate }) => {
        const key = evaluate(expression);
        const updateText = () => {
            el.textContent = Alpine.store('i18n').t(key);
        };

        updateText();

        // 언어 변경시 텍스트 업데이트
        document.addEventListener('language-changed', updateText);
    });
});

// 유틸리티 함수들
const utils = {
    /**
     * 디바운스 함수
     */
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    /**
     * 쓰로틀 함수
     */
    throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    },

    /**
     * 로컬 스토리지 안전 접근
     */
    storage: {
        get(key, defaultValue = null) {
            try {
                const item = localStorage.getItem(key);
                return item ? JSON.parse(item) : defaultValue;
            } catch (error) {
                console.warn(`Failed to read from localStorage: ${key}`, error);
                return defaultValue;
            }
        },

        set(key, value) {
            try {
                localStorage.setItem(key, JSON.stringify(value));
                return true;
            } catch (error) {
                console.warn(`Failed to write to localStorage: ${key}`, error);
                return false;
            }
        },

        remove(key) {
            try {
                localStorage.removeItem(key);
                return true;
            } catch (error) {
                console.warn(`Failed to remove from localStorage: ${key}`, error);
                return false;
            }
        }
    }
};

// 전역 유틸리티 노출
window.utils = utils;
