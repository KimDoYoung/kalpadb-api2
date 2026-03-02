/**
 * KFS Homepage - User JavaScript Entry Point
 *
 * 이 파일은 일반 사용자 페이지용 JavaScript 모듈을 번들링하기 위한 Entry Point입니다.
 * esbuild를 통해 kfs_user.js로 빌드됩니다.
 *
 * 구조:
 * - modules/ : 공통 기능 (Alpine base, scroll, theme-lang, swiper)
 * - pages/ : 페이지 특화 로직 (index1.js, ceo.js, brief.js)
 */

// 1. i18n 번역 시스템 먼저 로드 (다른 모듈들이 의존)
import '../i18n/translator.js';

// 2. 공통 기능 로드 (경량 버전)
import './common_user.js';

// 3. 공통 모듈 Export
export { initAlpineBase } from './modules/alpine-base.js';
export { setupScrollListener } from './modules/scroll.js';
export { setupThemeLangWatchers } from './modules/theme-lang-watcher.js';
export { initIndexSwipers, initHeroSwiper, initVoicesSwiper } from './modules/swiper-init.js';

// 4. 페이지별 로직 Export
export { initIndexPage } from './pages/index1.js';
export { initCeoPage } from './pages/ceo.js';
export { initBriefPage } from './pages/brief.js';
export { initOrganizationPage } from './pages/organization.js';
export { initDirectionPage } from './pages/direction.js';
export { initHistoryPage } from './pages/company_history.js';
export { initAreaPage } from './pages/area.js';
export { initArchivePage } from './pages/archive.js';
export { initCustomerPage } from './pages/customer.js';
export { initIcamPage } from './pages/icam.js';
export { initAssetErpPage } from './pages/asseterp.js';
export { initFrontOfficePage } from './pages/frontoffice.js';
export { initMessengerPage } from './pages/messenger.js';
export { initChatbotPage } from './pages/chatbot.js';
export { initGongjiPage } from './pages/gongji.js';
export { initGongjiViewPage } from './pages/gongji_view.js';
export { initGosiPage } from './pages/gosi.js';
export { initGosiViewPage } from './pages/gosi_view.js';
export { initBodoPage } from './pages/bodo.js';
export { initBodoViewPage } from './pages/bodo_view.js';

// 5. Alpine.js x-data에서 접근할 수 있도록 등록
import { initIndexPage } from './pages/index1.js';
import { initCeoPage } from './pages/ceo.js';
import { initBriefPage } from './pages/brief.js';
import { initOrganizationPage } from './pages/organization.js';
import { initDirectionPage } from './pages/direction.js';
import { initHistoryPage } from './pages/company_history.js';
import { initAreaPage } from './pages/area.js';
import { initArchivePage } from './pages/archive.js';
import { initCustomerPage } from './pages/customer.js';
import { initIcamPage } from './pages/icam.js';
import { initAssetErpPage } from './pages/asseterp.js';
import { initFrontOfficePage } from './pages/frontoffice.js';
import { initMessengerPage } from './pages/messenger.js';
import { initChatbotPage } from './pages/chatbot.js';
import { initGongjiPage } from './pages/gongji.js';
import { initGongjiViewPage } from './pages/gongji_view.js';
import { initGosiPage } from './pages/gosi.js';
import { initGosiViewPage } from './pages/gosi_view.js';
import { initBodoPage } from './pages/bodo.js';
import { initBodoViewPage } from './pages/bodo_view.js';

// 전역 함수로 노출 (Alpine.js x-data="initIndexPage()"에서 호출 가능)
window.initIndexPage = initIndexPage;
window.initCeoPage = initCeoPage;
window.initBriefPage = initBriefPage;
window.initOrganizationPage = initOrganizationPage;
window.initDirectionPage = initDirectionPage;
window.initHistoryPage = initHistoryPage;
window.initAreaPage = initAreaPage;
window.initArchivePage = initArchivePage;
window.initCustomerPage = initCustomerPage;
window.initIcamPage = initIcamPage;
window.initAssetErpPage = initAssetErpPage;
window.initFrontOfficePage = initFrontOfficePage;
window.initMessengerPage = initMessengerPage;
window.initChatbotPage = initChatbotPage;
window.initGongjiPage = initGongjiPage;
window.initGongjiViewPage = initGongjiViewPage;
window.initGosiPage = initGosiPage;
window.initGosiViewPage = initGosiViewPage;
window.initBodoPage = initBodoPage;
window.initBodoViewPage = initBodoViewPage;

// Alpine.js 초기화 후 컴포넌트로도 등록 (호환성)
document.addEventListener('alpine:init', () => {
    // Alpine이 이미 x-data 속성을 평가했으므로, 이 이벤트는 나중에 호출되는 코드에서만 필요
    // 예: 동적으로 생성된 요소들에 대해
    Alpine.data('initIndexPage', initIndexPage);
    Alpine.data('initCeoPage', initCeoPage);
    Alpine.data('initBriefPage', initBriefPage);
    Alpine.data('initOrganizationPage', initOrganizationPage);
    Alpine.data('initDirectionPage', initDirectionPage);
    Alpine.data('initHistoryPage', initHistoryPage);
    Alpine.data('initAreaPage', initAreaPage);
    Alpine.data('initArchivePage', initArchivePage);
    Alpine.data('initCustomerPage', initCustomerPage);
    Alpine.data('initIcamPage', initIcamPage);
    Alpine.data('initAssetErpPage', initAssetErpPage);
    Alpine.data('initFrontOfficePage', initFrontOfficePage);
    Alpine.data('initMessengerPage', initMessengerPage);
    Alpine.data('initChatbotPage', initChatbotPage);
    Alpine.data('initGongjiPage', initGongjiPage);
    Alpine.data('initGongjiViewPage', initGongjiViewPage);
    Alpine.data('initGosiPage', initGosiPage);
    Alpine.data('initGosiViewPage', initGosiViewPage);
    Alpine.data('initBodoPage', initBodoPage);
    Alpine.data('initBodoViewPage', initBodoViewPage);
});

console.log('KFS User JavaScript modules loaded successfully');
