/**
 * Swiper Initialization Helper
 * Swiper 인스턴스 생성 헬퍼 함수들
 */

/**
 * Hero Carousel 초기화
 * alpine:init 이벤트 리스너로 등록되며, initIndexPage의 init() 메서드에서 호출됨
 * 이미 alpine:init이 발생한 경우 직접 호출되므로 이벤트를 기다리지 말고 바로 초기화
 */
export function initHeroSwiper() {
    // 약간의 딜레이를 주어 DOM이 완전히 렌더링될 때까지 기다림
    setTimeout(() => {
        if (typeof Swiper !== 'undefined') {
            const heroSwiperEl = document.querySelector('.heroSwiper');
            if (heroSwiperEl && !heroSwiperEl.swiper) {
                new Swiper('.heroSwiper', {
                    slidesPerView: 1,
                    autoplay: {
                        delay: 10000,
                        disableOnInteraction: false,
                    },
                    pagination: {
                        el: '.heroSwiper .swiper-pagination',
                        clickable: true,
                        bulletClass: 'swiper-pagination-bullet',
                        bulletActiveClass: 'swiper-pagination-bullet-active',
                    },
                    navigation: {
                        nextEl: '.heroSwiper .swiper-button-next',
                        prevEl: '.heroSwiper .swiper-button-prev',
                    },
                    loop: true,
                    effect: 'fade',
                    fadeEffect: {
                        crossFade: true,
                    },
                    speed: 1500,
                    preloadImages: true,
                    updateOnImagesReady: true,
                });
                console.log('Hero Swiper initialized successfully');
            }
        }
    }, 100);
}

/**
 * Customer Voices Carousel 초기화
 * alpine:init 이벤트 리스너로 등록되며, initIndexPage의 init() 메서드에서 호출됨
 * 이미 alpine:init이 발생한 경우 직접 호출되므로 이벤트를 기다리지 말고 바로 초기화
 */
export function initVoicesSwiper() {
    // 약간의 딜레이를 주어 DOM이 완전히 렌더링될 때까지 기다림
    setTimeout(() => {
        if (typeof Swiper !== 'undefined') {
            const voicesSwiperEl = document.querySelector('.voicesSwiper');
            if (voicesSwiperEl && !voicesSwiperEl.swiper) {
                new Swiper('.voicesSwiper', {
                    slidesPerView: 1,
                    autoplay: {
                        delay: 8000,
                        disableOnInteraction: false,
                    },
                    pagination: {
                        el: '.voicesSwiper .swiper-pagination',
                        clickable: true,
                        bulletClass: 'swiper-pagination-bullet',
                        bulletActiveClass: 'swiper-pagination-bullet-active',
                    },
                    navigation: {
                        nextEl: '.voicesSwiper .swiper-button-next',
                        prevEl: '.voicesSwiper .swiper-button-prev',
                    },
                    loop: true,
                    effect: 'fade',
                    fadeEffect: {
                        crossFade: true,
                    },
                    speed: 1000,
                });
                console.log('Voices Swiper initialized successfully');
            }
        }
    }, 100);
}

/**
 * 두 개의 Swiper를 함께 초기화 (index1.html용)
 */
export function initIndexSwipers() {
    initHeroSwiper();
    initVoicesSwiper();
}
