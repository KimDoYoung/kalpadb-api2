/**
 * 공통 기능 및 설정
 * Korean Fund Service Homepage
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

/**
 * 게시판 공통 JavaScript 함수들 추가
 */

/**
 * 상태 메시지 표시 (전역 함수) - 게시판용
 */
window.showBoardMessage = function(text, type = 'success') {
    // Alpine.js 앱에서 접근 가능하도록 전역 이벤트 발생
    document.dispatchEvent(new CustomEvent('show-board-message', {
        detail: { text, type }
    }));
};

/**
 * 파일 업로드 관련 공통 함수들
 */
const FileUtils = {
    /**
     * 파일 선택 처리
     */
    handleFileSelect(event, attachedFiles, maxSize = 10 * 1024 * 1024) {
        const files = Array.from(event.target.files);
        let hasError = false;

        files.forEach(file => {
            if (file.size > maxSize) {
                window.showBoardMessage(`파일 "${file.name}"이 너무 큽니다. (최대 ${this.formatFileSize(maxSize)})`, 'error');
                hasError = true;
                return;
            }
            attachedFiles.push(file);
        });

        event.target.value = '';
        return !hasError;
    },

    removeFile(attachedFiles, index) {
        attachedFiles.splice(index, 1);
    },

    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },

    getFileIcon(fileName) {
        const extension = fileName.split('.').pop()?.toLowerCase();
        const icons = {
            pdf: '<svg class="w-5 h-5 text-red-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path></svg>',
            doc: '<svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path></svg>',
            docx: '<svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path></svg>',
            xls: '<svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm6 6a1 1 0 10-2 0v1H7a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1v-1z" clip-rule="evenodd"></path></svg>',
            xlsx: '<svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm6 6a1 1 0 10-2 0v1H7a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1v-1z" clip-rule="evenodd"></path></svg>',
            ppt: '<svg class="w-5 h-5 text-orange-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm4 2a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1zm0 4a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1zm-1 4a1 1 0 100 2h4a1 1 0 100-2H7z" clip-rule="evenodd"></path></svg>',
            pptx: '<svg class="w-5 h-5 text-orange-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm4 2a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1zm0 4a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1zm-1 4a1 1 0 100 2h4a1 1 0 100-2H7z" clip-rule="evenodd"></path></svg>',
            jpg: '<svg class="w-5 h-5 text-purple-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd"></path></svg>',
            jpeg: '<svg class="w-5 h-5 text-purple-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd"></path></svg>',
            png: '<svg class="w-5 h-5 text-purple-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd"></path></svg>',
            gif: '<svg class="w-5 h-5 text-purple-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd"></path></svg>'
        };
        return icons[extension] || '<svg class="w-5 h-5 text-gray-400" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clip-rule="evenodd"></path></svg>';
    }
};

/**
 * 폼 검증 관련 공통 함수들
 */
const FormValidator = {
    validateRequired(form, requiredFields) {
        for (const field of requiredFields) {
            if (!form[field] || !form[field].toString().trim()) {
                return {
                    isValid: false,
                    message: `${this.getFieldDisplayName(field)}을(를) 입력해주세요.`
                };
            }
        }
        return { isValid: true, message: '' };
    },

    getFieldDisplayName(fieldName) {
        const displayNames = {
            title: '제목',
            author: '작성자',
            content: '내용',
            name: '이름',
            email: '이메일',
            phone: '전화번호',
            company: '회사명'
        };
        return displayNames[fieldName] || fieldName;
    }
};

/**
 * 쿠키에서 토큰 읽기 함수
 */
function getCookieToken() {
    const cookies = document.cookie.split(';');
    console.log('All cookies:', document.cookie);

    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        console.log('Cookie:', name, '=', value);
        if (name === 'token') {
            console.log('Found token in cookie:', value);
            return value;
        }
    }
    console.log('No token found in cookies');
    return null;
}

/**
 * API 호출 관련 공통 함수들
 */
