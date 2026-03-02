/**
 * 중앙화된 다국어 번역 시스템
 * Korean Fund Service Homepage
 */

class Translator {
    constructor() {
        this.translations = {};
        this.currentLanguage = localStorage.getItem('language') || 'ko';
        this.fallbackLanguage = 'ko';
        this.loadingPromises = new Map();
    }

    /**
     * 번역 데이터 로드
     * @param {string} language - 언어 코드 (ko, en)
     * @returns {Promise} 로딩 프로미스
     */
    async loadTranslations(language) {
        // 이미 로딩 중이거나 완료된 경우 기존 프로미스 반환
        if (this.loadingPromises.has(language)) {
            return this.loadingPromises.get(language);
        }

        // 이미 로드된 경우 즉시 반환
        if (this.translations[language]) {
            return Promise.resolve(this.translations[language]);
        }

        // 새로운 로딩 프로미스 생성
        const loadingPromise = this._fetchTranslations(language);
        this.loadingPromises.set(language, loadingPromise);

        try {
            const translations = await loadingPromise;
            this.translations[language] = translations;
            return translations;
        } catch (error) {
            this.loadingPromises.delete(language);
            throw error;
        }
    }

    /**
     * 실제 번역 데이터 페치
     * @private
     */
    async _fetchTranslations(language) {
        try {
            // 여러 경로 시도
            const possiblePaths = [
                `/assets/js/i18n/${language}.json`,
                `assets/js/i18n/${language}.json`,
                `/js/i18n/${language}.json`,
                `./js/i18n/${language}.json`,
                `js/i18n/${language}.json`
            ];

            let response;
            let lastError;

            for (const path of possiblePaths) {
                try {
                    response = await fetch(path);
                    if (response.ok) {
                        console.log(`Loaded translations from: ${path}`);
                        return await response.json();
                    }
                } catch (err) {
                    lastError = err;
                    console.warn(`Failed to load from ${path}:`, err.message);
                }
            }

            throw new Error(`Failed to load translations for ${language} from any path. Last error: ${lastError?.message}`);
        } catch (error) {
            console.error(`Error loading translations for ${language}:`, error);

            // 폴백 언어와 다른 경우 폴백 시도
            if (language !== this.fallbackLanguage && !this.translations[this.fallbackLanguage]) {
                console.warn(`Attempting to load fallback language: ${this.fallbackLanguage}`);
                return this._fetchTranslations(this.fallbackLanguage);
            }

            throw error;
        }
    }

    /**
     * 번역 키를 실제 텍스트로 변환
     * @param {string} key - 번역 키 (예: "common.company", "home.title")
     * @param {string} [language] - 언어 코드 (미지정시 현재 언어 사용)
     * @param {Object} [params] - 매개변수 객체 (템플릿 치환용)
     * @returns {string} 번역된 텍스트
     */
    t(key, language = null, params = {}) {
        const lang = language || this.currentLanguage;
        const translations = this.translations[lang];

        if (!translations) {
            console.warn(`Translations not loaded for language: ${lang}`);
            return this._getFallbackText(key, params);
        }

        const text = this._getNestedValue(translations, key);

        if (text === undefined || text === null) {
            console.warn(`Translation key not found: ${key} for language: ${lang}`);
            return this._getFallbackText(key, params);
        }

        // 매개변수 치환
        return this._interpolate(text, params);
    }

    /**
     * 중첩된 객체에서 키로 값 추출
     * @private
     */
    _getNestedValue(obj, key) {
        return key.split('.').reduce((current, keyPart) => {
            return current && current[keyPart] !== undefined ? current[keyPart] : undefined;
        }, obj);
    }

    /**
     * 폴백 텍스트 반환
     * @private
     */
    _getFallbackText(key, params) {
        // 폴백 언어 시도
        if (this.currentLanguage !== this.fallbackLanguage && this.translations[this.fallbackLanguage]) {
            const fallbackText = this._getNestedValue(this.translations[this.fallbackLanguage], key);
            if (fallbackText !== undefined) {
                return this._interpolate(fallbackText, params);
            }
        }

        // 최종 폴백: 키 자체 반환
        return key;
    }

    /**
     * 텍스트에 매개변수 치환
     * @private
     */
    _interpolate(text, params) {
        if (typeof text !== 'string' || Object.keys(params).length === 0) {
            return text;
        }

        return text.replace(/\{\{(\w+)\}\}/g, (match, paramName) => {
            return params[paramName] !== undefined ? params[paramName] : match;
        });
    }

    /**
     * 현재 언어 설정
     * @param {string} language - 언어 코드
     */
    setLanguage(language) {
        this.currentLanguage = language;
        localStorage.setItem('language', language);
        document.documentElement.lang = language;
    }

    /**
     * 현재 언어 반환
     * @returns {string} 현재 언어 코드
     */
    getCurrentLanguage() {
        return this.currentLanguage;
    }

    /**
     * 사용 가능한 언어 목록 반환
     * @returns {Array} 언어 코드 배열
     */
    getAvailableLanguages() {
        return ['ko', 'en'];
    }

    /**
     * 번역 데이터가 로드되었는지 확인
     * @param {string} [language] - 언어 코드 (미지정시 현재 언어)
     * @returns {boolean} 로드 여부
     */
    isLoaded(language = null) {
        const lang = language || this.currentLanguage;
        return Boolean(this.translations[lang]);
    }

    /**
     * 번역 시스템 초기화
     * @returns {Promise} 초기화 프로미스
     */
    async initialize() {
        try {
            // 현재 언어와 폴백 언어 로드
            await Promise.all([
                this.loadTranslations(this.currentLanguage),
                this.currentLanguage !== this.fallbackLanguage ?
                    this.loadTranslations(this.fallbackLanguage) :
                    Promise.resolve()
            ]);

            console.log(`Translation system initialized with language: ${this.currentLanguage}`);
            return true;
        } catch (error) {
            console.error('Failed to initialize translation system:', error);
            throw error;
        }
    }
}

// 전역 번역기 인스턴스 생성
window.translator = new Translator();

// Alpine.js 글로벌 스토어 설정
document.addEventListener('alpine:init', () => {
    Alpine.store('i18n', {
        language: window.translator.getCurrentLanguage(),
        isLoaded: false,

        // 번역 함수
        t(key, params = {}) {
            return window.translator.t(key, null, params);
        },

        // 언어 전환
        async switchLanguage(language) {
            try {
                // 새 언어 번역 데이터 로드
                await window.translator.loadTranslations(language);

                // 언어 설정 변경
                window.translator.setLanguage(language);
                this.language = language;

                // 이벤트 디스패치
                document.dispatchEvent(new CustomEvent('language-changed', { detail: { language } }));

                console.log(`Language switched to: ${language}`);
            } catch (error) {
                console.error(`Failed to switch language to ${language}:`, error);
                // 에러 발생시 이전 언어 유지
            }
        },

        // 초기화
        async init() {
            try {
                await window.translator.initialize();
                this.isLoaded = true;
                document.dispatchEvent(new CustomEvent('i18n-ready'));
            } catch (error) {
                console.error('Failed to initialize i18n store:', error);
            }
        },

        // 사용 가능한 언어 목록
        getAvailableLanguages() {
            return window.translator.getAvailableLanguages();
        },

        // 현재 언어 반환
        getCurrentLanguage() {
            return this.language;
        }
    });
});

// DOM 로드 완료 후 자동 초기화
document.addEventListener('DOMContentLoaded', async () => {
    try {
        await window.translator.initialize();
        console.log('Translator ready');
    } catch (error) {
        console.error('Translator initialization failed:', error);
    }
});