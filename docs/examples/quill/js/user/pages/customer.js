/**
 * Customer Page Initialization
 * 고객사 페이지 초기화
 */

/**
 * Initialize Customer page with Alpine.js
 */
export function initCustomerPage() {
    return {
        // Use shared state from common_lite.js
        lang: localStorage.getItem('language') || 'ko',
        darkMode: localStorage.getItem('darkMode') === 'true',

        // Header state (required by header fragment)
        scrolled: false,
        mobileMenuOpen: false,
        megaMenuOpen: null,

        /**
         * Initialize page
         */
        init() {
            console.log('[Customer Page] Initialized');
            this.setupImageErrorHandling();
            this.setupLazyLoading();
            this.setupScrollListener();
        },

        /**
         * Setup scroll listener for header
         */
        setupScrollListener() {
            window.addEventListener('scroll', () => {
                this.scrolled = window.scrollY > 50;
            });
        },

        /**
         * Handle image loading errors
         * 이미지 로딩 에러 처리
         */
        setupImageErrorHandling() {
            const images = document.querySelectorAll('.customer-logo');
            images.forEach(img => {
                img.addEventListener('error', (e) => {
                    console.warn('[Customer Page] Image loading failed:', e.target.src);

                    // Hide broken image
                    e.target.style.display = 'none';

                    // Show placeholder if available
                    const placeholder = e.target.parentElement.querySelector('.customer-logo-placeholder');
                    if (placeholder) {
                        placeholder.style.display = 'flex';
                    }
                });

                // Handle successful load
                img.addEventListener('load', (e) => {
                    e.target.classList.add('loaded');
                });
            });
        },

        /**
         * Setup lazy loading for images
         * 이미지 레이지 로딩 설정
         */
        setupLazyLoading() {
            if ('IntersectionObserver' in window) {
                const imageObserver = new IntersectionObserver((entries, observer) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting) {
                            const img = entry.target;

                            // Trigger loading if not already loaded
                            if (img.dataset.src && !img.src) {
                                img.src = img.dataset.src;
                            }

                            observer.unobserve(img);
                        }
                    });
                });

                const images = document.querySelectorAll('.customer-logo');
                images.forEach(img => imageObserver.observe(img));
            }
        },

        /**
         * Toggle language
         */
        toggleLanguage() {
            this.lang = this.lang === 'ko' ? 'en' : 'ko';
            localStorage.setItem('language', this.lang);
            console.log('[Customer Page] Language changed to:', this.lang);
        },

        /**
         * Toggle dark mode
         */
        toggleDarkMode() {
            this.darkMode = !this.darkMode;
            localStorage.setItem('darkMode', this.darkMode.toString());
            console.log('[Customer Page] Dark mode:', this.darkMode);
        }
    };
}

// Make function globally available
window.initCustomerPage = initCustomerPage;