const ApiUtils = {
    async get(url, config = {}) {
        const cookieToken = getCookieToken();
        const localStorageToken = localStorage.getItem('token');
        const token = cookieToken || localStorageToken;

        console.log('API GET request:', url);
        console.log('Cookie token:', cookieToken);
        console.log('LocalStorage token:', localStorageToken);
        console.log('Using token:', token);

        const headers = { ...config.headers };
        if (token && token !== 'null' && token !== 'undefined') {
            headers['Authorization'] = `Bearer ${token}`;
        }

        return await axios.get(url, {
            headers,
            ...config
        });
    },

    async post(url, data, config = {}) {
        const token = getCookieToken() || localStorage.getItem('token');
        const headers = { ...config.headers };

        if (token && token !== 'null' && token !== 'undefined') {
            headers['Authorization'] = `Bearer ${token}`;
        }

        if (!(data instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }

        return await axios.post(url, data, {
            headers,
            ...config
        });
    },

    async put(url, data, config = {}) {
        const token = getCookieToken() || localStorage.getItem('token');
        const headers = { ...config.headers };

        if (token && token !== 'null' && token !== 'undefined') {
            headers['Authorization'] = `Bearer ${token}`;
        }

        if (!(data instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }

        return await axios.put(url, data, {
            headers,
            ...config
        });
    },

    async patch(url, data, config = {}) {
        const token = getCookieToken() || localStorage.getItem('token');
        const headers = { ...config.headers };

        if (token && token !== 'null' && token !== 'undefined') {
            headers['Authorization'] = `Bearer ${token}`;
        }

        return await axios.patch(url, data, {
            headers,
            ...config
        });
    },

    async postFormData(url, formData, config = {}) {
        const token = getCookieToken() || localStorage.getItem('token');
        const headers = { ...config.headers };

        if (token && token !== 'null' && token !== 'undefined') {
            headers['Authorization'] = `Bearer ${token}`;
        }

        // FormData는 Content-Type을 자동 설정하므로 명시하지 않음
        return await axios.post(url, formData, {
            headers,
            ...config
        });
    },

    async putFormData(url, formData, config = {}) {
        const token = getCookieToken() || localStorage.getItem('token');
        const headers = { ...config.headers };

        if (token && token !== 'null' && token !== 'undefined') {
            headers['Authorization'] = `Bearer ${token}`;
        }

        // FormData는 Content-Type을 자동 설정하므로 명시하지 않음
        return await axios.put(url, formData, {
            headers,
            ...config
        });
    },

    async delete(url, config = {}) {
        const token = getCookieToken() || localStorage.getItem('token');
        const headers = { ...config.headers };

        if (token && token !== 'null' && token !== 'undefined') {
            headers['Authorization'] = `Bearer ${token}`;
        }

        return await axios.delete(url, {
            headers,
            ...config
        });
    },

    handleError(error) {
        if (error.response?.status === 401) {
            this.redirectToLogin();
            return '로그인이 필요합니다.';
        }
        return error.response?.data?.message || error.message || '요청 처리에 실패했습니다.';
    },

    redirectToLogin() {
        localStorage.removeItem('token');
        setTimeout(() => {
            window.location.href = '/admin/login.html';
        }, 1000);
    }
};

/**
 * 인증 관련 공통 함수들
 */
const AuthUtils = {
    async logout() {
        // 쿠키와 localStorage 모두 삭제
        // document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        // localStorage.removeItem('token');
        // window.location.href = '/admin/login';
        // POST 요청으로 서버에 로그아웃 알림
        try {
            await axios.post('/admin/logout');
            localStorage.removeItem('token');
            window.location.href = '/admin/login';
        } catch (error) {
            console.log('Logout request failed:', error);
        }
    },

    getToken() {
        return getCookieToken() || localStorage.getItem('token');
    },

    setToken(token) {
        // 쿠키와 localStorage 모두에 저장
        document.cookie = `token=${token}; path=/; max-age=86400`; // 24시간
        localStorage.setItem('token', token);
    },

    isAuthenticated() {
        return !!this.getToken();
    }
};

// 게시판 공통 유틸리티 전역 노출
window.FileUtils = FileUtils;
window.FormValidator = FormValidator;
window.ApiUtils = ApiUtils;
window.AuthUtils = AuthUtils;