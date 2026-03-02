/**
 * Scroll Event Handler
 * 스크롤 감지 (scrolled 상태 업데이트)
 */

export function initScroll() {
    return (Alpine) => {
        document.addEventListener('DOMContentLoaded', () => {
            // Alpine.data나 x-data에서 scrolled 상태를 관리하므로
            // 여기서는 event listener만 설정
            window.addEventListener('scroll', () => {
                // 각 Alpine 인스턴스에서 scrolled = window.scrollY > 50으로 업데이트
                // Alpine의 반응성을 활용하기 위해 x-init에서 처리하는 것이 더 효율적
            });
        });
    };
}

/**
 * x-init에서 사용할 수 있는 헬퍼 함수
 */
export function setupScrollListener(instance) {
    window.addEventListener('scroll', () => {
        instance.scrolled = window.scrollY > 50;
    });
}
