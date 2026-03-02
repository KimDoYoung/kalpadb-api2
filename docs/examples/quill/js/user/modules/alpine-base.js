/**
 * Alpine.js Base Store
 * 모든 사용자 페이지에서 공통으로 사용되는 Alpine 상태 관리
 */

export function initAlpineBase() {
    return {
        darkMode: localStorage.getItem('darkMode') === 'true',
        lang: localStorage.getItem('lang') || 'ko',
        mobileMenuOpen: false,
        megaMenuOpen: null,
        scrolled: false,
        modalOpen: false,
        modalContent: '',

        /**
         * Modal 콘텐츠 로드 및 열기
         */
        openModal(event) {
            const url = event.target.closest('button').getAttribute('data-modal-url');
            if (!url) return;

            fetch(url)
                .then(res => res.text())
                .then(html => {
                    this.modalContent = html;
                    this.modalOpen = true;
                })
                .catch(err => {
                    console.error('Failed to load modal content:', err);
                });
        }
    };
}
